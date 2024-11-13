package com.livewall.girl.wallpapers.extension

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.parallax.hdvideo.wallpapers.utils.Logger

fun Context.checkPermissions(permissions: Array<String>): Array<String> {
    val list = mutableListOf<String>()
    for (p in permissions) {
        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission(p, packageName))
            list.add(p)
    }
    return list.toTypedArray()
}

fun AppCompatActivity.requestPermissions(code: Int, permissions: Array<String>): Boolean {
    val list = checkPermissions(permissions)
    return if (list.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, list, code)
        false
    } else {
        true
    }
}

fun Fragment.requestPermissions(code: Int, permissions: Array<String>): Boolean {
    val context = context ?: return false
    val list = context.checkPermissions(permissions)
    return if (list.isNotEmpty()) {
        requestPermissions(list, code)
        false
    } else {
        true
    }
}

fun Fragment.requestPermissionStorage(code: Int): Boolean {
    val context = context ?: return false
    val list = context.checkPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    return if (list.isNotEmpty()) {
        requestPermissions(list, code)
        false
    } else {
        true
    }
}

fun AppCompatActivity.requestPermissionStorage(code: Int): Boolean {
    val list = checkPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    return if (list.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, list, code)
        false
    } else {
        true
    }
}

val Context.checkPermissionStorage : Boolean get() = checkPermissions(
    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)).isEmpty()

fun AppCompatActivity.openNetworkSettings() : Boolean {
    if (!openIntent(Intent(Settings.ACTION_WIRELESS_SETTINGS))) {
        return openIntent(Intent(Settings.ACTION_SETTINGS))
    }
    return true
}

fun AppCompatActivity.openIntent(intent: Intent) : Boolean {
    try {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
    } catch (e: Exception) {
        Logger.d(e)
    }
    return false
}