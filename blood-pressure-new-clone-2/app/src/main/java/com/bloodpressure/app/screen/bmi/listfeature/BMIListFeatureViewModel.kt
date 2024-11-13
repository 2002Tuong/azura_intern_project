package com.bloodpressure.app.screen.bmi.listfeature

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.DatabaseExporter
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BMIOptionType
import com.bloodpressure.app.data.repository.BMIRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BMIListFeatureViewModel(
    private val repository: BMIRepository,
    private val databaseExporter: DatabaseExporter,
    private val alarmManager: AlarmingManager,
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
                features = BMIOptionType.values().filter {type-> type.isShow }.toList()
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
            alarmManager.alarmRepository.getAllRecords().collectLatest { alarmRecords ->
                val hasRecords = alarmRecords.any { it.type == AlarmType.WEIGHT_BMI }
                _uiState.update {
                    it.copy(
                        hasBmiAlarm = hasRecords
                    )
                }

            }
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            databaseExporter.exportBmiRecordsToCsv(uri)
            _uiState.update { it.copy(shareUri = uri) }
        }
    }

    fun clearShareUri() {
        _uiState.update {
            it.copy(shareUri = null)
        }
    }

    fun showSetAlarmDialog(show: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = show) }
    }

    fun showAskSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showAskSetAlarmDialog = shown) }
    }

    fun insertAlarm(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmManager.insertRecord(alarmRecord)
        }
    }

    data class UiState(
        val isPurchased: Boolean = false,
        val features: List<BMIOptionType> = listOf(),
        val isHistoryAvailable: Boolean = false,
        val showExportedData: Boolean = false,
        val shareUri: Uri? = null,
        val showSetAlarmDialog: Boolean = false,
        val showAskSetAlarmDialog: Boolean = false,
        val hasBmiAlarm: Boolean = false
    )
}