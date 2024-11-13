package com.bloodpressure.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VibrationManager(context: Context) {

    private val vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    private var isVibrating = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun vibrateHeartbeatPattern() {
        if (isVibrating || vibrator == null || !vibrator.hasVibrator()) {
            return
        }

        isVibrating = true
        coroutineScope.launch {
            while (isVibrating) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrate(createHeartbeatPattern())
                }
                delay(1000)
            }
        }
    }

    fun stopVibration() {
        if (isVibrating) {
            isVibrating = false
            vibrator?.cancel()
        }
    }

    private fun vibrate(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, -1)
            vibrator?.vibrate(vibrationEffect)
        } else {
            vibrator?.vibrate(pattern, -1)
        }
    }

    private fun createHeartbeatPattern(): LongArray {
        val baseVibrationDuration = 20L
        val basePauseDuration = 1000L

        val vibrationIntensities = listOf(1, 2, 3)
        val pattern = mutableListOf<Long>()

        vibrationIntensities.forEach { intensity ->
            pattern.addAll(
                listOf(
                    0,                                      // Start immediately
                    baseVibrationDuration,      // Vibrate for scaled duration
                    basePauseDuration * intensity           // Pause for scaled duration
                )
            )
        }

        return pattern.toLongArray()
    }

    fun vibrateNotification() {
        val pattern = longArrayOf(
            0, // Start immediately
            400, // Vibrate for 400 milliseconds
            200, // Pause for 200 milliseconds
            400, // Vibrate for 400 milliseconds
            200, // Pause for 200 milliseconds
            400 // Vibrate for 400 milliseconds
            // You can extend the pattern as needed
        )
        vibrate(pattern)
    }
}