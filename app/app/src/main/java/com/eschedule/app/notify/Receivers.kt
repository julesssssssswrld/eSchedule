package com.eschedule.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eschedule.app.EScheduleApp

/** Posts the notification for an alarm that just went off, then plans the next one. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val kind = Notifier.Kind.entries.firstOrNull { it.id == intent.getStringExtra(EXTRA_KIND) } ?: return
        val id = intent.getLongExtra(EXTRA_ID, 0L)

        runCatching {
            Notifier.show(
                context = context,
                notificationId = (kind.id.hashCode() * 31 + id).toInt(),
                kind = kind,
                title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
                text = intent.getStringExtra(EXTRA_TEXT).orEmpty(),
                sound = intent.getBooleanExtra(EXTRA_SOUND, true),
                vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true),
            )
        }

        // A reminder that alerted late (added inside its notice time) must not alert again on re-plan.
        intent.getStringExtra(EXTRA_LATE_KEY)?.let { NotificationScheduler(context).markLateAlerted(it) }

        // A class repeats: plan its next meeting now that this one has been announced.
        (context.applicationContext as EScheduleApp).container.requestNotificationResync()
    }

    companion object {
        const val ACTION = "com.eschedule.app.ALARM"
        const val EXTRA_KIND = "kind"
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_SOUND = "sound"
        const val EXTRA_VIBRATE = "vibrate"
        const val EXTRA_LATE_KEY = "late_key"
    }
}

/**
 * Reboot, app update, clock or time-zone change: alarms are gone or wrong, so plan them again.
 * The process starting is enough (the Application re-plans on start); this just asks explicitly.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as EScheduleApp).container.requestNotificationResync()
    }
}
