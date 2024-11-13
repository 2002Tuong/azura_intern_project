package com.bloodpressure.app.screen.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.screen.heartrate.add.GenderType
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

class AddRecordViewModel(
    private val handle: SavedStateHandle,
    private val textFormatter: TextFormatter,
    private val repository: BpRepository,
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    private val recordId: Long?
        get() = handle.get<Long>("id")

    init {
        if (recordId != null) {
            loadCurrentRecord(recordId!!)
        } else {
            getCurrentDateTime()
        }

        viewModelScope.launch {
            dataStore.increaseAddRecordClickCounter()
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
        TrackingManager.logAddRecordScreenLaunchEvent()
    }

    private fun loadCurrentRecord(id: Long) {
        viewModelScope.launch {
            repository.getRecordById(id).collectLatest { record ->
                if (record != null) {
                    _uiState.update {
                        it.copy(
                            recordId = record.createdAt,
                            systolic = record.systolic,
                            diastolic = record.diastolic,
                            pulse = record.pulse,
                            selectedBpType = record.type,
                            date = record.date,
                            time = record.time,
                            notes = record.notes
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


    fun setSystolic(systolic: Int) {
        val bpType = getSelectedBpType(systolic, uiState.value.diastolic)
        _uiState.update { it.copy(systolic = systolic, selectedBpType = bpType) }
    }

    fun setDiastolic(diastolic: Int) {
        val bpType = getSelectedBpType(uiState.value.systolic, diastolic)
        _uiState.update { it.copy(diastolic = diastolic, selectedBpType = bpType) }
    }

    fun setPulse(pulse: Int) {
        _uiState.update { it.copy(pulse = pulse) }
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

    fun showSetReminder(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    fun save() {
        viewModelScope.launch {
            val record = Record(
                systolic = uiState.value.systolic,
                diastolic = uiState.value.diastolic,
                pulse = uiState.value.pulse,
                time = uiState.value.time,
                date = uiState.value.date,
                type = uiState.value.selectedBpType,
                typeName = textFormatter.getBpTypeName(uiState.value.selectedBpType.nameRes),
                notes = uiState.value.notes,
                createdAt = recordId ?: System.currentTimeMillis()
            )

            if (recordId != null) {
                repository.updateRecord(record)
            } else {
                repository.insertRecord(record)
                removeSampleRecordIfNeeded()
            }
            _uiState.update {
                it.copy(shouldNavigateUp = true)
            }
        }
    }

    private fun removeSampleRecordIfNeeded() {
        viewModelScope.launch {
            if (dataStore.sampleRecordId != 0L) {
                val sampleRecord = repository.getRecordById(dataStore.sampleRecordId).firstOrNull()
                if (sampleRecord != null) {
                    repository.deleteRecord(sampleRecord)
                }
                dataStore.setSampleRecordId(0L)
            }
        }
    }

    private fun getSelectedBpType(systolic: Int, diastolic: Int): BpType {
        BpType.values().forEach {
            if (it.isValid(systolic, diastolic)) {
                return it
            }
        }
        return BpType.NORMAL
    }

    private fun getCurrentDateTime() {
        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)
        _uiState.update { it.copy(date = formattedDate, time = formattedTime) }
    }

    data class UiState(
        val recordId: Long? = null,
        val systolic: Int = 100,
        val diastolic: Int = 78,
        val pulse: Int = 80,
        val selectedBpType: BpType = BpType.NORMAL,
        val date: String = "",
        val time: String = "",
        val shouldNavigateUp: Boolean = false,
        val shouldShowDeleteDialog: Boolean = false,
        val notes: Set<String> = emptySet(),
        val isPurchased: Boolean = false,
        val showAds: Boolean = false,
        val isAgeInputted: Boolean = false,
        val isGenderInputted: Boolean = false,
        val showAgeDialog: Boolean = false,
        val age: Int = 0,
        val showGenderDialog: Boolean = false,
        val forceShowGenderDialog: Boolean = false,
        val forceShowAgeDialog: Boolean = false,
        val genderType: GenderType = GenderType.OTHERS,
        val showSetAlarmDialog: Boolean = false,
    )
}
