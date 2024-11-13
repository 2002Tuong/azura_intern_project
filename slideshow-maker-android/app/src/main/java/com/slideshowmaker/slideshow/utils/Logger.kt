package com.slideshowmaker.slideshow.utils

import android.util.Log
import com.slideshowmaker.slideshow.BuildConfig

object Logger {
    private var className: String = ""
    private var methodName: String = ""
    private var lineNumber: Int = 0

    private val isDebuggable: Boolean
        get() = BuildConfig.DEBUG

    private fun createLog(log: String): String {
        val stringBuffer = StringBuffer()
        stringBuffer.append("[")
        stringBuffer.append(methodName)
        stringBuffer.append(":")
        stringBuffer.append(lineNumber)
        stringBuffer.append("]")
        stringBuffer.append(log)

        return stringBuffer.toString()
    }

    private fun getMethodNames(sElements: Array<StackTraceElement>) {
        className = sElements[1].fileName
        methodName = sElements[1].methodName
        lineNumber = sElements[1].lineNumber
    }

    fun e(message: String) {
        if (!isDebuggable)
            return

        // Throwable instance must be created before any methods
        getMethodNames(Throwable().stackTrace)
        Log.e(className, createLog(message))
    }

    fun i(message: String) {
        if (!isDebuggable)
            return

        getMethodNames(Throwable().stackTrace)
        Log.i(className, createLog(message))
    }

    fun v(message: String) {
        if (!isDebuggable)
            return

        getMethodNames(Throwable().stackTrace)
        Log.v(className, createLog(message))
    }

    fun w(message: String) {
        if (!isDebuggable)
            return

        getMethodNames(Throwable().stackTrace)
        Log.w(className, createLog(message))
    }

    fun d(obj: Any) {
        if (!isDebuggable)
            return

        getMethodNames(Throwable().stackTrace)
        Log.d(className, createLog(obj.toString()))
    }
}