package com.example.claptofindphone.presenter.common

import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.util.Log
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FlashHelper(var context: Context) {
    private var idOfCamera: String = ""
    private var cameraManagerInstance: CameraManager? = null
    private var flashEnable = false

    init {
        setupFlash()
    }

    private fun setupFlash() {
        val hasFlash = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        if (!hasFlash) {
            Toast.makeText(
                context,
                "Flash not available in this device",
                Toast.LENGTH_LONG
            ).show()
        }
        cameraManagerInstance = context.getSystemService(Service.CAMERA_SERVICE) as CameraManager
        try {
            idOfCamera = cameraManagerInstance!!.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun turnOnFlash(flashMode: Int, duration: Int) {
        try {
            when (flashMode) {
                FlashType.Default.value -> {
                    blinkFlash(duration = duration, shouldBlink = true)
                }

                FlashType.Disco.value -> {
                    blinkFlash(BlinkMode.Disco.value, duration)
                }

                FlashType.SOS.value -> {
                    blinkFlash(BlinkMode.SOS.value, duration)
                }
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun blinkFlash(blinkLevel: Long = 0L, duration: Int, shouldBlink: Boolean = true) {
        flashEnable = false
        GlobalScope.launch(Dispatchers.IO) {
            val startTimeInMillis = System.currentTimeMillis()
            var blinkTimeInMillis = System.currentTimeMillis()
            while (!flashEnable && System.currentTimeMillis() - startTimeInMillis < duration * 1000) {
                turnOnFlash()
                if (shouldBlink && System.currentTimeMillis() - blinkTimeInMillis > blinkLevel) {
                    turnOfFlash()
                    blinkTimeInMillis = System.currentTimeMillis()
                }
            }
            turnOfFlash()
        }
    }

    private fun turnOnFlash() {
        try {
            cameraManagerInstance?.setTorchMode(idOfCamera, true)
        } catch (exception: Exception) {
            Log.d("FlashHelper", "Unable to turn on Flash")
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    private fun turnOfFlash() {
        if (idOfCamera.isNotEmpty()) {
            try {
                cameraManagerInstance?.setTorchMode(idOfCamera, false)
            } catch (exception: Exception) {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }
    }

    fun turnOffFromOutSide() {
        flashEnable = true
        if (idOfCamera.isNotEmpty()) {
            try {
                cameraManagerInstance?.setTorchMode(idOfCamera, false)
            } catch (exception: Exception) {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }
    }

    enum class FlashType(val value: Int) {
        Default(0),
        Disco(1),
        SOS(2)
    }

    enum class BlinkMode(val value: Long) {
        Disco(300),
        SOS(800)
    }
}