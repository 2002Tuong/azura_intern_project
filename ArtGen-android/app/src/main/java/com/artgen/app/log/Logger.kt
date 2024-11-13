package com.artgen.app.log

import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.regex.Pattern
import timber.log.Timber

object Logger {
    fun setup(debugMode: Boolean) {
        if (debugMode) {
            Timber.plant(DebugTree())
        } else {
            runCatching {
                Timber.plant(CrashlyticsTree(FirebaseCrashlytics.getInstance()))
            }
        }
    }

    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    fun e(t: Throwable) {
        Timber.e(t)
    }
}

private class DebugTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, createClassTag(), message, t)
    }

    private fun createClassTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= CALL_STACK_INDEX) {
            throw IllegalStateException("Synthetic stacktrace didn't have enough elements: are you using proguard?")
        }
        var tag = stackTrace[CALL_STACK_INDEX].className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        // Tag length limit was removed in API 24.
        return when {
            Build.VERSION.SDK_INT >= 24 || tag.length <= MAX_TAG_LENGTH -> tag
            else -> tag.substring(0, MAX_TAG_LENGTH)
        }
    }

    companion object {
        private const val MAX_TAG_LENGTH = 23
        private const val CALL_STACK_INDEX = 7
        private val ANONYMOUS_CLASS by lazy { Pattern.compile("(\\$\\d+)+$") }
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
