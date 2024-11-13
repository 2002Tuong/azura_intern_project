package com.parallax.hdvideo.wallpapers.extension

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import java.util.*

fun Context.getMemory() : ActivityManager.MemoryInfo {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo
}

val Context.memoryInGB :  Double get() = getMemory().totalMem / 1073741824.0

fun Context.getString(@StringRes id: Int, locale: Locale) : String? {
    return try {
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        createConfigurationContext(configuration).resources.getString(id)
    } catch (e: Exception) {
        null
    }
}