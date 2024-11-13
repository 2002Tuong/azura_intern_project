package com.bloodpressure.app.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.screen.bloodpressure.filterRecordsByDateRange
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: BpRepository,
    private val dataStore: AppDataStore,
    private val alarmingManager: AlarmingManager
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
        loadData()
        TrackingManager.logHistoryScreenLaunch()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    _uiState.update {
                        it.copy(
                            records = records.groupBy { record -> record.date },
                            filteredRecords = records.groupBy { item -> item.date }
                        )
                    }
                }
        }
        viewModelScope.launch {
            dataStore.sampleRecordIdFlow.collectLatest { id ->
                _uiState.update { it.copy(sampleRecordId = id) }
            }
        }
    }

    fun setFilteredRecords(startDateLong: Long, endDateLong: Long) {
        val filteredRecords = filterRecordsByDateRange(startDateLong, endDateLong, uiState.value.records)
        _uiState.update {
            it.copy(
                filteredRecords = filteredRecords,
            )
        }
    }

    fun showSetReminder(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    data class UiState(
        val isPurchased: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val records: Map<String, List<Record>>? = null,
        val filteredRecords: Map<String, List<Record>> = emptyMap(),
        val sampleRecordId: Long = 0L
    )
}
