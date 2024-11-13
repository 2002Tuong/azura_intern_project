package com.artgen.app.ui.screen.main


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.ui.screen.updateapp.AppUpdateChecker
import com.artgen.app.utils.NotificationController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StylePickerViewModel(
    remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore,
    private val appUpdateChecker: AppUpdateChecker,
    private val notificationController: NotificationController
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState
    private val _updateType: MutableStateFlow<AppUpdateChecker.UpdateType> = MutableStateFlow(AppUpdateChecker.UpdateType.Updated)
    val updateType: StateFlow<AppUpdateChecker.UpdateType> = _updateType
    init {
        val artStyleList = remoteConfig.artStyles
        _uiState.update { it.copy(artStyleList = artStyleList) }

        viewModelScope.launch {
            dataStore.setLanguageSelected(true)
            dataStore.setOnboardingShown(true)
        }

        viewModelScope.launch {
            val updateType = appUpdateChecker.checkUpdate()
            if(updateType is AppUpdateChecker.UpdateType.RequireUpdate) {
                //Makes sure update dialog show after ads
                delay(300L)
                _updateType.update{updateType}
            }
        }

        viewModelScope.launch {
            dataStore.isPurchasedFlow.collectLatest {purchase ->
                _uiState.update {
                    it.copy(showPremiumAction = !purchase)
                }
            }
        }
    }


    fun showRating() {
        _uiState.update {
            it.copy(isShowingRatingDialog = true)
        }
    }

    fun hideRating() {
        _uiState.update {
            it.copy(isShowingRatingDialog = false)
        }
    }

    fun setSelectedArtStyle(artStyle: ArtStyle) {
        _uiState.update { it.copy(selectedArtStyle = artStyle) }
        viewModelScope.launch {
            dataStore.setSelectedArtStyle(artStyle.styleId)
        }
    }

    fun clearUpdateType() {
        _updateType.update{AppUpdateChecker.UpdateType.Updated}
    }

    fun showMenuNotification() {
        notificationController.showMenuNotification()
    }

    fun shouldShowSubscriptionPremiumDialog(shouldShow: Boolean) {
        _uiState.update {
            it.copy(showSubscriptionPremiumDialog = shouldShow)
        }
    }

    data class UiState(
        val artStyleList: List<ArtStyle> = emptyList(),
        val selectedArtStyle: ArtStyle? = null,
        val showPremiumAction: Boolean = true,
        val showSubscriptionPremiumDialog: Boolean = false,
        val isShowingRatingDialog: Boolean = false
    )
}
