package com.screentheme.app.utils.helpers

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.calltheme.app.ui.activity.MainActivity

fun permissionList(): ArrayList<String> {
    val permissions = ArrayList<String>()
    permissions.add(android.Manifest.permission.READ_CONTACTS)

    if (!isSPlus()) {
        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    return permissions
}

fun MainActivity.handlePermissions(permissions: ArrayList<String>, callback: (granted: Boolean) -> Unit) {
    val grantedPermissions = mutableListOf<String>()
    val deniedPermissions = mutableListOf<String>()

    for (permission in permissions) {
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
        onRequestPermissionsResultCallback = { requestCode, permissions, grantResults ->
            if (requestCode == requestCode && permissions.isNotEmpty()) {
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

fun MainActivity.requestWriteSettingsPermission(callback: (granted: Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            .setData(Uri.parse("package:" + this.packageName))
        this.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
    } else {
        callback.invoke(false)
    }

    onRequestWriteSettingsPermissionCallback = { granted ->
        callback.invoke(granted)
    }
}

fun MainActivity.requestXiaomiPermissions(callback: (granted: Boolean) -> Unit) {
    if (isMIUI(this)) {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity"
        )
        intent.putExtra("extra_pkgname", packageName)
        startActivity(intent)
    }

    onRequestXiaomiPermissionCallback = { granted ->
        callback.invoke(granted)
    }
}

fun isMIUI(context: Context): Boolean {
    val intentList = listOf(
        Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT),
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"))
    )
    intentList.forEach { intent ->
        val resolved = resolveActivityList(context, intent).isNotEmpty()
        if (resolved) return true
    }
    return false
}

fun resolveActivityList(context: Context, intent: Intent): List<ResolveInfo> = with(context.packageManager) {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
    } else queryIntentActivities(intent, PackageManager.GET_META_DATA)
}