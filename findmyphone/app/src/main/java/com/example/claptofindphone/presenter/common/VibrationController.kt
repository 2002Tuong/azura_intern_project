package com.example.claptofindphone.presenter.common

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class VibrationController(var context: Context) {
    private lateinit var VIBRATE_PATTERN: LongArray
    private val vibratorInstance = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

    companion object {
        var isVibrate = false
    }

    fun getIsVibrating() = isVibrate

    @RequiresApi(Build.VERSION_CODES.Q)
    fun vibrate(vibrateType: Int, shouldRepeat: Boolean) {
        isVibrate = true
        GlobalScope.launch(Dispatchers.IO) {
            VIBRATE_PATTERN = when (vibrateType) {
                VibrateMode.Default.value -> {
                    longArrayOf(0, 500, 500, 500, 500, 500, 500)
                }

                VibrateMode.StrongVibrate.value -> {
                    longArrayOf(0, 300, 300, 300, 300, 300, 300)
                }

                VibrateMode.Heartbeat.value -> {
                    longArrayOf(0, 400, 400, 400, 400, 400, 400)
                }

                VibrateMode.Ticktock.value -> {
                    longArrayOf(0, 1000, 1000, 1000, 1000, 1000, 1000)
                }

                else -> {
                    longArrayOf(0, 500, 500, 500, 500, 500, 500)
                }
            }
            val repeatTime = if (shouldRepeat) 0 else -1
            vibratorInstance!!.vibrate(
                VibrationEffect.createWaveform(
                    VIBRATE_PATTERN,
                    repeatTime
                )
            )
        }
    }

    fun stopVibrate() {
        vibratorInstance?.cancel()
        isVibrate = false
    }

    enum class VibrateMode(val value: Int) {
        Default(0),
        StrongVibrate(1),
        Heartbeat(2),
        Ticktock(3)
    }
}