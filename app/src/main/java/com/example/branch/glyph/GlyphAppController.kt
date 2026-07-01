package com.example.branch.glyph

import android.content.Context
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject

object GlyphAppController {

    private const val TAG = "GlyphAppController"
    
    private var matrixManager: GlyphMatrixManager? = null
    private var glyphManager: GlyphManager? = null
    
    private var matrixConnected = false
    private var glyphConnected = false
    
    private var currentGymStreak = 0
    private var currentFlowStreak = 0
    private var activeCategory = "gym"

    fun init(context: Context) {
        val appCtx = context.applicationContext
        
        // Initialize Standard Glyph Manager (Strips)
        try {
            glyphManager = GlyphManager.getInstance(appCtx)
            glyphManager?.init(object : GlyphManager.Callback {
                override fun onServiceConnected(componentName: android.content.ComponentName?) {
                    glyphConnected = true
                    val gm = glyphManager ?: return
                    try {
                        when {
                            Common.is20111() -> gm.register(Glyph.DEVICE_20111)
                            Common.is22111() -> gm.register(Glyph.DEVICE_22111)
                            Common.is23111() -> gm.register(Glyph.DEVICE_23111)
                            Common.is23113() -> gm.register(Glyph.DEVICE_23113)
                            Common.is24111() -> gm.register(Glyph.DEVICE_24111)
                            Common.is25111() -> gm.register(Glyph.DEVICE_25111)
                            else -> gm.register(Glyph.DEVICE_25111)
                        }
                        gm.openSession()
                        Log.d(TAG, "GlyphManager connected and session opened")
                    } catch (e: Exception) {
                        Log.e(TAG, "GlyphManager register/openSession failed: ${e.message}")
                    }
                }
                override fun onServiceDisconnected(componentName: android.content.ComponentName?) {
                    try { glyphManager?.closeSession() } catch (e: Exception) {}
                    glyphConnected = false
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "GlyphManager init failed: ${e.message}")
        }

        // Initialize Matrix Manager (Grid)
        try {
            matrixManager = GlyphMatrixManager.getInstance()
            matrixManager?.init(appCtx, object : GlyphMatrixManager.Callback {
                override fun onServiceConnected(mgr: GlyphMatrixManager) {
                    matrixConnected = true
                    try {
                        when {
                            Common.is23111() -> mgr.register(Glyph.DEVICE_23111)
                            Common.is23113() -> mgr.register(Glyph.DEVICE_23113)
                            Common.is24111() -> mgr.register(Glyph.DEVICE_24111)
                            Common.is25111() -> mgr.register(Glyph.DEVICE_25111)
                            else -> mgr.register(Glyph.DEVICE_25111)
                        }
                        Log.d(TAG, "GlyphMatrixManager connected")
                    } catch (e: Exception) {
                        Log.e(TAG, "GlyphMatrixManager register failed: ${e.message}")
                    }
                }
                override fun onServiceDisconnected() {
                    matrixConnected = false
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "GlyphMatrixManager init failed: ${e.message}")
        }
    }

    fun showEmblem(category: String, gymStreak: Int, flowStreak: Int) {
        currentGymStreak = gymStreak
        currentFlowStreak = flowStreak
        activeCategory = category
        pushEmblem(category, if (category == "gym") gymStreak else flowStreak)
    }

    fun showCountdown(secondsRemaining: Int, totalSeconds: Int) {
        val progressPercent = if (totalSeconds > 0) ((secondsRemaining.toFloat() / totalSeconds.toFloat()) * 100).toInt() else 0

        // Update Matrix Grid
        if (matrixConnected) {
            try {
                val grid = CountdownRenderer.render(secondsRemaining)
                val obj = GlyphMatrixObject(grid)
                val frame = GlyphMatrixFrame.Builder().addTop(obj).build()
                matrixManager?.setAppMatrixFrame(frame.render())
            } catch (e: Exception) {
                Log.w(TAG, "GlyphMatrix countdown push failed: ${e.message}")
            }
        }

        // Update Standard Glyph Progress Strip
        if (glyphConnected) {
            try {
                val gm = glyphManager ?: return
                val builder = gm.glyphFrameBuilder
                if (builder != null) {
                    val frame = builder.build()
                    // If secondsRemaining == 0, turn off
                    if (secondsRemaining <= 0) {
                        gm.turnOff()
                    } else {
                        gm.displayProgress(frame, progressPercent)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Glyph progress push failed: ${e.message}")
            }
        }
    }

    fun restoreEmblem() {
        val streak = if (activeCategory == "gym") currentGymStreak else currentFlowStreak
        pushEmblem(activeCategory, streak)
    }

    fun release() {
        try { glyphManager?.closeSession() } catch (e: Exception) {}
        try { glyphManager?.unInit() } catch (e: Exception) {}
        try { matrixManager?.unInit() } catch (e: Exception) {}
        glyphManager = null
        matrixManager = null
        glyphConnected = false
        matrixConnected = false
    }

    private fun pushEmblem(category: String, filledSections: Int) {
        if (!matrixConnected) return
        try {
            val grid = EmblemRenderer.render(category, filledSections)
            val obj = GlyphMatrixObject(grid)
            val frame = GlyphMatrixFrame.Builder().addTop(obj).build()
            matrixManager?.setAppMatrixFrame(frame.render())
        } catch (e: Exception) {
            Log.w(TAG, "GlyphMatrix emblem push failed: ${e.message}")
        }
    }
}
