package com.eschedule.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.eschedule.app.data.NotificationPrefs
import com.eschedule.app.data.model.Reminder
import com.eschedule.app.data.model.SchoolClass
import com.eschedule.app.ui.display
import com.eschedule.app.ui.relativeDay
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Turns classes, reminders and the notification settings into system alarms.
 *
 * It is a pure "plan and set" step: [reschedule] cancels what it planned last time and registers the
 * next alarm for every class (its next meeting) and every open reminder. It is re-run whenever the
 * data or the settings change, after an alarm fires, and when the app starts (which also covers
 * reboots, app updates and clock changes).
 */
class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val store = context.getSharedPreferences("scheduled_alarms", Context.MODE_PRIVATE)

    private data class Planned(
        val kind: Notifier.Kind,
        val id: Long,
        val triggerAt: LocalDateTime,
        val title: String,
        val text: String,
        val sound: Boolean,
        val vibrate: Boolean,
        /** Set for a reminder alerting late; the receiver records it as announced when it fires. */
        val lateKey: String? = null,
        /** Empty for an item's main alarm; [SLOT_DUE] for a reminder's second alert at its due time. */
        val slot: String = "",
    ) {
        val key: String get() = if (slot.isEmpty()) "${kind.id}:$id" else "${kind.id}:$id:$slot"
    }

    fun reschedule(
        classes: List<SchoolClass>,
        reminders: List<Reminder>,
        prefs: NotificationPrefs,
        now: LocalDateTime = LocalDateTime.now(),
    ): Unit = synchronized(lock) {
        cancelPrevious()

        val lateAlerted = store.getStringSet(KEY_LATE_ALERTED, emptySet()).orEmpty().toSet()
        val lateKeys = mutableSetOf<String>()
        val planned = buildList {
            if (prefs.classAlerts) {
                classes.forEach { cls -> nextClassAlarm(cls, prefs, now)?.let { add(it) } }
            }
            if (prefs.reminderAlerts) {
                reminders.forEach { reminder ->
                    addAll(reminderAlarms(reminder, prefs, now, lateAlerted, lateKeys))
                }
            }
        }

        planned.forEach { schedule(it) }
        store.edit()
            .putStringSet(KEY_PLANNED, planned.map { it.key }.toSet())
            // Forget reminders that are no longer inside their late window (due, done or deleted).
            .putStringSet(KEY_LATE_ALERTED, lateAlerted intersect lateKeys)
            .apply()
    }

    /** Called by the receiver once a late reminder alert has been shown, so it isn't shown twice. */
    fun markLateAlerted(lateKey: String): Unit = synchronized(lock) {
        val current = store.getStringSet(KEY_LATE_ALERTED, emptySet()).orEmpty()
        store.edit().putStringSet(KEY_LATE_ALERTED, current + lateKey).commit()
    }

    // ── Planning ──────────────────────────────────────────────────────────────

    /** The next meeting (within two weeks) whose alert time is still in the future. */
    private fun nextClassAlarm(cls: SchoolClass, prefs: NotificationPrefs, now: LocalDateTime): Planned? {
        val lead = prefs.classLeadMinutes.toLong()
        for (offset in 0L..14L) {
            val date = now.toLocalDate().plusDays(offset)
            if (!cls.meetsOn(date)) continue
            val trigger = LocalDateTime.of(date, cls.startTime).minusMinutes(lead)
            if (!trigger.isAfter(now)) continue
            val text = buildString {
                append(if (lead > 0) "Starts in $lead min" else "Starting now")
                append(" · ${cls.startTime.display()}")
                if (cls.location.isNotBlank()) append(" · ${cls.location}")
            }
            return Planned(Notifier.Kind.Class, cls.id, trigger, cls.title, text, prefs.sound, prefs.vibrate)
        }
        return null
    }

    /**
     * A reminder alerts twice: once ahead of time (the notice set in Settings) and again when it is
     * actually due. With no notice set (0 min) only the due-time alert is planned. All-day reminders
     * are due at 8:00 AM on their day.
     *
     * A reminder whose notice time has already passed but which isn't due yet (e.g. added 5 minutes
     * before it's due with a 15-minute notice) gets its notice right away instead — once: [lateAlerted]
     * remembers it so later re-plans don't announce it again.
     */
    private fun reminderAlarms(
        reminder: Reminder,
        prefs: NotificationPrefs,
        now: LocalDateTime,
        lateAlerted: Set<String>,
        lateKeys: MutableSet<String>,
    ): List<Planned> {
        if (reminder.completed) return emptyList()
        val due = LocalDateTime.of(reminder.date, reminder.time ?: LocalTime.of(8, 0))
        if (!due.isAfter(now)) return emptyList()

        val lead = prefs.reminderLeadMinutes.toLong()
        val alarms = mutableListOf<Planned>()

        // 1. The notice, [lead] minutes before it's due.
        if (lead > 0) {
            val onTime = due.minusMinutes(lead)
            var trigger: LocalDateTime? = onTime
            var lateKey: String? = null
            if (!onTime.isAfter(now)) {
                lateKey = lateKey(reminder.id, due).also { lateKeys += it }
                // Skip it when it's already announced, or so close to due that the due alert covers it.
                val skip = lateKey in lateAlerted || Duration.between(now, due).toMinutes() < 1
                trigger = if (skip) null else now.plusSeconds(LATE_ALERT_DELAY_SECONDS)
            }
            if (trigger != null) {
                val day = relativeDay(reminder.date, trigger.toLocalDate())
                val text = "Due $day" + (reminder.time?.let { ", ${it.display()}" } ?: "")
                alarms += Planned(
                    Notifier.Kind.Reminder, reminder.id, trigger, reminder.title, text,
                    prefs.sound, prefs.vibrate, lateKey,
                )
            }
        }

        // 2. The alert when it's actually due.
        val dueText = if (reminder.time != null) "Due now" else "Due today"
        alarms += Planned(
            Notifier.Kind.Reminder, reminder.id, due, reminder.title, dueText,
            prefs.sound, prefs.vibrate, slot = SLOT_DUE,
        )
        return alarms
    }

    /** Keyed by due time too, so moving a reminder's time lets it alert late again. */
    private fun lateKey(id: Long, due: LocalDateTime) = "$id@$due"

    // ── Alarm plumbing ────────────────────────────────────────────────────────

    /** One PendingIntent per item and slot, so a reminder's notice and due alerts don't replace each other. */
    private fun intentFor(kind: Notifier.Kind, id: Long, slot: String): Intent {
        val path = if (slot.isEmpty()) "${kind.id}/$id" else "${kind.id}/$id/$slot"
        return Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION)
            .setData(Uri.parse("eschedule://alarm/$path"))
    }

    private fun schedule(planned: Planned) {
        val intent = intentFor(planned.kind, planned.id, planned.slot).apply {
            putExtra(AlarmReceiver.EXTRA_KIND, planned.kind.id)
            putExtra(AlarmReceiver.EXTRA_ID, planned.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, planned.title)
            putExtra(AlarmReceiver.EXTRA_TEXT, planned.text)
            putExtra(AlarmReceiver.EXTRA_SOUND, planned.sound)
            putExtra(AlarmReceiver.EXTRA_VIBRATE, planned.vibrate)
            planned.lateKey?.let { putExtra(AlarmReceiver.EXTRA_LATE_KEY, it) }
        }
        val pending = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerMillis = planned.triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val exactAllowed = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
        val exactResult = if (exactAllowed) {
            runCatching { alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending) }
        } else {
            null
        }
        if (exactResult == null || exactResult.isFailure) {
            // Exact alarms not permitted: the system may deliver this a few minutes late.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        }
    }

    private fun cancelPrevious() {
        store.getStringSet(KEY_PLANNED, emptySet()).orEmpty().forEach { key ->
            val parts = key.split(":")
            val kind = Notifier.Kind.entries.firstOrNull { it.id == parts.getOrNull(0) } ?: return@forEach
            val id = parts.getOrNull(1)?.toLongOrNull() ?: return@forEach
            val slot = parts.getOrNull(2).orEmpty()
            val existing = PendingIntent.getBroadcast(
                context, 0, intentFor(kind, id, slot), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (existing != null) {
                alarmManager.cancel(existing)
                existing.cancel()
            }
        }
        store.edit().remove(KEY_PLANNED).apply()
    }

    private companion object {
        const val KEY_PLANNED = "planned"
        const val KEY_LATE_ALERTED = "late_alerted"
        const val LATE_ALERT_DELAY_SECONDS = 5L
        const val SLOT_DUE = "due"

        /** The receiver and the re-plan run on different threads and both edit [KEY_LATE_ALERTED]. */
        val lock = Any()
    }
}
