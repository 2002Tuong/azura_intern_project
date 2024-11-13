package com.screentheme.app.utils.helpers

import android.os.CountDownTimer

class CountTimeHelper {
    var reloadBannerTimer : CountDownTimer? = null
    var categoryInterTimer: CountDownTimer? = null

    fun createReloadBannerTimer(
        timeInFuture: Long,
        countDownInterval: Long,
        action: () -> Unit) {
        reloadBannerTimer?.cancel()
        reloadBannerTimer = ReloadBannerTimer(timeInFuture, countDownInterval, action)
    }

    fun startReloadBannerTimer() {
        reloadBannerTimer?.start()
    }

    fun stopReloadBannerTimer() {
        reloadBannerTimer?.cancel()
    }

    fun createCategoryInterTime(
        timeInFuture: Long,
        countDownInterval: Long,
        action: () -> Unit
    ) {
        categoryInterTimer?.cancel()
        categoryInterTimer = CategoryInterTimer(timeInFuture, countDownInterval, action)
    }

    fun startCategoryInterTime() {
        categoryInterTimer?.start()
    }

    fun stopCategoryInterTime() {
        categoryInterTimer?.cancel()
    }

    fun cancelAll() {
        stopReloadBannerTimer()
        stopCategoryInterTime()
        reloadBannerTimer = null
        categoryInterTimer = null
    }

    companion object {
        const val RELOAD_HOME_BANNER = 30000L
        const val NEXT_TIME_INTER_CATEGORY = 30000L
        const val INTERVAL_TIME = 1000L

    }
}

class ReloadBannerTimer(
    timeInFuture: Long,
    countDownInterval: Long,
    val action: () -> Unit = {}
) : CountDownTimer(timeInFuture, countDownInterval) {
    override fun onTick(millisUntilFinished: Long) {
    }

    override fun onFinish() {
        action.invoke()
        this.start()
    }
}

class CategoryInterTimer(
    timeInFuture: Long,
    countDownInterval: Long,
    val action: () -> Unit = {}
) : CountDownTimer(timeInFuture, countDownInterval){
    override fun onTick(millisUntilFinished: Long) {
    }

    override fun onFinish() {
        action.invoke()
        //this.start()
    }
}