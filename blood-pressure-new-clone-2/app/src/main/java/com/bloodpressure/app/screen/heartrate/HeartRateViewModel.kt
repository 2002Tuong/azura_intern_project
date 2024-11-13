package com.bloodpressure.app.screen.heartrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.HeartRateOptionType
import com.bloodpressure.app.data.repository.AlarmRepository
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeartRateViewModel(
    private val heartRateRepository: HeartRateRepository,
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore
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
                    _uiState.update { it.copy(isHistoryAvailable = records.isNotEmpty()) }
                }
        }
        viewModelScope.launch {
            alarmingManager.alarmRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->

                    val hasHeartRateAlarm = records.any { it.type == AlarmType.HEART_RATE }
                    _uiState.update { it.copy(hasHeartRateAlarm = hasHeartRateAlarm) }
                }
        }
    }

    fun showSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun showAskSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showAskSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    data class UiState(
        val heartRateOptionList: List<HeartRateOptionType> = listOf(
            HeartRateOptionType.MEASURE_NOW,
            HeartRateOptionType.ADD_MANUALLY
        ),
        val isHistoryAvailable: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val showAskSetAlarmDialog: Boolean = false,
        val hasHeartRateAlarm: Boolean = false,
        val isPurchased: Boolean = false,
    )

}