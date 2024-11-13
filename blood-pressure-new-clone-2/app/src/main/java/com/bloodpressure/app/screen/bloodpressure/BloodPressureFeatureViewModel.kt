package com.bloodpressure.app.screen.bloodpressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BloodPressureOption
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BloodPressureFeatureViewModel(
    private val repository: BpRepository,
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore
) : ViewModel() {

    private var _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        _uiState.update {
            it.copy(
                features = BloodPressureOption.values().filter { type -> type.isShow }.toList()
            )
        }

        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            repository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    _uiState.update {
                        it.copy(isHistoryAvailable = records.isNotEmpty())
                    }
                }
        }

        viewModelScope.launch {
            alarmingManager.alarmRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->

                    val hasBloodPressureAlarm = records.any { it.type == AlarmType.BLOOD_PRESSURE }
                    _uiState.update { it.copy(hasBloodPressureAlarm = hasBloodPressureAlarm) }
                }
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

    fun showAskSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showAskSetAlarmDialog = shown) }
    }

    data class UiState(
        val isPurchased: Boolean = false,
        val features: List<BloodPressureOption> = listOf(),
        val isHistoryAvailable: Boolean = false,
        val showExportedData: Boolean = true,
        val hasBloodPressureAlarm: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val showAskSetAlarmDialog: Boolean = false
    )
}