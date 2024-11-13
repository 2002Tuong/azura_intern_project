package com.example.videoart.batterychargeranimation.ui.premission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionViewModel : ViewModel() {

    private val _permissionReadStorage: MutableLiveData<Boolean> = MutableLiveData()
    val permissionReadStorage: LiveData<Boolean> = _permissionReadStorage

    private val _permissionDisplayOverOtherApps: MutableLiveData<Boolean> = MutableLiveData()
    val permissionDisplayOverOtherApps: LiveData<Boolean> = _permissionDisplayOverOtherApps

    fun setReadStorage(isGranted: Boolean) {
        _permissionReadStorage.postValue(isGranted)
    }

    fun setDisplayOver(isGranted: Boolean) {
        _permissionDisplayOverOtherApps.postValue(isGranted)
    }
}