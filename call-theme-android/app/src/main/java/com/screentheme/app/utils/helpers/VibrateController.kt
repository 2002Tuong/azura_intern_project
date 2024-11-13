package com.screentheme.app.utils.helpers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibrateController(private val context: Context) {
    private var vibrator: Vibrator? = null

    fun vibrateDevice() {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Vibrate with repeat mode for incoming call
        val vibratePattern = longArrayOf(0, 1000, 1000) // Vibrate 1 second, pause 1 second, repeat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(vibratePattern, 0)
            )
        } else {
            vibrator?.vibrate(vibratePattern, 0)
        }
    }

    fun stopVibration() {
        vibrator?.cancel()
    }
}