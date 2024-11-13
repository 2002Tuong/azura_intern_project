package com.example.claptofindphone.presenter.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.claptofindphone.data.local.PreferenceSupplier

class ActiveManager(
    private val preferenceSupplier: PreferenceSupplier
) {
    private val _isActiveState: MutableLiveData<Boolean> = MutableLiveData(preferenceSupplier.isFindPhoneActivated())
    val isActiveState: LiveData<Boolean> = _isActiveState

    fun setActive(isActive: Boolean) {
        _isActiveState.postValue(isActive)
        preferenceSupplier.setFindPhoneActivated(isActive)

    }

    fun isActive() = isActiveState.value ?: false
}