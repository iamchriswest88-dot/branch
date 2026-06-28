package com.example.branch.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.example.branch.data.model.PlanDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalendarRepository(private val context: Context) {

    private val fmt = DateTimeFormatter.ISO_DATE

    suspend fun syncPlanDay(planDay: PlanDay) = withContext(Dispatchers.IO) {
        // Ensure we have permissions before proceeding
        if (context.checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return@withContext
        }

        val date = LocalDate.parse(planDay.dateKey, fmt)
        val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val calendarId = getPrimaryCalendarId() ?: return@withContext

        // Delete any existing Branch events on this day
        deleteBranchEventsOnDay(startMillis, endMillis)

        // If the day has no gym and no flow, we are done
        if (!planDay.hasGym && !planDay.hasFlow) return@withContext

        // Otherwise, insert the new event
        val title = when {
            planDay.hasGym && planDay.hasFlow -> "Branch: Gym & Flow"
            planDay.hasGym -> "Branch: Gym"
            planDay.hasFlow -> "Branch: Flow"
            else -> "Branch: Rest"
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().id)
            put(CalendarContract.Events.ALL_DAY, 1)
        }

        context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    private fun deleteBranchEventsOnDay(startMillis: Long, endMillis: Long) {
        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} < ?) AND (${CalendarContract.Events.TITLE} LIKE ?)"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString(), "Branch:%")
        
        // Android requires querying the event ID first before deleting
        val projection = arrayOf(CalendarContract.Events._ID)
        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
            while (it.moveToNext()) {
                val eventId = it.getLong(idIndex)
                val deleteUri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                context.contentResolver.delete(deleteUri, null, null)
            }
        }
    }

    private fun getPrimaryCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY
        )
        val selection = "${CalendarContract.Calendars.IS_PRIMARY} = 1"
        
        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        var primaryId: Long? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                primaryId = it.getLong(idIndex)
            }
        }
        
        // Fallback to the first calendar if no primary is found
        if (primaryId == null) {
            val fallbackCursor: Cursor? = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID),
                null,
                null,
                null
            )
            fallbackCursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                    primaryId = it.getLong(idIndex)
                }
            }
        }
        
        return primaryId
    }
}
