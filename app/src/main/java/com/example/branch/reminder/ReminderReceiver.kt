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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Ensure glyph system is initialized
                GlyphAppController.init(context)

                // Poll for connection (up to 3 seconds)
                var attempts = 0
                while (!GlyphAppController.isConnected() && attempts < 30) {
                    delay(100)
                    attempts++
                }

                if (!GlyphAppController.isConnected()) {
                    Log.e(TAG, "Failed to connect to Glyph Service in time.")
                    return@launch
                }

                val iGlyphService = GlyphAppController.getService() ?: return@launch
                val setAppMatrixColorsMethod = iGlyphService.javaClass.getMethod("setAppMatrixColors", IntArray::class.java)
                val regMethod = iGlyphService.javaClass.getMethod("registerMatrixSDK", String::class.java)

                // Register control
                regMethod.invoke(iGlyphService, "A069P")

                // Play Animation
                val frames = ReminderAnimationRenderer.createSweepAnimation()
                for (frame in frames) {
                    setAppMatrixColorsMethod.invoke(iGlyphService, frame)
                    delay(80) // ~12 fps
                }

                // Wait a moment at the end before turning off
                delay(500)

            } catch (e: Exception) {
                Log.e(TAG, "Error playing reminder animation", e)
            } finally {
                // Ensure we return control to the system default
                GlyphAppController.turnOff()
                pendingResult.finish()
            }
        }
    }
}
