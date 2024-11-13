package com.example.claptofindphone.presenter.common

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.os.PowerManager

class ScreenUpSupporter {

    companion object {
        fun wakeScreenUp(context: Context) {
            val powerManagerInstance = context.getSystemService(Service.POWER_SERVICE) as PowerManager
            if (!powerManagerInstance.isInteractive) { // if screen is not already on, turn it on (get wake_lock)
                @SuppressLint("InvalidWakeLockTag") val wl = powerManagerInstance.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                    "id:wakeupscreen"
                )
                wl.acquire(1 * 60 * 1000L)
            }
        }
    }

}