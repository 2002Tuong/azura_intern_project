package com.bloodpressure.app.screen.home.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.DatabaseExporter
import com.bloodpressure.app.tracking.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingViewModel(
    private val databaseExporter: DatabaseExporter,
    private val appDataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            appDataStore.isPurchasedFlow.collectLatest { isPurchased ->
                _uiState.update { it.copy(isPurchased = isPurchased) }
            }
        }
        TrackingManager.logSettingScreenLaunchEvent()
    }

    fun rateApp() {
        _uiState.update { it.copy(shouldShowRating = true) }
    }

    fun onRateShown() {
        _uiState.update { it.copy(shouldShowRating = false) }
    }

    fun exportData(fileUri: Uri) {
        viewModelScope.launch {
            val uri = databaseExporter.exportRecordsToCsv(fileUri)
            _uiState.update { it.copy(shareUri = uri) }
        }
    }

    fun clearShareUri() {
        _uiState.update { it.copy(shareUri = null) }
    }

    data class UiState(
        val shouldShowRating: Boolean = false,
        val shareUri: Uri? = null,
        val isPurchased: Boolean = false
    )
}
