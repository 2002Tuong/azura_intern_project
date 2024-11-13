package com.screentheme.app.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object Logger {
    fun setup(debugMode: Boolean) {
        if (debugMode) {
            Timber.plant(Timber.DebugTree())
        } else {
            runCatching {
                Timber.plant(CrashlyticsTree(FirebaseCrashlytics.getInstance()))
            }
        }
    }

    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }

    fun e(t: Throwable) {
        Timber.e(t)
    }
}
private class CrashlyticsTree(
    private val firebaseCrashlytics: FirebaseCrashlytics
) : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.INFO
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        runCatching {
            if (t != null) {
                firebaseCrashlytics.recordException(t)
            } else {
                firebaseCrashlytics.log("$tag - $message")
            }
        }
    }
}