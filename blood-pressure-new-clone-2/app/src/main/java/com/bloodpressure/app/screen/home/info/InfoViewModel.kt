package com.bloodpressure.app.screen.home.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.tracking.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoViewModel(
    private val handle: SavedStateHandle,
    private val infoFactory: InfoFactory,
    private val dataStore: AppDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    private val infoItemType: InfoItemType
        get() = handle.get<InfoItemType>("feature")!!

    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }
        _uiState.update { it.copy(items = infoFactory.getInfos(infoItemType)) }
        TrackingManager.logInfoScreenLaunchEvent()
    }

    data class UiState(
        val items: List<InfoItemData> = emptyList(),
        val isPurchased: Boolean = false
    )
}
