package com.example.videoart.batterychargeranimation.ui.info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BatteryInfoViewModel : ViewModel() {
    val batteryCapacity: MutableLiveData<Int> = MutableLiveData()
}