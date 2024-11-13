package com.screentheme.app.utils.extensions

import android.Manifest
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Binder
import android.os.PowerManager
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.screentheme.app.R
import com.screentheme.app.utils.helpers.PERMISSION_READ_CONTACTS
import java.io.IOException
import java.lang.reflect.Method


val Context.powerManager: PowerManager get() = getSystemService(Context.POWER_SERVICE) as PowerManager
val Context.audioManager: AudioManager get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

fun Context.getDrawableFromAssets(fileName: String): Drawable? {
    return try {
        val streamData = this.assets.open(fileName)
        val drawable = Drawable.createFromStream(streamData, null)
        streamData.close()
        drawable
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(this, getPermissionString(permId)) == PackageManager.PERMISSION_GRANTED

fun Context.getPermissionString(id: Int) = when (id) {
    PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
    else -> ""
}

fun Context.isAlreadyDefaultDialer(): Boolean {
    val telecomManager = getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
    return this.packageName == telecomManager.defaultDialerPackage
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.arePermissionsGranted(permissions: ArrayList<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun Context.isShowOnLockScreenPermissionEnable(): Boolean {
    return try {
        val manager = this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val method: Method = AppOpsManager::class.java.getDeclaredMethod(
            "checkOpNoThrow",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java
        )
        val result =
            method.invoke(manager, 10020, Binder.getCallingUid(), this.packageName) as Int
        AppOpsManager.MODE_ALLOWED == result
    } catch (e: Exception) {
        false
    }
}

fun Context.getResColor(resId: Int): Int = ContextCompat.getColor(this, resId);

fun Context.rateApp() {
    val applicationID = this.packageName
    val playStoreUri = Uri.parse("market://details?id=$applicationID")

    val rateIntent = Intent(Intent.ACTION_VIEW, playStoreUri)
    rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

    try {
        this.startActivity(rateIntent)
    } catch (e: ActivityNotFoundException) {
        val webPlayStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=$applicationID")
        val webRateIntent = Intent(Intent.ACTION_VIEW, webPlayStoreUri)
        this.startActivity(webRateIntent)
    }
}

fun Context.shareApp() {
    val applicationID = this.packageName
    val appPlayStoreUrl = "https://play.google.com/store/apps/details?id=$applicationID"

    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
    shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.check_out_this_amazing_app) + appPlayStoreUrl)

    val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_via))
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(chooserIntent)
}

fun Context.composeEmail(recipient: String, subject: String) {

    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse("mailto:")
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

    try {
        this.startActivity(Intent.createChooser(emailIntent, "Send Email"))
    } catch (e: ActivityNotFoundException) {
        // Handle case where no email app is available
    }
}

fun Context.openTermOfService() {
    openUrl("https://docs.google.com/document/d/e/2PACX-1vSs4qjjMdluIOd295FICMhl09clZXWYIeePxiokz8TL_RfGmvWc8vQzdbislS0sHJ-kQT-Ic-A91SF4/pub")
}

fun Context.openPrivacy() {
    openUrl("https://docs.google.com/document/d/e/2PACX-1vQNNvj5o3R8iSkjLnrE7e-zJlIzwdlY4El0yPYKeStDfjJDL-fA-tbHWouMguclGWRgGwOxjLGcJ6jI/pub")
}

private fun Context.openUrl(url: String) {
    runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).also {
            this.startActivity(it)
        }
    }
}
