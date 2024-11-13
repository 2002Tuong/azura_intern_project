package com.bloodpressure.app.screen.heartrate.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class AddHeartRateViewModel(
    private val handle: SavedStateHandle,
    private val textFormatter: TextFormatter,
    private val repository: HeartRateRepository,
    private val dataStore: AppDataStore,
    private val alarmingManager: AlarmingManager,
    private val remoteConfig: RemoteConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    private val recordId: Long?
        get() = handle.get<Long>("id")

    init {
        observePurchases()
        if (recordId != null) {
            loadCurrentRecord(recordId!!)
        } else {
            getCurrentDateTime()
        }

        viewModelScope.launch {
            dataStore.increaseAddHeartRateRecordClickCounter()
        }

        viewModelScope.launch {
            dataStore.age
                .distinctUntilChanged()
                .collectLatest { age ->

                    if (age <= 0) {
                        _uiState.update { it.copy(forceShowAgeDialog = true, age = 22, isAgeInputted = false) }
                    } else {
                        _uiState.update { it.copy(age = age, isAgeInputted = true) }
                    }

                }

        }

        viewModelScope.launch {
            dataStore.gender
                .distinctUntilChanged()
                .collectLatest { gender ->
                    _uiState.update { it.copy(genderType = gender) }
                }
        }

        TrackingManager.logAddHeartRateRecordScreenLaunchEvent()
    }

    private fun loadCurrentRecord(id: Long) {
        viewModelScope.launch {
            repository.getRecordById(id).collectLatest { record ->
                if (record != null) {
                    _uiState.update {
                        it.copy(
                            recordId = record.createdAt,
                            heartRate = record.heartRate,
                            date = record.date,
                            time = record.time,
                            notes = record.notes,
                            age = record.age,
                            genderType = record.genderType
                        )
                    }
                }
            }
        }
    }

    fun setTime(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun setDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }


    fun setHeartRate(heartRate: Int) {
        val heartRateType = getSelectedHeartRateType(heartRate)
        _uiState.update { it.copy(heartRate = heartRate, selectedHeartRateType = heartRateType) }
    }

    fun setNotes(notes: Set<String>) {
        _uiState.update { it.copy(notes = notes) }
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

    fun saveRecord() {

        val heartRateRecord = HeartRateRecord(
            heartRate = uiState.value.heartRate,
            time = uiState.value.time,
            date = uiState.value.date,
            type = uiState.value.selectedHeartRateType,
            typeName = textFormatter.getBpTypeName(uiState.value.selectedHeartRateType.nameRes),
            notes = uiState.value.notes,
            age = uiState.value.age,
            genderType = uiState.value.genderType,
            createdAt = recordId ?: System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(heartRateRecord = heartRateRecord, didClickSave = true)
        }
    }

    fun showAgeDialog(shown: Boolean) {
        _uiState.update { it.copy(showAgeDialog = shown) }
    }

    fun forceShowAgeDialog(shown: Boolean) {
        _uiState.update { it.copy(forceShowAgeDialog = shown) }
    }

    fun showGenderDialog(shown: Boolean) {
        _uiState.update { it.copy(showGenderDialog = shown) }
    }

    fun forceShowGenderDialog(shown: Boolean) {
        _uiState.update { it.copy(forceShowGenderDialog = shown) }
    }

    fun setAge(age: Int) {

        viewModelScope.launch {
            dataStore.setAge(age)
            _uiState.update { it.copy(age = age) }
        }
    }

    fun setGender(gender: GenderType) {
        viewModelScope.launch {
            dataStore.setGender(gender)
            _uiState.update { it.copy(genderType = gender) }
        }
    }

    fun showAddSuccessDialog(shown: Boolean) {
        _uiState.update { it.copy(showAddSuccessDialog = shown) }
    }

    private fun getSelectedHeartRateType(heartRate: Int): HeartRateType {
        HeartRateType.values().forEach {
            if (it.isValid(heartRate)) {
                return it
            }
        }
        return HeartRateType.NORMAL
    }

    private fun getCurrentDateTime() {
        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)
        _uiState.update { it.copy(date = formattedDate, time = formattedTime) }
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

    fun showSetReminder(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    data class UiState(
        val recordId: Long? = null,
        val heartRate: Int = 0,
        val selectedHeartRateType: HeartRateType = HeartRateType.NORMAL,
        val date: String = "",
        val time: String = "",
        val shouldNavigateUp: Boolean = false,
        val shouldShowDeleteDialog: Boolean = false,
        val notes: Set<String> = emptySet(),
        val isPurchased: Boolean = false,
        val isAgeInputted: Boolean = false,
        val isGenderInputted: Boolean = false,
        val showAgeDialog: Boolean = false,
        val age: Int = 0,
        val showGenderDialog: Boolean = false,
        val forceShowAgeDialog: Boolean = false,
        val genderType: GenderType = GenderType.OTHERS,
        val showAddSuccessDialog: Boolean = false,
        val didClickSave: Boolean = false,
        val heartRateRecord: HeartRateRecord? = null,
        val forceShowGenderDialog: Boolean = false,
        val showAds: Boolean = true,
        val isAdsEnabled: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
    )
}