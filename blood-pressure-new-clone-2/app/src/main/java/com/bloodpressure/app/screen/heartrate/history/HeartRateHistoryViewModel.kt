package com.bloodpressure.app.screen.heartrate.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeartRateHistoryViewModel(
    private val repository: HeartRateRepository,
    private val dataStore: AppDataStore,
    private val alarmingManager: AlarmingManager,
    private val remoteConfig: RemoteConfig
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        loadData()
        TrackingManager.logHeartRateHistoryScreenLaunch()
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
            repository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    _uiState.update { it.copy(allRecords = records, filteredRecords = records) }
                }
        }
    }

    fun setFilteredHeartRateRecords(records: List<HeartRateRecord>) {
        _uiState.update { it.copy(filteredRecords = records) }
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
        val allRecords: List<HeartRateRecord> = emptyList(),
        val filteredRecords: List<HeartRateRecord> = emptyList(),
        val isAdsEnabled: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val isPurchased: Boolean = false,
    )
}
