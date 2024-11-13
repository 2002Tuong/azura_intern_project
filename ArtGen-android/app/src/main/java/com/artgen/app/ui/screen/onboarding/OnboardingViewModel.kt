package com.artgen.app.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.tracking.TrackingManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class OnboardingViewModel(
    private val dataStore: AppDataStore
) : ViewModel() {

    val isPurchased: StateFlow<Boolean> = flow {
        emit(dataStore.isPurchased)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        TrackingManager.logOnboardingLaunchEvent()
    }
}
