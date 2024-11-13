package com.screentheme.app.utils.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler

class FlashController(private val context: Context) {

    private var cameraManagerInstance: CameraManager? = null
    private var cameraId: String? = null
    private var flashOn: Boolean = false
    private var isFlashing: Boolean = false
    private var flashHandler: Handler? = null
    private var flashRunnable: Runnable? = null

    fun startFlashLight() {
        if (isFlashing) return

        cameraManagerInstance = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val cameraIds = cameraManagerInstance?.cameraIdList
            if (!cameraIds.isNullOrEmpty()) {
                cameraId = cameraIds[0]
                flashHandler = Handler()
                flashRunnable = Runnable {
                    toggleFlash()
                    flashHandler?.postDelayed(flashRunnable!!, 500) // Blink interval: 500 milliseconds
                }
                flashHandler?.post(flashRunnable!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isFlashing = true
    }

    private fun turnOffFlashlight() {
        if (cameraId != null) {
            try {
                cameraManagerInstance?.setTorchMode(cameraId!!, false)
                flashOn = false
            } catch (e: Exception) {

            }
        }
    }

    fun stopFlashLight() {
        if (!isFlashing) return

        // Turn off the flashlight
        turnOffFlashlight()

        // Remove the flash toggle handler and runnables
        flashRunnable?.let { flashHandler?.removeCallbacks(it) }
        flashHandler = null
        flashRunnable = null

        isFlashing = false
    }

    private fun toggleFlash() {
        if (!isFlashing || cameraId == null) {
            return
        }

        try {
            val hasFlashUnit = cameraManagerInstance?.getCameraCharacteristics(cameraId!!)?.get(
                CameraCharacteristics.FLASH_INFO_AVAILABLE)
            if (hasFlashUnit == true) {
                flashOn = !flashOn
                cameraManagerInstance?.setTorchMode(cameraId!!, flashOn)
            } else {
                // Handle the case where the camera does not have a flash unit
                // For example, you can show a message or disable the flashlight functionality
            }
        } catch (e: Exception) {
        }
    }
}