package com.bloodpressure.app.screen.bmi.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.BMIRepository
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

class BMIHistoryViewModel(
    private val repository: BMIRepository,
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

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
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest {records ->
                    _uiState.update {
                        it.copy(
                            records = records.groupBy { item -> item.date },
                            filteredRecords = records.groupBy { item -> item.date }
                        )
                    }
            }
        }
    }

    fun setFilteredRecords(startDateLong: Long, endDateLong: Long) {
        val filteredRecords = filterRecordsByDateRange(startDateLong, endDateLong)
        _uiState.update { it.copy(filteredRecords = filteredRecords) }
    }


    fun showWeightUnitDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                showWeightUnitDialog = show
            )
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

    private fun filterRecordsByDateRange(
        startDateLong: Long,
        endDateLong: Long
    ):  Map<String, List<BMIRecord>> {
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

        return _uiState.value.records.filter { record ->
            try {
                val recordDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(record.key)
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
        }
    }

    data class UiState(
        val records: Map<String, List<BMIRecord>> = mapOf(),
        val filteredRecords: Map<String, List<BMIRecord>> = mapOf(),
        val showWeightUnitDialog: Boolean = false,
        val showHeightUnitDialog: Boolean = false,
        val isPurchased: Boolean = false,
        val weightUnit: WeightUnit = WeightUnit.KG,
        val heightUnit: HeightUnit = HeightUnit.CM,
    )
}