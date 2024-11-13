package com.bloodpressure.app.screen.home.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.tracking.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val dataStore: AppDataStore,
    private val heartRateRepository: HeartRateRepository,
    private val bpRepository: BpRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            heartRateRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    if (records.isNotEmpty()) {
                        _uiState.update { it.copy(lastHeartRateRecord = records.first()) }
                    }

                }
        }

        viewModelScope.launch {
            bpRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    if (records.isNotEmpty()) {
                        _uiState.update { it.copy(lastBpRecord = records.first()) }
                    }
                }
        }

        TrackingManager.logTrackerScreenLaunchEvent()
    }


    data class UiState(
        val isPurchased: Boolean = false,
        val trackerList: List<TrackerType> = listOf(TrackerType.BLOOD_PRESSURE, TrackerType.HEART_RATE, TrackerType.BLOOD_SUGAR),
        val lastHeartRateRecord: HeartRateRecord? = null,
        val lastBpRecord: Record? = null
    )
}
