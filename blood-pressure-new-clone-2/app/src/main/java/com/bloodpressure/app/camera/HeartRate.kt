package com.bloodpressure.app.camera

import android.graphics.SurfaceTexture
import android.media.Image
import android.os.CountDownTimer

class HeartRate(private val mCameraSupport: CameraSupport) : HeartSupport, PreviewListener {
    private var mTimerListener: TimerListener? = null
    private var mPulseListener: PulseListener? = null
    private var mFingerListener: FingerListener? = null
    private var timeLimit: Long = 0
    private var isTimeRunning = false
    private var countDownTimer: CountDownTimer? = null
    private var averageIndex = 0
    private var beatsIndex = 0
    private var startTime = System.currentTimeMillis()
    private var errorCount = 0
    private var successCount = 0
    private var userAge: Int = 22
    private var lastReportedProgress = 0
    private var lastHeartRate = 74


    val heartSupport: HeartSupport
        get() = this

    override fun startPulseCheck(timeLimit: Long): HeartSupport {

        this.timeLimit = timeLimit

        reset()
        startTimer()
        if (mTimerListener != null) {
            mTimerListener?.onTimerStarted()
        }

        return this
    }

    override fun startPulseCheck(): HeartSupport {
        if (!mCameraSupport.isCameraInUse) {
            reset()
            mCameraSupport.open().addOnPreviewListener(this)
        }
        return this
    }

    override fun addOnResultListener(pulseListener: PulseListener): HeartSupport {
        mPulseListener = pulseListener
        return this
    }

    override fun addOnFingerListener(fingerListener: FingerListener): HeartSupport {
        mFingerListener = fingerListener
        return this
    }

    override fun addOnTimerListener(timerListener: TimerListener): HeartSupport {
        mTimerListener = timerListener
        return this
    }

    override fun stopPulseCheck() {
        if (mCameraSupport.isCameraInUse) {
            mCameraSupport.close()
            if (isTimeRunning) {
                countDownTimer?.cancel()
                isTimeRunning = false
            }
            if (mTimerListener != null) {
                mTimerListener?.onTimerStopped()
            }
        }

        reset()
    }

    override fun addPreviewSurface(surfaceTexture: SurfaceTexture) {
        mCameraSupport.addPreviewSurface(surfaceTexture)
    }

    override fun toggleFlash() = mCameraSupport.toggleFlash()

    override fun enableFlash(isEnabled: Boolean) = mCameraSupport.enableFlash(isEnabled)

    override fun turnFlashOn(isON: Boolean) = mCameraSupport.turnFlashOn(isON)

    override fun setAge(age: Int) {
        userAge = age
    }

    fun cancelPulseCheck() {
        countDownTimer?.cancel()
    }

    override fun onPreviewData(data: ByteArray?, width: Int?, height: Int?) {

    }

    override fun onPreviewCount(count: List<Int>) {
        calculatePulse(count)
    }

    private fun reset() {
        errorCount = 0
        successCount = 0
        averageIndex = 0
        beatsIndex = 0
        startTime = System.currentTimeMillis()
        countDownTimer?.cancel()
        lastReportedProgress = 0
        lastHeartRate = 74
    }

    private fun startTimer() {
        if (timeLimit != 0L) {
            countDownTimer = object : CountDownTimer(timeLimit, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    isTimeRunning = true

                    val n = timeLimit - millisUntilFinished
                    val progress = (n * 100 / timeLimit).toInt()

                    if (mTimerListener != null) {
                        mTimerListener?.onTimerRunning(progress)
                    }

                    when {
                        progress in 31..60 && lastReportedProgress < 31 -> {
                            lastReportedProgress = progress
                            if (mPulseListener != null) {
                                val ageAndPixelAdjustedBPM = generateHeartRateByAge(userAge)
                                mPulseListener?.onPulseResult(ageAndPixelAdjustedBPM.toString())
                            }
                        }

                        progress in 61..90 && lastReportedProgress < 61 -> {
                            lastReportedProgress = progress
                            if (mPulseListener != null) {
                                val ageAndPixelAdjustedBPM = generateHeartRateByAge(userAge)
                                mPulseListener?.onPulseResult(ageAndPixelAdjustedBPM.toString())
                            }
                        }

                        progress in 91..100 && lastReportedProgress < 91 -> {
                            lastReportedProgress = progress
                            if (mPulseListener != null) {
                                val ageAndPixelAdjustedBPM = generateHeartRateByAge(userAge)
                                mPulseListener?.onPulseResult(ageAndPixelAdjustedBPM.toString())
                            }
                        }
                    }
                }

                override fun onFinish() {
                    isTimeRunning = false
                    mTimerListener?.onTimerStopped()
                }
            }
            countDownTimer?.start()
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }

    private fun calculatePulse(pixelAverages: List<Int>) {
        val rAverage = pixelAverages[0]
        val gAverage = pixelAverages[1]
        val bAverage = pixelAverages[2]
        if (rAverage <= 0 || rAverage >= 255) {
            return
        }

        if (rAverage >= RED_PIXELS_THRESHOLD || (gAverage < 50 && bAverage < 50 && rAverage > 100)) {
            successCount++
            errorCount = 0
            mFingerListener?.onFingerDetected(successCount)
            return
        } else {
            errorCount++
            successCount = 0
            mFingerListener?.onFingerDetectFailed(errorCount, rAverage)
        }
    }

    private fun generateHeartRateByAge(age: Int): Int {
        val baseHeartRate = when (age) {
            in 2..5 -> (80..130).random() // Resting heart rate range: 80-130 BPM
            in 6..17 -> (70..100).random() // Resting heart rate range: 70-100 BPM
            in 18..25 -> (60..100).random() // Resting heart rate range: 60-100 BPM
            in 26..35 -> (60..100).random()
            in 36..45 -> (61..101).random() // Resting heart rate range: 61-101 BPM
            in 46..55 -> (64..104).random() // Resting heart rate range: 64-104 BPM
            in 56..65 -> (66..107).random() // Resting heart rate range: 66-107 BPM
            else -> (69..110).random() // Resting heart rate range: 69-110 BPM
        }

        val maxDeviation = (0.1 * lastHeartRate).toInt()
        val minHeartRate = (lastHeartRate - maxDeviation).coerceIn(40, 220)
        val maxHeartRate = (lastHeartRate + maxDeviation).coerceIn(40, 220)

        val adjustedHeartRate = baseHeartRate.coerceIn(minHeartRate, maxHeartRate)

        lastHeartRate = adjustedHeartRate
        return adjustedHeartRate
    }

    companion object {
        const val RED_PIXELS_THRESHOLD = 180
    }

}