package com.example.branch.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted. Restoring Glyph reminder alarm...")
            val prefs = com.example.branch.data.prefs.BranchPrefs(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = prefs.reminderEnabled.first()
                if (enabled) {
                    val time = prefs.reminderTime.first()
                    AlarmScheduler.scheduleAlarm(context, true, time)
                }
            }
        }
    }
}
