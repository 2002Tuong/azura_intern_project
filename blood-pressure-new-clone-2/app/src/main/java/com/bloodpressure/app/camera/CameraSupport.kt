package com.bloodpressure.app.camera

import android.graphics.SurfaceTexture

interface CameraSupport {
    fun open(): CameraSupport
    fun close()
    val isCameraInUse: Boolean
    fun addOnPreviewListener(callBack: PreviewListener)
    fun addPreviewSurface(surface: SurfaceTexture): CameraSupport

    fun toggleFlash()

    fun enableFlash(isEnabled: Boolean)

    fun turnFlashOn(isON: Boolean)

    fun setSurfaceTextureSize(width: Int, height: Int)
}