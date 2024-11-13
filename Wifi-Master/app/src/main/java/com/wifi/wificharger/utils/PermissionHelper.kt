package com.wifi.wificharger.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wifi.wificharger.ui.main.MainActivity

fun Context.arePermissionsGranted(permissions: ArrayList<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

fun MainActivity.handlePermissions(
    listPermission: ArrayList<String>,
    callback: (granted: Boolean) -> Unit
) {
    val grantedPermissions = mutableListOf<String>()
    val deniedPermissions = mutableListOf<String>()

    for (permission in listPermission) {
        val permissionStatus = ContextCompat.checkSelfPermission(this, permission)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            grantedPermissions.add(permission)
        } else {
            deniedPermissions.add(permission)
        }
    }

    if (deniedPermissions.isEmpty()) {
        // All permissions already granted
        callback(true)
    } else {
        val requestCode = 0
        val deniedPermissionArray = deniedPermissions.toTypedArray()
        val shouldShowRequestPermissionRationale = deniedPermissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }

        ActivityCompat.requestPermissions(this, deniedPermissionArray, requestCode)

        // Handle the permission result in onRequestPermissionsResult callback
        onRequestPermissionsResultCallback = { code, permissions, grantResults ->
            if (code == requestCode && permissions.isNotEmpty()) {
                val grantedPermissionsResult = mutableListOf<String>()
                val deniedPermissionsResult = mutableListOf<String>()

                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        grantedPermissionsResult.add(permissions[i])
                    } else {
                        deniedPermissionsResult.add(permissions[i])
                    }
                }

                if (deniedPermissionsResult.isEmpty()) {
                    // All requested permissions granted
                    callback(true)
                } else {
                    // Some or all requested permissions denied
                    callback(false)
                }
            }
        }

        if (!shouldShowRequestPermissionRationale) {
            // Permission request rationale should be shown
            callback(false)
        }
    }
}

fun MainActivity.requestLocationPermissionIfNeeded(preAction: () -> Unit = {}, onGrantPermission: (Boolean) -> Unit) {
    val accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    val accessCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
    if (!arePermissionsGranted(permissions = arrayListOf(accessFineLocation))) {
        preAction()
        handlePermissions(arrayListOf(accessCoarseLocation, accessFineLocation)) {
            onGrantPermission(it)
        }
    } else {
        onGrantPermission(true)
    }
}
