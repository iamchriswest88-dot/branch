package com.example.branch.glyph

import android.content.Context
import android.util.Log
import com.example.branch.glyph.CountdownRenderer
import com.example.branch.glyph.EmblemRenderer
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphManager
import android.content.ComponentName
import java.lang.reflect.Method

object GlyphAppController {

    private const val TAG = "GlyphAppController"

    private var glyphManager: GlyphManager? = null
    private var glyphConnected = false
    
    // Direct Service Reflection
    private var iGlyphService: Any? = null
    private var setAppMatrixColorsMethod: Method? = null
    private var setFrameColorsMethod: Method? = null

    fun init(appCtx: Context) {
        try {
            glyphManager = GlyphManager.getInstance(appCtx)
            glyphManager?.init(object : GlyphManager.Callback {
                override fun onServiceConnected(componentName: ComponentName?) {
                    glyphConnected = true
                    val mgr = glyphManager ?: return
                    try {
                        mgr.openSession()
                        Log.d(TAG, "GlyphManager connected and session opened")
                        
                        val serviceField = mgr.javaClass.getDeclaredField("mService")
                        serviceField.isAccessible = true
                        val service = serviceField.get(mgr)
                        if (service != null) {
                            iGlyphService = service
                            Log.d(TAG, "Extracted IGlyphService")
                            
                            try {
                                val regMethod = service.javaClass.getMethod("registerMatrixSDK", String::class.java)
                                val regResult = regMethod.invoke(service, Glyph.DEVICE_25111p) as? Boolean ?: false
                                Log.d(TAG, "Matrix SDK registered via reflection: $regResult")
                            } catch (e: Throwable) {
                                Log.w(TAG, "Matrix register method not found/failed")
                            }
                            
                            try {
                                setAppMatrixColorsMethod = service.javaClass.getMethod("setAppMatrixColors", IntArray::class.java)
                                setFrameColorsMethod = service.javaClass.getMethod("setFrameColors", IntArray::class.java)
                            } catch (e: Throwable) {
                                Log.w(TAG, "Methods not found: ${e.message}")
                            }
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "GlyphManager register failed: ${t.message}")
                    }
                }

                override fun onServiceDisconnected(componentName: ComponentName?) {
                    glyphConnected = false
                    iGlyphService = null
                    setAppMatrixColorsMethod = null
                    setFrameColorsMethod = null
                    Log.d(TAG, "GlyphManager disconnected")
                }
            })
        } catch (t: Throwable) {
            Log.e(TAG, "GlyphManager init failed: ${t.message}")
        }
    }

    fun showCountdown(secondsRemaining: Int, totalSeconds: Int) {
        if (!glyphConnected || iGlyphService == null) return
        
        try {
            val grid = CountdownRenderer.render(secondsRemaining)
            setAppMatrixColorsMethod?.invoke(iGlyphService, grid)
        } catch (t: Throwable) {
            Log.w(TAG, "Matrix push failed: ${t.message}")
        }
        
        try {
            if (secondsRemaining <= 0) {
                glyphManager?.turnOff()
            } else {
                val progressPercent = if (totalSeconds > 0) ((secondsRemaining.toFloat() / totalSeconds.toFloat()) * 100).toInt() else 0
                val arraySize = 33
                val frameColors = IntArray(arraySize) { 0 }
                
                val ledsToLight = (arraySize * progressPercent) / 100
                for (i in 0 until ledsToLight) {
                    frameColors[i] = 4095
                }
                
                setFrameColorsMethod?.invoke(iGlyphService, frameColors)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Strip push failed: ${t.message}")
        }
    }

    fun showEmblem(category: String, gymStreak: Int, flowStreak: Int) {
        val filledSections = if (category == "gym") gymStreak else flowStreak
        pushEmblem(category, filledSections)
    }

    private fun pushEmblem(category: String, filledSections: Int) {
        if (!glyphConnected || iGlyphService == null) return
        try {
            val grid = EmblemRenderer.render(category, filledSections)
            setAppMatrixColorsMethod?.invoke(iGlyphService, grid)
        } catch (t: Throwable) {
            Log.w(TAG, "GlyphMatrix emblem push failed: ${t.message}")
        }
    }
}
