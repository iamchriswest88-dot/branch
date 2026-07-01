package com.example.branch.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.branch.glyph.GlyphAppController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Method

import kotlinx.coroutines.flow.first

class ReminderReceiver : BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "ReminderReceiver triggered!")
        val pendingResult = goAsync()

        // Reschedule for tomorrow
        val prefs = com.example.branch.data.prefs.BranchPrefs(context)
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = prefs.reminderEnabled.first()
            if (enabled) {
                val time = prefs.reminderTime.first()
                AlarmScheduler.scheduleAlarm(context, true, time)
            }
        }

        // Start the Foreground Service to handle the long-running animation
        val serviceIntent = Intent(context, ReminderService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        
        pendingResult.finish()
    }
}
