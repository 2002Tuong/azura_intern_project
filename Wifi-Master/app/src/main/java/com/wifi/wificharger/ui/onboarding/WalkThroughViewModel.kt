package com.wifi.wificharger.ui.onboarding

import androidx.lifecycle.viewModelScope
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class WalkThroughViewModel(dataStore: AppDataStore): BaseViewModel(dataStore) {

    fun setOnBoardingShow() {
        viewModelScope.launch {
            dataStore.setOnboardingShown(true)
        }
    }
}