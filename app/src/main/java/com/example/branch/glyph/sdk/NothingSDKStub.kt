/**
 * STUB — Replace by dropping GlyphMatrixSDK.aar into app/libs/ and
 * uncommenting the implementation line in build.gradle.kts, then deleting this file.
 */
@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")
package com.nothing.ketchum

import android.content.Context

class GlyphMatrixManager private constructor() {
    companion object {
        @JvmStatic fun getInstance(): GlyphMatrixManager = GlyphMatrixManager()
    }
    interface Callback {
        fun onServiceConnected(manager: GlyphMatrixManager)
        fun onServiceDisconnected()
    }
    fun init(context: Context, callback: Callback) {}
    fun register(device: Int) {}
    fun unInit() {}
    fun setAppMatrixFrame(frame: ByteArray?) {}
    fun setMatrixFrame(frame: ByteArray?) {}
}

class GlyphMatrixObject(val data: IntArray) {
    class Builder {
        fun buildRandom(): GlyphMatrixObject = GlyphMatrixObject(IntArray(169))
    }
}

class GlyphMatrixFrame {
    class Builder {
        fun addTop(obj: GlyphMatrixObject): Builder = this
        fun build(): GlyphMatrixFrame = GlyphMatrixFrame()
        fun render(): ByteArray = ByteArray(0)
    }
    fun render(): ByteArray = ByteArray(0)
}

object Glyph {
    const val DEVICE_25111p = 3  // Nothing Phone (4a) Pro
}
