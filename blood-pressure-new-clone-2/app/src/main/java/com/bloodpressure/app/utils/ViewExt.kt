package com.bloodpressure.app.utils

import android.view.View
import android.view.ViewParent
import androidx.core.view.postDelayed
import com.google.firebase.remoteconfig.FirebaseRemoteConfig


fun View.requestLayoutWithDelay() {
    if (FirebaseRemoteConfig.getInstance().getBoolean("native_ads_delay_default")) {
        requestLayoutWithDelay1(1500)
    } else {
        requestLayoutWithDelay2(500)
    }
}

fun View.requestLayoutWithDelay1(delayMillis: Long) {
    postDelayed({
        val t = parent?.parent?.parent
        if (t == null) {
            postDelayed({
                val k = parent?.parent?.parent
                if (k != null) {
                    k.requestLayout()
                } else {
                    Logger.d("NativeBanner: parent is null again")
                }
            }, delayMillis)
        } else {
            t.requestLayout()
        }
    }, delayMillis)
}

fun View.requestLayoutWithDelay2(delayMillis: Long) {
    post {
        val t = parent.findAndroidComposeViewParent()
        if (t == null) {
            postDelayed(delayMillis) {
                val k = parent.findAndroidComposeViewParent()
                k?.requestLayout()
            }
        } else {
            t.requestLayout()
        }
    }
}

private fun ViewParent?.findAndroidComposeViewParent(): ViewParent? = when {
    this != null && this::class.java.simpleName == "AndroidComposeView" -> this
    this != null -> this.parent.findAndroidComposeViewParent()
    else -> null
}
