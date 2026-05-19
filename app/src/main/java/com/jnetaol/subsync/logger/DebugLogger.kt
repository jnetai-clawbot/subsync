package com.jnetaol.subsync.logger

import android.util.Log

object DebugLogger {
    private const val TAG = "SubSync"
    private var enabled = true

    fun i(code: String, message: String) {
        if (enabled) Log.i(TAG, "[$code] $message")
    }

    fun w(code: String, message: String) {
        if (enabled) Log.w(TAG, "[$code] $message")
    }

    fun e(code: String, message: String, throwable: Throwable? = null) {
        if (enabled) Log.e(TAG, "[$code] $message", throwable)
    }

    fun d(code: String, message: String) {
        if (enabled) Log.d(TAG, "[$code] $message")
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
