package com.example.videoart.batterychargeranimation.helper

import androidx.lifecycle.MutableLiveData

class BatteryHelper {

    val battery: MutableLiveData<Int> = MutableLiveData(0)

    fun updateBatteryLevel(batteryValue: Int) {
        battery.postValue(batteryValue)
    }
    companion object {
        var batteryLevel = 0
    }
}