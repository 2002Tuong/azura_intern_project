package com.bloodpressure.app.screen.bloodsugar.add

import androidx.core.util.toRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.data.repository.bloodsugar.IBloodSugarRepository
import com.bloodpressure.app.screen.bloodsugar.convertToMg
import com.bloodpressure.app.screen.bloodsugar.convertToMole
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.screen.bloodsugar.type.DEFAULT_AFTER_EATING_1H
import com.bloodpressure.app.screen.bloodsugar.type.DEFAULT_AFTER_EATING_2H
import com.bloodpressure.app.screen.bloodsugar.type.TargetRange
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class BloodSugarDetailViewModel(
    private val textFormatter: TextFormatter,
    private val repository: IBloodSugarRepository,
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
        }
        getCurrentDateTime()

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
    }

    fun setTime(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun setDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun setNotes(notes: Set<String>) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun showAddSuccessDialog(shown: Boolean) {
        _uiState.update { it.copy(showAddSuccessDialog = shown) }
    }

    fun saveRecord() {
        viewModelScope.launch {
            if (uiState.value.recordId == null) {
                repository.insertRecord(
                    BloodSugarRecord(
                        bloodSugar = uiState.value.bloodSugarValue,
                        bloodSugarUnit = uiState.value.bloodSugarUnit,
                        time = uiState.value.time,
                        date = uiState.value.date,
                        bloodSugarStateType = uiState.value.stateSelected,
                        bloodSugarRateType = uiState.value.getBloodSugarRateType(),
                        targetRanges = uiState.value.targetRanges,
                        notes = uiState.value.notes,
                    )
                )
            } else {
                repository.updateRecord(
                    BloodSugarRecord(
                        bloodSugar = uiState.value.bloodSugarValue,
                        bloodSugarUnit = uiState.value.bloodSugarUnit,
                        time = uiState.value.time,
                        date = uiState.value.date,
                        bloodSugarStateType = uiState.value.stateSelected,
                        bloodSugarRateType = uiState.value.getBloodSugarRateType(),
                        targetRanges = uiState.value.targetRanges,
                        notes = uiState.value.notes,
                        rowId = uiState.value.recordId!!
                    )
                )
            }
        }
        _uiState.update { it.copy(showAddSuccessDialog = true) }
    }

    fun getRecord(recordId: Long) {
        viewModelScope.launch {
            repository.getRecordById(recordId).collectLatest { record ->
                record?.run {
                    _uiState.update {
                        it.copy(
                            recordId = recordId,
                            stateSelected = this.bloodSugarStateType,
                            bloodSugarUnit = this.bloodSugarUnit,
                            bloodSugarValue = this.bloodSugar,
                            date = this.date,
                            time = this.time,
                            notes = this.notes,
                            targetRanges = this.targetRanges
                        )
                    }
                }
            }
        }
    }

    fun setDefault() {
        _uiState.update {
            it.copy(
                bloodSugarValue = 1f,
            )
        }
    }

    fun setLastValue(value: Float?) {
        _uiState.update { it.copy(lastValue = value) }
    }

    fun updateMeasureUnit(bloodSugarUnit: BloodSugarUnit) {
        if (uiState.value.bloodSugarUnit != bloodSugarUnit) {
            if (bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE) {
                val targetRanges = uiState.value.targetRanges.map {
                    it.copy(
                        bloodSugarUnit = bloodSugarUnit,
                        isChecked = false,
                        normalRangeMin = it.normalRangeMin.convertToMole(),
                        normalRangeMax = it.normalRangeMax.convertToMole(),
                        diabetesValue = it.diabetesValue.convertToMole()
                    )
                }
                val bloodSugarValue = uiState.value.bloodSugarValue.convertToMole()
                _uiState.update {
                    it.copy(
                        bloodSugarUnit = bloodSugarUnit,
                        bloodSugarValue = bloodSugarValue,
                        targetRanges = targetRanges
                    )
                }
            } else {
                val targetRanges = uiState.value.targetRanges.map {
                    it.copy(
                        bloodSugarUnit = bloodSugarUnit,
                        isChecked = false,
                        normalRangeMin = it.normalRangeMin.convertToMg(),
                        normalRangeMax = it.normalRangeMax.convertToMg(),
                        diabetesValue = it.diabetesValue.convertToMg(),
                    ).apply {

                    }
                }
                val bloodSugarValue = uiState.value.bloodSugarValue.convertToMg()
                _uiState.update {
                    it.copy(
                        bloodSugarUnit = bloodSugarUnit,
                        bloodSugarValue = bloodSugarValue,
                        targetRanges = targetRanges
                    )
                }
            }
        }
    }

    fun resetTarget() {
        _uiState.update {
            it.copy(
                targetRanges = BloodSugarStateType.values().map { stateType ->
                    when (stateType) {
                        BloodSugarStateType.AFTER_EATING_1H -> DEFAULT_AFTER_EATING_1H
                        BloodSugarStateType.AFTER_EATING_2H -> DEFAULT_AFTER_EATING_2H
                        else -> TargetRange(stateType)
                    }
                }
            )
        }
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

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            repository.getRecordById(recordId).firstOrNull()?.let { record ->
                repository.deleteRecord(record)
            }
        }
    }

    fun confirmDelete() {
        _uiState.update { it.copy(shouldShowDeleteDialog = true) }
    }

    fun clearConfirmDelete() {
        _uiState.update { it.copy(shouldShowDeleteDialog = false) }
    }

    private fun getCurrentDateTime() {
        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)
        _uiState.update { it.copy(date = formattedDate, time = formattedTime) }
    }

    fun setBloodSugarRate(bloodSugarRate: Float) {
        _uiState.update {
            it.copy(bloodSugarValue = bloodSugarRate, lastValue = null)
        }
    }

    fun setSate(bloodSugarStateType: BloodSugarStateType) {
        _uiState.update {
            it.copy(stateSelected = bloodSugarStateType)
        }
    }

    fun setDiabetes(value: String, stateIndex: Int): Boolean {
        val diabetesValue = value.toFloatOrNull() ?: 0f
        return uiState.value.targetRanges[stateIndex].isDiabetesValueInputValid(diabetesValue)
            .also { isValid ->
                if (isValid) {
                    _uiState.update {
                        it.copy(
                            targetRanges = it.targetRanges.mapIndexed { index, targetRange ->
                                if (index == stateIndex) {
                                    targetRange.copy(diabetesValue = diabetesValue)
                                } else {
                                    targetRange
                                }
                            }
                        )
                    }
                }
            }
    }

    fun setNormalRangeMin(value: String, stateIndex: Int): Boolean {
        val normalRangeMin = value.toFloatOrNull() ?: 0f
        return uiState.value.targetRanges[stateIndex].isNormalMinInputValid(normalRangeMin)
            .also { isValid ->
                if (isValid) {
                    _uiState.update {
                        it.copy(
                            targetRanges = it.targetRanges.mapIndexed { index, targetRange ->
                                if (index == stateIndex) {
                                    targetRange.copy(normalRangeMin = normalRangeMin)
                                } else {
                                    targetRange
                                }
                            }
                        )
                    }
                }
            }
    }

    fun setNormalRangeMax(value: String, stateIndex: Int): Boolean {
        val normalRangeMax = value.toFloatOrNull() ?: 0f
        return uiState.value.targetRanges[stateIndex].isNormalMaxInputValid(normalRangeMax)
            .also { isValid ->
                if (isValid) {
                    _uiState.update {
                        it.copy(
                            targetRanges = it.targetRanges.mapIndexed { index, targetRange ->
                                if (index == stateIndex) {
                                    targetRange.copy(normalRangeMax = normalRangeMax)
                                } else {
                                    targetRange
                                }
                            }
                        )
                    }
                }
            }
    }

    fun setChecked(isChecked: Boolean, stateIndex: Int) {
        _uiState.update {
            it.copy(
                targetRanges = it.targetRanges.mapIndexed { index, targetRange ->
                    if (index == stateIndex) {
                        targetRange.copy(isChecked = isChecked)
                    } else {
                        targetRange
                    }
                }
            )
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
        val stateSelected: BloodSugarStateType = BloodSugarStateType.DEFAULT,
        val bloodSugarValue: Float = 1f,
        val bloodSugarUnit: BloodSugarUnit = BloodSugarUnit.MILLIMOLES_PER_LITRE,
        val date: String = "",
        val time: String = "",
        val notes: Set<String> = emptySet(),
        val lastValue: Float? = null,
        val showAddSuccessDialog: Boolean = false,
        val shouldShowDeleteDialog: Boolean = false,
        val didClickSave: Boolean = false,
        val targetRanges: List<TargetRange> = BloodSugarStateType.values()
            .map { bloodSugarStateType ->
                when (bloodSugarStateType) {
                    BloodSugarStateType.AFTER_EATING_1H -> DEFAULT_AFTER_EATING_1H
                    BloodSugarStateType.AFTER_EATING_2H -> DEFAULT_AFTER_EATING_2H
                    else -> TargetRange(bloodSugarStateType)
                }
            },
        val isAgeInputted: Boolean = false,
        val isGenderInputted: Boolean = false,
        val showAgeDialog: Boolean = false,
        val age: Int = 0,
        val showGenderDialog: Boolean = false,
        val forceShowGenderDialog: Boolean = false,
        val forceShowAgeDialog: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val genderType: GenderType = GenderType.OTHERS,
        val isPurchased: Boolean = false,
    ) {

        val maxRange =
            if (bloodSugarUnit != BloodSugarUnit.MILLIMOLES_PER_LITRE) {
                (18.0..630.0).toRange()
            } else {
                (1.0..35.0).toRange()
            }.run {
                (lower * 10).toInt()..(upper * 10).toInt()
            }.map {
                it / 10f
            }

        fun getBloodSugarRateType(): BloodSugarRateType {
            return (targetRanges.firstOrNull { it.bloodSugarStateType == stateSelected && it.isChecked }
                ?: targetRanges[0])
                .getBloodSugarRateType(bloodSugarValue)
        }
    }
}
