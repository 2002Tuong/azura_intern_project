package com.bloodpressure.app.screen.heartrate.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeartRateResultViewModel(
    private val handle: SavedStateHandle,
    private val textFormatter: TextFormatter,
    private val repository: HeartRateRepository,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    private val recordId: Long?
        get() = handle.get<Long>("id")

    init {

        if (recordId != null) {
            loadCurrentRecord(recordId!!)
        }

        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

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
                    _uiState.update { it.copy(records = records) }
                }
        }
    }

    private fun loadCurrentRecord(id: Long) {
        viewModelScope.launch {
            repository.getRecordById(id).collectLatest { record ->
                if (record != null) {
                    val heartRateType = getSelectedHeartRateType(record.heartRate)

                    _uiState.update {
                        it.copy(recordId = record.createdAt, selectedHeartRateType = heartRateType)
                    }

                    setHeartRateRecord(record)
                }
            }
        }
    }


    fun setHeartRateRecord(heartRateRecord: HeartRateRecord) {

        val heartRateType = getSelectedHeartRateType(heartRateRecord.heartRate)

        _uiState.update { it.copy(heartRateRecord = heartRateRecord, selectedHeartRateType = heartRateType) }
    }

    fun confirmDelete() {
        _uiState.update { it.copy(shouldShowDeleteDialog = true) }
    }

    fun clearConfirmDelete() {
        _uiState.update { it.copy(shouldShowDeleteDialog = false) }
    }

    fun deleteRecord() {
        viewModelScope.launch {
            recordId?.let { id ->
                repository.getRecordById(id).firstOrNull()?.let { record ->
                    repository.deleteRecord(record)
                }
                _uiState.update { it.copy(shouldNavigateUp = true) }
            }
        }
    }

    fun save() {

        viewModelScope.launch {

            val heartRateRecord = uiState.value.heartRateRecord

            if (heartRateRecord != null) {

                if (recordId != null) {
                    repository.updateRecord(heartRateRecord)
                } else {
                    repository.insertRecord(heartRateRecord)
                }
            }

            _uiState.update {
                it.copy(shouldNavigateUp = true)
            }
        }
    }

    private fun getSelectedHeartRateType(heartRate: Int): HeartRateType {
        HeartRateType.values().forEach {
            if (it.isValid(heartRate)) {
                return it
            }
        }
        return HeartRateType.NORMAL
    }

    data class UiState(
        val recordId: Long? = null,
        val heartRateRecord: HeartRateRecord? = null,
        val shouldNavigateUp: Boolean = false,
        val isPurchased: Boolean = false,
        val shouldShowDeleteDialog: Boolean = false,
        val selectedHeartRateType: HeartRateType = HeartRateType.NORMAL,
        val records: List<HeartRateRecord> = emptyList(),
        val isAdsEnabled: Boolean = false,
    )
}
