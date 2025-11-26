package io.github.alimsrepo.crashguard.utils

import android.util.Log

/**
 * Logger for CrashGuard internal logging
 */
object CrashGuardLogger {

    private var isDebug = false

    fun init(debug: Boolean) {
        isDebug = debug
    }

    fun d(tag: String, message: String) {
        if (isDebug) {
            Log.d("CrashGuard:$tag", message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            Log.e("CrashGuard:$tag", message, throwable)
        }
    }

    fun w(tag: String, message: String) {
        if (isDebug) {
            Log.w("CrashGuard:$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (isDebug) {
            Log.i("CrashGuard:$tag", message)
        }
    }
}