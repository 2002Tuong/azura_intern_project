package com.bloodpressure.app.screen.home.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.screen.language.LanguageProvider
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoDetailViewModel(
    private val handle: SavedStateHandle,
    infoFactory: InfoFactory,
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore,
    private val languageProvider: LanguageProvider,
) : ViewModel() {

    private val infoItemType: InfoItemType
        get() = handle.get<InfoItemType>("feature")!!

    private val _uiState =
        MutableStateFlow(UiState(item = infoFactory.createItem(id, infoItemType)))
    val uiState: StateFlow<UiState> get() = _uiState

    private val id: String
        get() = handle.get<String>("id")!!

    init {
        viewModelScope.launch {
            dataStore.increaseInfoItemClickCounter()
        }

        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    val isAdsEnabled = !isPurchased && !remoteConfig.offAllAds()
                    _uiState.update { it.copy(isAdsEnabled = isAdsEnabled) }
                }
        }
        TrackingManager.logInfoDetailScreenLaunchEvent()
    }

    data class UiState(
        val item: InfoItemData,
        val infoDetail: String? = null,
        val isAdsEnabled: Boolean = false
    )
}
