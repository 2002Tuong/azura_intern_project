package com.example.videoart.batterychargeranimation.ui.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HelpViewModel : ViewModel() {
    private val _isIgnoreBatteryOptimization: MutableLiveData<Boolean> = MutableLiveData()
    val isIgnoreBatteryOptimization: LiveData<Boolean> = _isIgnoreBatteryOptimization

    private val _permissionDisplayOverOtherApps: MutableLiveData<Boolean> = MutableLiveData()
    val permissionDisplayOverOtherApps: LiveData<Boolean> = _permissionDisplayOverOtherApps

    fun setIsIgnoreBatteryOptimization(isGranted: Boolean) {
        _isIgnoreBatteryOptimization.postValue(isGranted)
    }

    fun setDisplayOver(isGranted: Boolean) {
        _permissionDisplayOverOtherApps.postValue(isGranted)
    }
}