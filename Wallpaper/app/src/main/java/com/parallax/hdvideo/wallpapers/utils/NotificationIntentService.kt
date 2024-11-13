package com.parallax.hdvideo.wallpapers.utils

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast

final class NotificationIntentService: IntentService("notificationIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            "left" -> {
                val leftHandler = Handler(Looper.getMainLooper())
                leftHandler.post {
                    Toast.makeText(
                        baseContext,
                        "You clicked the left button",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            "right" -> {
                val rightHandler = Handler(Looper.getMainLooper())
                rightHandler.post {
                    Toast.makeText(
                        baseContext,
                        "You clicked the right button",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}