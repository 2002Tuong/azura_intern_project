package com.bloodpressure.app.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.screen.updateapp.AppUpdateChecker
import com.bloodpressure.app.utils.DefaultReminderManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig,
    private val appUpdateChecker: AppUpdateChecker,
    private val notificationController: NotificationController,
    defaultReminderManager: DefaultReminderManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    private val _updateType: MutableStateFlow<AppUpdateChecker.UpdateType> = MutableStateFlow(AppUpdateChecker.UpdateType.Updated)
    val updateType: StateFlow<AppUpdateChecker.UpdateType> = _updateType

    init {
        observePurchases()
//        viewModelScope.launch {
//            dataStore.isOnboardingPremiumShownFlow.collectLatest { isShown ->
//                val shouldShowPremium = !isShown && remoteConfig.shouldShowPremiumPopup()
//                _uiState.update { it.copy(shouldShowPremium = shouldShowPremium) }
//            }
//        }

        viewModelScope.launch {
            dataStore.setLanguageSelected(true)
            dataStore.setOnboardingShown(true)
        }

        viewModelScope.launch {
            val updateType = appUpdateChecker.checkUpdate()
            if(updateType is AppUpdateChecker.UpdateType.RequireUpdate) {
                //If update version is available
                //Makes sure update dialog show after inter splash ads
                delay(300L)
                _updateType.update {
                    updateType
                }
            }
        }

//        defaultReminderManager.scheduleReminderRepeating()
    }

    private fun observePurchases() {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update {
                        it.copy(
                            isPurchased = isPurchased,
                            isAdsEnabled = !remoteConfig.offAllAds(),
                            shouldLoadAds = !isPurchased && !remoteConfig.offAllAds()
                        )
                    }
                    if (isPurchased) {
                        onPremiumShown()
                    }
                }
        }
    }

    fun onPremiumShown() {
        viewModelScope.launch {
            dataStore.setOnboardingPremiumShown()
        }
    }

    fun onAdsLoaded() {
        _uiState.update { it.copy(shouldLoadAds = false) }
    }

    fun clearUpdateType() {
        _updateType.update {
            AppUpdateChecker.UpdateType.Updated
        }
    }

    fun onPermissionGranted() {
        notificationController.showMenuNotification()
    }

    data class UiState(
        val isPurchased: Boolean = false,
        val isAdsEnabled: Boolean = false,
        val shouldShowPremium: Boolean = false,
        val shouldLoadAds: Boolean = false
    )
}
