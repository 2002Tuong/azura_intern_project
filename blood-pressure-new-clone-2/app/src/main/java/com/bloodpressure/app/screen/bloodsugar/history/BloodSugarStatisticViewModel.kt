package com.bloodpressure.app.screen.bloodsugar.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.data.repository.bloodsugar.IBloodSugarRepository
import com.bloodpressure.app.screen.bloodsugar.convertToMg
import com.bloodpressure.app.screen.bloodsugar.convertToMole
import com.bloodpressure.app.screen.bloodsugar.filterRecordsByDateRange
import com.bloodpressure.app.screen.bloodsugar.type.ALL_TYPE
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.screen.bloodsugar.type.StateType
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.BloodSugarUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BloodSugarStatisticViewModel(
    val repository: IBloodSugarRepository,
    private val alarmingManager: AlarmingManager,
    private val dataStore: AppDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        loadData()
    }

    private var dateFilter: Pair<Long, Long>? = null
    private var unitFilter: StateType? = null

    private fun loadData() {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(bloodSugarUnit = dataStore.bloodSugarUnit) }
        }
        viewModelScope.launch {
            repository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->
                    _uiState.update { it.copy(allRecords = records, filteredRecords = records) }
                    calculateStatistics(uiState.value.bloodSugarUnit)
                }
        }
    }

    fun updateDateFilter(startDate: Long, endDate: Long) {
        dateFilter = startDate to endDate
    }

    fun updateUnitFilter(stateType: StateType) {
        unitFilter = stateType
    }

    fun setBloodSugarUnit(bloodSugarUnit: BloodSugarUnit) {
        viewModelScope.launch {
            dataStore.setBloodSugarUnit(bloodSugarUnit)
        }
    }

    fun setFilteredBloodSugarRecords(bloodSugarUnit: BloodSugarUnit) {
        val filteredByDateRecords = (dateFilter?.let { (startDate, endDate) ->
                uiState.value.allRecords.filterRecordsByDateRange(
                    startDateLong = startDate,
                    endDateLong = endDate
                )
        } ?: uiState.value.allRecords)
        val filteredRecords = unitFilter?.let {
            filteredByDateRecords.filter {
                if (unitFilter == ALL_TYPE) {
                    it.bloodSugarStateType in BloodSugarStateType.values()
                } else {
                    it.bloodSugarStateType == unitFilter
                }
            }
        } ?: filteredByDateRecords
        _uiState.update { it.copy(filteredRecords = filteredRecords) }
        calculateStatistics(bloodSugarUnit)
    }

    fun showSetReminder(shown: Boolean) {
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

    fun calculateStatistics(bloodSugarUnit: BloodSugarUnit) {

        val records = _uiState.value.filteredRecords

        val bloodSugars = records.map {
            if (it.bloodSugarUnit == bloodSugarUnit) it.bloodSugar
            else if (bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE) it.bloodSugar.convertToMole()
            else it.bloodSugar.convertToMg()
        }
        val averageBloodSugar = if (bloodSugars.isEmpty()) 0f else (bloodSugars.average() * 10).roundToInt() / 10f
        val minBloodSugar = bloodSugars.minOrNull() ?: 0f
        val maxBloodSugar = bloodSugars.maxOrNull() ?: 0f

        val lowRecordsCount = records.count { it.bloodSugarRateType == BloodSugarRateType.LOW }
        val normalRecordsCount = records.count { it.bloodSugarRateType == BloodSugarRateType.NORMAL }
        val preDiabetesRecordsCount = records.count { it.bloodSugarRateType == BloodSugarRateType.PRE_DIABETES }
        val diabetesRecordsCount = records.count { it.bloodSugarRateType == BloodSugarRateType.DIABETES }

        _uiState.update {
            it.copy(
                averageBloodSugar = averageBloodSugar,
                minBloodSugar = minBloodSugar,
                maxBloodSugar = maxBloodSugar,
                lowRecordsCount = lowRecordsCount,
                normalRecordsCount = normalRecordsCount,
                preDiabetesRecordsCount = preDiabetesRecordsCount,
                diabetesRecordsCount = diabetesRecordsCount
            )
        }
    }

    data class UiState(
        val allRecords: List<BloodSugarRecord> = emptyList(),
        val filteredRecords: List<BloodSugarRecord> = emptyList(),
        val bloodSugarUnit: BloodSugarUnit = BloodSugarUnit.MILLIMOLES_PER_LITRE,
        val averageBloodSugar: Float = 0f,
        val minBloodSugar: Float = 0f,
        val maxBloodSugar: Float = 0f,
        val lowRecordsCount: Int = 0,
        val normalRecordsCount: Int = 0,
        val preDiabetesRecordsCount: Int = 0,
        val diabetesRecordsCount: Int = 0,
        val showAskSetAlarmDialog: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val isPurchased: Boolean = false,
    )
}
