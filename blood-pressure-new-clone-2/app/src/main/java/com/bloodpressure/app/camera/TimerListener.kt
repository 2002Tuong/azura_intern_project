package com.bloodpressure.app.camera

interface TimerListener {
    fun onTimerStarted()
    fun onTimerRunning(progress: Int)
    fun onTimerStopped()
}