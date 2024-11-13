package com.bloodpressure.app.camera

interface FingerListener {
    fun onFingerDetected(success: Int)
    fun onFingerDetectFailed(failed: Int, pixelAverage: Int)
}