package com.artgen.app.utils

import android.view.View
import android.view.ViewParent
import androidx.core.view.postDelayed
import com.artgen.app.log.Logger

internal fun View.requestLayoutWithDelay(delayMillis: Long) {
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
