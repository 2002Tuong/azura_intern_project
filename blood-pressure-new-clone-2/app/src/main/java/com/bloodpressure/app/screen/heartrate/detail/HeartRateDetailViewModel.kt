package com.bloodpressure.app.screen.heartrate.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.DatabaseExporter
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeartRateDetailViewModel(
    private val heartRateRepository: HeartRateRepository,
    private val databaseExporter: DatabaseExporter,
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        loadData()
        observePurchases()
    }
    private fun observePurchases() {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update {
                        it.copy(
                            isPurchased = isPurchased,
                            isAdsEnabled = !isPurchased && !remoteConfig.offAllAds(),
                        )
                    }
                }
        }
    }


    private fun loadData() {
        viewModelScope.launch {
            heartRateRepository.getAllRecords()
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

    fun exportData(fileUri: Uri) {
        viewModelScope.launch {
            val uri = databaseExporter.exportHeartRAteRecordsToCsv(fileUri)
            _uiState.update { it.copy(shareUri = uri) }
        }
    }

    fun clearShareUri() {
        _uiState.update { it.copy(shareUri = null) }
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
        val shareUri: Uri? = null,
        val hasHeartRateAlarm: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val showAskSetAlarmDialog: Boolean = false,
        val isAdsEnabled: Boolean = false,
        val isPurchased: Boolean = false,
    )
}
