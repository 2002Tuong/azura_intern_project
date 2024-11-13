package com.bloodpressure.app.screen.bmi.historyandstatistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.data.repository.BMIRepository
import com.bloodpressure.app.screen.bmi.add.BMIType
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.ConvertUnit
import com.bloodpressure.app.utils.HeightUnit
import com.bloodpressure.app.utils.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BMIHistoryAndStatisticsViewModel(
    private val repository: BMIRepository,
    private val dataStore: AppDataStore,
    private val alarmingManager: AlarmingManager,
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val  uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            dataStore.weightUnit
                .distinctUntilChanged()
                .collectLatest { weightUnit ->
                    _uiState.update { it.copy(weightUnit = weightUnit) }
                }
        }

        viewModelScope.launch {
            dataStore.heightUnit
                .distinctUntilChanged()
                .collectLatest { heightUnit ->
                    _uiState.update { it.copy(heightUnit = heightUnit) }
                }
        }

        observeRecordData()
    }

    fun setFilteredChartData(startDateLong: Long, endDateLong: Long) {
        val filteredChartData = filteredRecordByDateRange(startDateLong, endDateLong)
        _uiState.update {
            it.copy(
                filteredRecordForTrends = filteredChartData,
                filteredRecordsForHistory = filteredChartData.groupBy { item -> item.date },
                filteredRecordForStatistics = filteredChartData.groupBy { item -> item.type },
                weightMin = getMinWeight(filteredChartData),
                weightMax = getMaxWeight(filteredChartData),
                bmiMin = getMinBMI(filteredChartData),
                bmiMax = getMaxBMI(filteredChartData)
            )
        }
    }

    private fun observeRecordData() {
        viewModelScope.launch {
            repository.getChartData()
                .distinctUntilChanged()
                .collectLatest {records ->
                    val reverseRecords = records.reversed()
                    _uiState.update {
                        it.copy(
                            records = reverseRecords,
                            filteredRecordForTrends = reverseRecords,
                            filteredRecordsForHistory = reverseRecords.groupBy { item -> item.date },
                            filteredRecordForStatistics = reverseRecords.groupBy { item -> item.type },
                            weightMax = getMaxWeight(reverseRecords),
                            weightMin = getMinWeight(reverseRecords),
                            bmiMax = getMaxBMI(reverseRecords),
                            bmiMin = getMinBMI(reverseRecords)
                        )
                    }
            }
        }
    }

    private fun getMinBMI(records: List<BMIRecord>): Float {
        return records.minByOrNull { it.bmi }?.bmi ?: 0f
    }

    private fun getMaxBMI(records: List<BMIRecord>): Float {
        return records.maxByOrNull { it.bmi }?.bmi ?: 0f
    }

    private fun getMinWeight(records: List<BMIRecord>): Float {
        return records.minByOrNull { it.weight }?.weight ?: 0f
    }

    private fun getMaxWeight(records: List<BMIRecord>): Float {
        return records.maxByOrNull { it.weight }?.weight ?: 0f
    }

    private fun filteredRecordByDateRange(
        startDateLong: Long,
        endDateLong: Long
    ): List<BMIRecord> {
        val startDate = Date(startDateLong)
        val endDate = Date(endDateLong)

        val startDateCalendar = Calendar.getInstance().apply { time = startDate }
        val endDateCalendar = Calendar.getInstance().apply { time = endDate }

        startDateCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        endDateCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
        }

        return _uiState.value.records?.filter {
            try {
                val recordDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)
                val recordDateCalendar = Calendar.getInstance().apply {
                    if (recordDate != null) {
                        time = recordDate
                    }
                }

                recordDateCalendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                recordDateCalendar in startDateCalendar..endDateCalendar
            } catch (e: Exception) {
                false
            }
        } ?: emptyList()
    }

    fun showSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    fun showWeightUnitDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                showWeightUnitDialog = show
            )
        }
    }

    fun onWeightUnitChange(weightUnit: WeightUnit, weight: Float): Float {
        return when(weightUnit) {
            WeightUnit.KG -> weight
            WeightUnit.LBS -> ConvertUnit.convertKgToLbs(weight)
        }
    }

    fun setWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            dataStore.setWeightUnit(weightUnit)
            _uiState.update { it.copy(weightUnit = weightUnit) }
        }
    }

    fun showHeightUnitDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                showHeightUnitDialog = show
            )
        }
    }

    fun setHeightUnit(heightUnit: HeightUnit) {
        viewModelScope.launch {
            dataStore.setHeightUnit(heightUnit)
            _uiState.update { it.copy(heightUnit = heightUnit) }
        }
    }

    data class UiState(
        val records: List<BMIRecord>? = null,
        val filteredRecordsForHistory: Map<String, List<BMIRecord>>? = null,
        val filteredRecordForTrends: List<BMIRecord>? = null,
        val filteredRecordForStatistics: Map<BMIType, List<BMIRecord>>? = null,
        val weightMax: Float = 105.0f,
        val weightMin: Float = 65.0f,
        val bmiMax: Float = 36.3f,
        val bmiMin: Float = 22.5f,
        val isPurchased: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val showWeightUnitDialog: Boolean = false,
        val showHeightUnitDialog: Boolean = false,
        val weightUnit: WeightUnit = WeightUnit.KG,
        val heightUnit: HeightUnit = HeightUnit.CM,
    )
}