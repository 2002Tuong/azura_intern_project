package com.bloodpressure.app.screen.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            alarmingManager.alarmRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    _uiState.update { it.copy(allRecords = records) }
                }
        }
    }

    fun showBottomSheet(shown: Boolean) {
        _uiState.update { it.copy(showBottomSheet = shown) }
    }

    fun setSelectedAlarmType(alarmType: AlarmType) {
        _uiState.update { it.copy(selectedAlarmType = alarmType) }
    }

    fun clearSelectedAlarmType() {
        _uiState.update { it.copy(selectedAlarmType = null, isUpdateAlarmRecord = false) }
    }

    fun showSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun showDeleteDialog(shown: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = shown) }
    }

    fun clearAlarmRecord() {
        _uiState.update { it.copy(alarmRecord = null, isUpdateAlarmRecord = false) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    fun updateRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.updateRecord(alarmRecord)
        }
    }

    fun deleteRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.deleteRecord(alarmRecord)
        }
    }

    fun setAlarmRecord(alarmRecord: AlarmRecord) {
        _uiState.update {
            it.copy(
                alarmRecord = alarmRecord,
                selectedAlarmType = alarmRecord.type,
                isUpdateAlarmRecord = true
            )
        }
    }

    data class UiState(
        val allRecords: List<AlarmRecord> = emptyList(),
        val showBottomSheet: Boolean = false,
        val selectedAlarmType: AlarmType? = null,
        val showSetAlarmDialog: Boolean = false,
        val alarmRecord: AlarmRecord? = null,
        val isUpdateAlarmRecord: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val isPurchased: Boolean = false
    )
}
