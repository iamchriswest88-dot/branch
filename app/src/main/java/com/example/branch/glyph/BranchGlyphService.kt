package com.example.branch.glyph

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.branch.data.prefs.BranchPrefs
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Glyph Toy Service — registered for the Glyph Button and AOD carousel.
 * Alternates between Gym and Flow emblems every 5 seconds.
 */
class BranchGlyphService : Service() {

    private val TAG = "BranchGlyphService"
    private var manager: GlyphMatrixManager? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var cycleJob: Job? = null
    private var showGym = true

    private val callback = object : GlyphMatrixManager.Callback {
        override fun onServiceConnected(mgr: GlyphMatrixManager) {
            mgr.register(Glyph.DEVICE_25111p)
            startCycle()
        }
        override fun onServiceDisconnected() {
            cycleJob?.cancel()
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            manager = GlyphMatrixManager.getInstance()
            manager?.init(this, callback)
        } catch (e: Exception) {
            Log.w(TAG, "Glyph service init failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        cycleJob?.cancel()
        scope.cancel()
        try { manager?.unInit() } catch (e: Exception) { /* ignore */ }
        super.onDestroy()
    }

    private fun startCycle() {
        cycleJob = scope.launch {
            val prefs = BranchPrefs(applicationContext)
            val db = (applicationContext as com.example.branch.BranchApplication).database
            while (isActive) {
                val todayKey = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE)
                
                // Dynamically compute streak instead of relying on stale prefs
                val planDays = db.planDao().getAllSync()
                val category = if (showGym) "gym" else "flow"
                val doneDates = db.doneDao().getDatesByCategorySync(category)
                val streak = com.example.branch.domain.StreakCalculator.deriveStreak(category, planDays, doneDates, todayKey)
                
                val planDay = db.planDao().getByDateFlow(todayKey).first()
                val isDone = db.doneDao().isDoneFlow(category, todayKey).first()
                
                val isPlannedToday = (if (showGym) planDay?.hasGym == true else planDay?.hasFlow == true) && !isDone
                
                pushEmblem(category, streak, isPlannedToday)
                showGym = !showGym
                delay(5_000)
            }
        }
    }

    private fun pushEmblem(style: String, filledSections: Int, isPlannedToday: Boolean) {
        try {
            val grid = EmblemRenderer.render(style, filledSections, isPlannedToday = isPlannedToday)
            val obj = GlyphMatrixObject(grid)
            val frame = GlyphMatrixFrame.Builder().addTop(obj).build()
            manager?.setMatrixFrame(frame.render())
        } catch (e: Exception) {
            Log.w(TAG, "Glyph push failed: ${e.message}")
        }
    }
}
