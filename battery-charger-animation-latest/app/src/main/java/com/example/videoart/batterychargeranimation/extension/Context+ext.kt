package com.example.videoart.batterychargeranimation.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import com.example.videoart.batterychargeranimation.BuildConfig
import com.example.videoart.batterychargeranimation.R

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.shareApp() {
    val appPackageName = this.packageName
    val appPlayStoreUrl = "https://play.google.com/store/apps/details?id=$appPackageName"

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

fun Context.openStore() {
    this.startActivity(Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
    ))
}

