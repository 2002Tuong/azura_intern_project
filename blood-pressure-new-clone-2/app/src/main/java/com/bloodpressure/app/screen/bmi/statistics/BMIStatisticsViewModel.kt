package com.bloodpressure.app.screen.bmi.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.data.repository.BMIRepository
import com.bloodpressure.app.screen.bmi.add.BMIType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BMIStatisticsViewModel(
    val repository: BMIRepository,
    val dataStore: AppDataStore
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllRecords().collectLatest {records ->
                _uiState.update {
                    it.copy(
                        records = records,
                        filteredRecord = records,
                        pieChartRecord = records.groupBy { item -> item.type },
                        filteredPieChartRecord = records.groupBy { item -> item.type },
                        weightMax = getMaxWeight(records),
                        weightMin = getMinWeight(records),
                        bmiMax = getMaxBMI(records),
                        bmiMin = getMinBMI(records)
                    )
                }
            }
        }
    }

    fun setFilteredRecords(startDateLong: Long, endDateLong: Long) {
        val filteredRecord = filteredRecordByDateRange(startDateLong, endDateLong)
        _uiState.update {
            it.copy(
                filteredRecord = filteredRecord,
                filteredPieChartRecord = filteredRecord.groupBy {item -> item.type },
                weightMin = getMinWeight(filteredRecord),
                weightMax = getMaxWeight(filteredRecord),
                bmiMin = getMinBMI(filteredRecord),
                bmiMax = getMaxBMI(filteredRecord)
            )
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

    data class UiState(
        val records: List<BMIRecord>? = null,
        val filteredRecord: List<BMIRecord>? = null,
        val pieChartRecord: Map<BMIType, List<BMIRecord>>? = null,
        val filteredPieChartRecord: Map<BMIType, List<BMIRecord>>? = null,
        val weightMax: Float = 105.0f,
        val weightMin: Float = 65.0f,
        val bmiMax: Float = 36.3f,
        val bmiMin: Float = 22.5f,
    )
}