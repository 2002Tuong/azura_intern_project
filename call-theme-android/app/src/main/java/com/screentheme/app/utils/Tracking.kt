package com.screentheme.app.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object Tracking {
    fun logEvent(eventName: String, bundle: Bundle = bundleOf()) =
        Firebase.analytics.logEvent(eventName, bundle)
    fun logNativeAdImpression(eventName: String) {
        logEvent(eventName)
    }
}