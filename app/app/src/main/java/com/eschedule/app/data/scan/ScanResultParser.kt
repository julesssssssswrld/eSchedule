package com.eschedule.app.data.scan

import com.eschedule.app.data.ScanDay
import com.eschedule.app.data.ScannedClass
import com.eschedule.app.data.parseTimeRange
import com.eschedule.app.data.scanTimeFormat
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/**
 * Turns the model's transcription (`{"rows":[{subject_code, description, from, to, day, room}]}`)
 * into [ScannedClass] rows. The model only copies what is printed; the conversions happen here:
 * "TueThu" becomes two days, "01:00 PM" becomes a time, "L4." becomes the room "L4".
 */
object ScanResultParser {

    fun parse(json: String): List<ScannedClass> {
        val rows = runCatching { JSONObject(json).getJSONArray("rows") }.getOrElse {
            throw GeminiFailure(FailureKind.BadResponse, 200, "The rows were missing from the reply", cause = it)
        }

        val result = mutableListOf<ScannedClass>()
        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i)
            val parsed = row?.let { toClasses(it) }
            if (!parsed.isNullOrEmpty()) result += parsed
        }

        return result
            .distinctBy { listOf(it.title, it.day, it.timeRange, it.location) }
            .sortedWith(compareBy({ it.day.ordinal }, { parseTimeRange(it.timeRange)?.first }))
    }

    private fun toClasses(row: JSONObject): List<ScannedClass> {
        val code = row.text("subject_code")
        val start = parseTime(row.text("from")) ?: return emptyList()
        val end = parseTime(row.text("to")) ?: return emptyList()
        if (code.isBlank() || !end.isAfter(start)) return emptyList()

        val days = parseDays(row.text("day"))
        if (days.isEmpty()) return emptyList()

        val description = row.text("description")
        val title = if (description.isBlank()) code else "$code - $description"
        val range = "${start.format(scanTimeFormat)} - ${end.format(scanTimeFormat)}"
        val room = row.text("room").trim(' ', '.', ',', ';', ':', '-')

        return days.map { day ->
            ScannedClass(
                id = UUID.randomUUID().toString(),
                title = title,
                timeRange = range,
                location = room,
                day = day,
            )
        }
    }

    private fun JSONObject.text(name: String): String =
        if (isNull(name)) "" else optString(name).trim()

    // ── Times ────────────────────────────────────────────────────────────────

    private val twelveHour = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val twentyFourHour = DateTimeFormatter.ofPattern("H:mm", Locale.US)

    /**
     * "01:00 PM", "1:00pm", "1 PM", "13:00" → time; null when it isn't one.
     * Philippine schedules write noon as "12:00 NN" (or "N", "Noon") and midnight as "12:00 MN".
     */
    internal fun parseTime(raw: String): LocalTime? {
        var s = raw.trim().uppercase(Locale.US).replace(".", "")
        if (s.isEmpty()) return null
        if (s == "NOON" || s == "NN") return LocalTime.NOON
        val meridiem = Regex("""\s*(AM|PM|NN|NOON|N|MN)$""").find(s)
        if (meridiem != null) {
            s = s.removeSuffix(meridiem.value).trim()
            if (s.isEmpty()) return null
            if (':' !in s) s += ":00"
            val marker = when (meridiem.groupValues[1]) {
                "AM", "MN" -> "AM"
                else -> "PM"
            }
            return runCatching { LocalTime.parse("$s $marker", twelveHour) }.getOrNull()
        }
        if (':' !in s) return null
        return runCatching { LocalTime.parse(s, twentyFourHour) }.getOrNull()
    }

    // ── Days ─────────────────────────────────────────────────────────────────

    /** Longest first so "thu" wins over "t" and "th" over "t". */
    private val dayTokens: List<Pair<String, ScanDay>> = listOf(
        "monday" to ScanDay.Monday, "mon" to ScanDay.Monday, "mo" to ScanDay.Monday, "m" to ScanDay.Monday,
        "tuesday" to ScanDay.Tuesday, "tues" to ScanDay.Tuesday, "tue" to ScanDay.Tuesday,
        "tu" to ScanDay.Tuesday, "t" to ScanDay.Tuesday,
        "wednesday" to ScanDay.Wednesday, "wed" to ScanDay.Wednesday, "we" to ScanDay.Wednesday, "w" to ScanDay.Wednesday,
        "thursday" to ScanDay.Thursday, "thurs" to ScanDay.Thursday, "thur" to ScanDay.Thursday,
        "thu" to ScanDay.Thursday, "th" to ScanDay.Thursday,
        "friday" to ScanDay.Friday, "fri" to ScanDay.Friday, "fr" to ScanDay.Friday, "f" to ScanDay.Friday,
        "saturday" to ScanDay.Saturday, "sat" to ScanDay.Saturday, "sa" to ScanDay.Saturday, "s" to ScanDay.Saturday,
        "sunday" to ScanDay.Sunday, "sun" to ScanDay.Sunday, "su" to ScanDay.Sunday,
    ).sortedByDescending { it.first.length }

    /**
     * "TueThu" → Tue, Thu; "MWF" → Mon, Wed, Fri; "TTh" → Tue, Thu; "M/W/F" → Mon, Wed, Fri.
     *
     * Each word must be made up entirely of day abbreviations, otherwise the whole word is skipped.
     * That keeps "TBA" / "TBD" / "Online" from being read as days because they start with a day letter.
     */
    internal fun parseDays(raw: String): List<ScanDay> {
        val found = linkedSetOf<ScanDay>()
        raw.lowercase(Locale.US)
            .split(Regex("[^a-z]+"))
            .filter { it.isNotEmpty() }
            .forEach { word -> splitDays(word, 0)?.let { found += it } }
        return found.toList()
    }

    /** Splits [word] from [start] into day tokens, or null when some letters are not part of a day. */
    private fun splitDays(word: String, start: Int): List<ScanDay>? {
        if (start == word.length) return emptyList()
        for ((token, day) in dayTokens) {
            if (!word.startsWith(token, start)) continue
            val rest = splitDays(word, start + token.length) ?: continue
            return listOf(day) + rest
        }
        return null
    }
}
