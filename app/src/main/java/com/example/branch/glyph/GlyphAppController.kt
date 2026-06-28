package com.example.branch.glyph

import android.content.Context
import android.util.Log
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject

/**
 * Wraps the Nothing Glyph Matrix SDK.
 * Works with the stub SDK (no-ops) or the real AAR.
 * Handles init, emblem display, countdown display, and cleanup.
 */
object GlyphAppController {

    private const val TAG = "GlyphAppController"
    private var manager: GlyphMatrixManager? = null
    private var connected = false
    private var currentGymStreak = 0
    private var currentFlowStreak = 0
    private var activeCategory = "gym"

    fun init(context: Context) {
        try {
            manager = GlyphMatrixManager.getInstance()
            manager?.init(context.applicationContext, object : GlyphMatrixManager.Callback {
                override fun onServiceConnected(mgr: GlyphMatrixManager) {
                    connected = true
                    mgr.register(Glyph.DEVICE_25111p)
                    Log.d(TAG, "Glyph service connected")
                }
                override fun onServiceDisconnected() {
                    connected = false
                    Log.d(TAG, "Glyph service disconnected")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Glyph init failed: ${e.message}")
        }
    }

    /**
     * Shows the emblem for [category] ("gym" or "flow").
     * Completing either discipline updates the Glyph to show that discipline's emblem.
     */
    fun showEmblem(category: String, gymStreak: Int, flowStreak: Int) {
        currentGymStreak = gymStreak
        currentFlowStreak = flowStreak
        activeCategory = category
        pushEmblem(category, if (category == "gym") gymStreak else flowStreak)
    }

    fun showCountdown(secondsRemaining: Int) {
        if (!connected) return
        try {
            val grid = CountdownRenderer.render(secondsRemaining)
            pushGrid(grid)
        } catch (e: Exception) {
            Log.w(TAG, "Glyph countdown push failed: ${e.message}")
        }
    }

    fun restoreEmblem() {
        val streak = if (activeCategory == "gym") currentGymStreak else currentFlowStreak
        pushEmblem(activeCategory, streak)
    }

    fun release() {
        try { manager?.unInit() } catch (e: Exception) { /* ignore */ }
        manager = null
        connected = false
    }

    private fun pushEmblem(category: String, filledSections: Int) {
        if (!connected) return
        try {
            val grid = EmblemRenderer.render(category, filledSections)
            pushGrid(grid)
        } catch (e: Exception) {
            Log.w(TAG, "Glyph emblem push failed: ${e.message}")
        }
    }

    private fun pushGrid(grid: IntArray) {
        try {
            val obj = GlyphMatrixObject(grid)
            val frame = GlyphMatrixFrame.Builder()
                .addTop(obj)
                .build()
            manager?.setAppMatrixFrame(frame.render())
        } catch (e: Exception) {
            Log.w(TAG, "Glyph frame push failed: ${e.message}")
        }
    }
}
