package com.bloodpressure.app.screen.heartrate.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeartRateStatisticsViewModel(
    private val repository: HeartRateRepository,
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
                    _uiState.update { it.copy(allRecords = records, filteredRecords = records) }
                    calculateStatistics()
                }
        }
    }

    fun setFilteredHeartRateRecords(records: List<HeartRateRecord>) {
        _uiState.update { it.copy(filteredRecords = records) }

        calculateStatistics()
    }

    private fun calculateStatistics() {

        val records = _uiState.value.filteredRecords

        val heartRates = records.map { it.heartRate }
        val averageHeartRate = heartRates.average().toInt()
        val minHeartRate = heartRates.minOrNull() ?: 0
        val maxHeartRate = heartRates.maxOrNull() ?: 0

        val slowRecordsCount = records.count { it.type == HeartRateType.SLOW }
        val normalRecordsCount = records.count { it.type == HeartRateType.NORMAL }
        val fastRecordsCount = records.count { it.type == HeartRateType.FAST }

        _uiState.update {
            it.copy(
                averageHeartRate = averageHeartRate,
                minHeartRate = minHeartRate,
                maxHeartRate = maxHeartRate,
                slowRecordsCount = slowRecordsCount,
                normalRecordsCount = normalRecordsCount,
                fastRecordsCount = fastRecordsCount
            )
        }
    }

    data class UiState(
        val allRecords: List<HeartRateRecord> = emptyList(),
        val filteredRecords: List<HeartRateRecord> = emptyList(),
        val averageHeartRate: Int = 0,
        val minHeartRate: Int = 0,
        val maxHeartRate: Int = 0,
        val slowRecordsCount: Int = 0,
        val normalRecordsCount: Int = 0,
        val fastRecordsCount: Int = 0,
    )
}
