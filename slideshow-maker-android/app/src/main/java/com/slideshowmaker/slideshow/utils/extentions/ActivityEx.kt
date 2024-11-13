package com.slideshowmaker.slideshow.utils.extentions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


fun Activity.openAppInStore() {
    openGooglePlayApp(BuildConfig.APPLICATION_ID)
}

fun Context.openGooglePlayApp(packageId: String) {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${packageId}"),
            ),
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${packageId}"),
            ),
        )
    }
}


fun AppCompatActivity.launchAndRepeatOnLifecycleStarted(callback: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            callback.invoke(this)
        }
    }
}

fun Fragment.launchAndRepeatOnLifecycleStarted(callback: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            callback.invoke(this)
        }
    }
}


fun Context.getUriFromFile(file: File): Uri {
    return FileProvider.getUriForFile(
        this,
        this.getString(R.string.file_provider, BuildConfig.APPLICATION_ID),
        file,
    )
}

fun Context.sendEmail(email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(
            Intent.EXTRA_SUBJECT,
            "[Android] $subject",
        )
        putExtra(
            Intent.EXTRA_TEXT,
            "\n\n\n-------------\nSlideshowMaker ${BuildConfig.VERSION_NAME}, ${Build.MODEL}, Android ${Build.VERSION.RELEASE}, ${Locale.getDefault()}",
        )
    }

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {

    }
}

fun Context.isFirstInstall(): Boolean {
    val packageInfo = packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
    return packageInfo.firstInstallTime == packageInfo.lastUpdateTime
}