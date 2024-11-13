package com.bloodpressure.app.screen.waterreminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.repository.WaterReminderRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WaterAlarmViewModel(
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore,
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
            alarmingManager.alarmRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    val waterRecords = records.filter { it.type == AlarmType.WATER_REMINDER }
                    val repoRecords = waterRecords.map { alarmRecord ->
                        WaterAlarmRecord(
                            reminderTime = alarmRecord.reminderTime ?: ReminderTime.AFTER_WAKE_UP,
                            alarmRecord = alarmRecord
                        )
                    }
                    val res2 = linkedMapOf<ReminderTime, WaterAlarmRecord>()
                    initListRecord().forEach {
                        res2[it.reminderTime] = it.copy(isChecked = false)
                    }
                    repoRecords.forEach {
                        res2[it.reminderTime] = it.copy(isChecked = true)
                    }
                    val res = res2.entries.map { it.value }
                    _uiState.update { it.copy(listRecords = res) }
                }
        }
    }


    fun updateListRecord(
        alarmRecord: AlarmRecord,
        reminderTime: ReminderTime
    ) {
        _uiState.update {
            it.copy(
                listRecords = uiState.value.listRecords.map { waterRecord ->
                    if (waterRecord.reminderTime == reminderTime) waterRecord.copy(alarmRecord = alarmRecord)
                    else waterRecord
                }
            )
        }
    }

    fun showSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun updateCheckedItem(isChecked: Boolean, item: WaterAlarmRecord) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    listRecords = uiState.value.listRecords.map { record ->
                        if (record == item) {
                            record.copy(isChecked = isChecked)
                        }
                        else {
                            record
                        }
                    }
                )
            }
        }

    }

    fun insertRecords() {
        viewModelScope.launch {
            uiState.value.listRecords
                .forEach {
                    if (!it.isChecked) alarmingManager.deleteRecord(it.alarmRecord)
                    else alarmingManager.insertRecord(it.alarmRecord.copy(createdAt = System.currentTimeMillis()))
                }
        }
    }

    data class UiState(
        val showSetAlarmDialog: Boolean = false,
        val listRecords: List<WaterAlarmRecord> = initListRecord(),
        val isPurchased: Boolean = false,
    )
}