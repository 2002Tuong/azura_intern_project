package com.example.videoart.batterychargeranimation.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object TrackingManager {
    fun logEvent(eventName: String, bundle: Bundle = bundleOf()) =
        Firebase.analytics.logEvent(eventName, bundle)

}