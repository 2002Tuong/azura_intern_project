package com.example.claptofindphone.presenter.result

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.claptofindphone.presenter.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultScreenViewModel @Inject constructor(
    private val activeManager: ActiveManager
) : BaseViewModel() {

    private val _isStopCountState: MutableLiveData<Boolean> = MutableLiveData(false)
    val isStopCountState: LiveData<Boolean> = _isStopCountState
    private val _curTimeState: MutableLiveData<Long> = MutableLiveData(0)
    val curTimeState: LiveData<Long> = _curTimeState

    fun startCountDown(timeInMills: Long) {
        _curTimeState.value = convertToSec(timeInMills)
        val countDownTimer = object :CountDownTimer(timeInMills, 1000L) {
            override fun onTick(p0: Long) {
                val convertValue = convertToSec(p0)
                _curTimeState.value = convertValue
            }

            override fun onFinish() {
                setStopCount(true)
            }
        }
        countDownTimer.start()
    }

    fun setActive(isActive: Boolean) {
        activeManager.setActive(isActive)
    }

    fun setStopCount(stopCount: Boolean) {
        _isStopCountState.value = stopCount
    }

    private fun convertToSec(timeInmilis: Long) : Long {
        return timeInmilis / 1000
    }
}