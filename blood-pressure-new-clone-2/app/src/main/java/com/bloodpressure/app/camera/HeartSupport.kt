package com.bloodpressure.app.camera

import android.graphics.SurfaceTexture

interface HeartSupport {
    fun startPulseCheck(timeLimit: Long): HeartSupport
    fun startPulseCheck(): HeartSupport
    fun addOnResultListener(pulseListener: PulseListener): HeartSupport
    fun addOnFingerListener(fingerListener: FingerListener): HeartSupport
    fun addOnTimerListener(timerListener: TimerListener): HeartSupport
    fun stopPulseCheck()
    fun addPreviewSurface(surfaceTexture: SurfaceTexture)
    fun toggleFlash()
    fun enableFlash(isEnabled: Boolean)
    fun turnFlashOn(isON: Boolean)
    fun setAge(age: Int)
}