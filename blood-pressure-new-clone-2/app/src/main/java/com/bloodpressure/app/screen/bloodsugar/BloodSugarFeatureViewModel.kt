package com.bloodpressure.app.screen.bloodsugar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BloodSugarOption
import com.bloodpressure.app.data.repository.bloodsugar.IBloodSugarRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BloodSugarFeatureViewModel(
    private val repository: IBloodSugarRepository,
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore
) : ViewModel() {

    private var _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }
        _uiState.update {
            it.copy(
                features = BloodSugarOption.values().filter { type -> type.isShow }.toList()
            )
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

                    val hasBloodSugarAlarm = records.any { it.type == AlarmType.BLOOD_SUGAR }
                    _uiState.update { it.copy(hasBloodSugarAlarm = hasBloodSugarAlarm) }
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
        val features: List<BloodSugarOption> = listOf(),
        val isHistoryAvailable: Boolean = false,
        val showExportedData: Boolean = true,
        val hasBloodSugarAlarm: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val showAskSetAlarmDialog: Boolean = false,
        val isPurchased: Boolean = false,
    )
}