package com.bloodpressure.app.screen.heartrate.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.repository.HeartRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeartRateTrendsViewModel(
    private val repository: HeartRateRepository,
    private val dataStore: AppDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        loadData()
//        TrackingManager.logHeartRateHistoryScreenLaunch()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    _uiState.update { it.copy(records = records) }
                }
        }
    }

    fun setSelectedHeartRateRecord(record: HeartRateRecord) {
        _uiState.update { it.copy(selectedHeartRateRecord = record) }
    }

    data class UiState(
        val records: List<HeartRateRecord>? = null,
        val selectedHeartRateRecord: HeartRateRecord? = null
    )
}
