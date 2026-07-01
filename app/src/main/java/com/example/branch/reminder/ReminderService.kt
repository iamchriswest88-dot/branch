package com.example.branch.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.branch.R
import com.example.branch.glyph.GlyphAppController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReminderService : Service() {
    private val TAG = "ReminderService"
    private var job: Job? = null
    private val maxDuration = 30 * 60 * 1000L // 30 minutes

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ReminderService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_REMINDER") {
            stopReminder()
            return START_NOT_STICKY
        }

        startForegroundServiceNotification()
        startAnimationLoop()
        
        return START_NOT_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "branch_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active daily glyph reminder"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Branch Reminder")
            .setContentText("Reminder is active. Open app to dismiss.")
            .setSmallIcon(R.mipmap.ic_launcher) // Fallback icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun startAnimationLoop() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            val startTime = System.currentTimeMillis()
            try {
                GlyphAppController.init(applicationContext)

                var attempts = 0
                while (!GlyphAppController.isConnected() && attempts < 30) {
                    delay(100)
                    attempts++
                }

                if (!GlyphAppController.isConnected()) {
                    Log.e(TAG, "Failed to connect to Glyph Service.")
                    stopReminder()
                    return@launch
                }

                val iGlyphService = GlyphAppController.getService() ?: return@launch
                val setAppMatrixColorsMethod = iGlyphService.javaClass.getMethod("setAppMatrixColors", IntArray::class.java)
                val regMethod = iGlyphService.javaClass.getMethod("registerMatrixSDK", String::class.java)

                while (System.currentTimeMillis() - startTime < maxDuration) {
                    // Register control in case it was dropped
                    regMethod.invoke(iGlyphService, "A069P")

                    val frames = ReminderAnimationRenderer.createSweepAnimation()
                    for (frame in frames) {
                        setAppMatrixColorsMethod.invoke(iGlyphService, frame)
                        delay(120) // Slower for battery (8fps)
                    }

                    // Sleep between pulses to save battery
                    delay(2000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in ReminderService animation loop", e)
            } finally {
                stopReminder()
            }
        }
    }

    private fun stopReminder() {
        job?.cancel()
        GlyphAppController.turnOff()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopReminder()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
