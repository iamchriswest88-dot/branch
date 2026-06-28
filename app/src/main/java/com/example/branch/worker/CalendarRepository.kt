package com.example.branch.worker

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId

/**
 * Mirrors Branch plan days into the device's default calendar.
 * Requires READ_CALENDAR + WRITE_CALENDAR permissions (requested at runtime in PlanScreen).
 */
class CalendarRepository(private val context: Context) {

    private val TAG = "CalendarRepo"

    private fun getDefaultCalendarId(): Long? {
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(CalendarContract.Calendars._ID)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Calendar permission denied")
            null
        }
    }

    /** Insert an all-day event for [dateKey] labelled by [label] (e.g. "Branch · GYM"). */
    fun addEvent(dateKey: String, label: String): Long? {
        val calId = getDefaultCalendarId() ?: return null
        val date  = LocalDate.parse(dateKey)
        val startMs = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs   = startMs + 86_400_000L  // +1 day

        val cv = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID,   calId)
            put(CalendarContract.Events.TITLE,         label)
            put(CalendarContract.Events.DTSTART,       startMs)
            put(CalendarContract.Events.DTEND,         endMs)
            put(CalendarContract.Events.ALL_DAY,       1)
            put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().id)
        }
        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, cv)
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: SecurityException) {
            Log.w(TAG, "Calendar write permission denied")
            null
        }
    }

    /** Delete all Branch events for [dateKey] by title prefix. */
    fun removeEvents(dateKey: String) {
        val date    = LocalDate.parse(dateKey)
        val startMs = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs   = startMs + 86_400_000L
        try {
            context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "${CalendarContract.Events.DTSTART} = ? AND ${CalendarContract.Events.TITLE} LIKE ?",
                arrayOf(startMs.toString(), "Branch%")
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Calendar delete permission denied")
        }
    }
}
