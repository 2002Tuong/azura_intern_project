package com.bloodpressure.app.screen.waterreminder.history

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.data.repository.WaterReminderRepository
import com.bloodpressure.app.screen.waterreminder.WaterReminderViewModel.Companion.convertRatio
import com.bloodpressure.app.utils.TextFormatter
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
import kotlin.math.roundToInt

class WaterHistoryViewModel(
    private val waterRepository: WaterReminderRepository,
    private val textFormatter: TextFormatter,
    private val dataStore: AppDataStore
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

        viewModelScope.launch {
            waterRepository.getAllWaterHistory().distinctUntilChanged().collectLatest { list ->
                _uiState.apply {
                    update { it.copy(records = list) }
                    update { it.copy(filteredRecords = list)}
                }
            }
        }

        viewModelScope.launch {
            waterRepository.getWaterCupRecordsToday().distinctUntilChanged().collectLatest { list ->
                var actualWater = list.firstOrNull()?.actualWater ?: 0
                if(actualWater != 0 && !dataStore.waterVolumeType) {
                    actualWater = (actualWater / convertRatio).roundToInt()
                }

                _uiState.update { it.copy(waterToday = actualWater) }
            }
        }

        viewModelScope.launch {
            var dailyGoal = dataStore.waterDailyGoal
            if(dailyGoal != 0 && !dataStore.waterVolumeType) {
                dailyGoal = (dailyGoal / convertRatio).roundToInt()
            }
            _uiState.update { it.copy(goal = dailyGoal) }
        }

        _uiState.update { it.copy(isMl = dataStore.waterVolumeType) }
    }

    fun setFilteredRecords(startDate: Long, endDate: Long) {
        val filteredList = filterRecordsByDateRange(uiState.value.records, startDate, endDate)
        _uiState.update { it.copy(filteredRecords = filteredList) }
    }

    private fun filterRecordsByDateRange(
        records: List<WaterCupRecord>,
        startDateLong: Long,
        endDateLong: Long
    ): List<WaterCupRecord> {
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

        return records.filter { record ->

            try {
                val recordDate =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(record.date)
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

                val isWithinRange = recordDateCalendar in startDateCalendar..endDateCalendar
                isWithinRange
            } catch (e: Exception) {
                false
            }
        }
    }

    @Immutable
    data class UiState(
        val records: List<WaterCupRecord> = listOf(),
        val filteredRecords: List<WaterCupRecord> = listOf(),
        val goal: Int = 2000,
        val waterToday: Int = 2000,
        val isMl: Boolean = true,
        val isPurchased: Boolean = false,
    )
}