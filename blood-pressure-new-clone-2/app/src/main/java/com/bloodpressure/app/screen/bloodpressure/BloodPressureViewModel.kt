package com.bloodpressure.app.screen.bloodpressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.SampleRecordProvider
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.AnalyticData
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.AlarmingManager
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

class BloodPressureViewModel(
    private val repository: BpRepository,
    private val dataStore: AppDataStore,
    private val sampleRecordProvider: SampleRecordProvider,
    private val alarmingManager: AlarmingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
//        populateSampleDataIfNeeded()
        observeRecordData()
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            dataStore.sampleRecordIdFlow.collectLatest { id ->
                _uiState.update { it.copy(sampleRecordId = id) }
            }
        }

        TrackingManager.logTrackerScreenLaunchEvent()
    }

    private fun populateSampleDataIfNeeded() {
        viewModelScope.launch {
            if (!dataStore.isSampleDataCreated) {
                val sampleRecord = sampleRecordProvider.provide()
                repository.insertRecord(sampleRecord)
                dataStore.setSampleDataCreated()
                dataStore.setSampleRecordId(sampleRecord.createdAt)
            }
        }
    }

    private fun observeRecordData() {
        viewModelScope.launch {
            repository.getChartData().collectLatest { records ->
                _uiState.update { it.copy(
                    chartData = records.reversed(),
                    filterChartData = records.reversed()
                ) }
            }
        }

        viewModelScope.launch {
            repository.getAllRecords().collectLatest { records ->
                _uiState.update {
                    it.copy(
                        records = records.groupBy { record -> record.date },
                        filteredRecords = records.groupBy { item -> item.date }
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getAnalyticData().collectLatest { analyticData ->
                val recordTypes = mapOf(
                    RecordType.MAX to RecordTypeData(
                        analyticData.maxSystolic,
                        analyticData.maxDiastolic,
                        analyticData.maxPulse
                    ),
                    RecordType.MIN to RecordTypeData(
                        analyticData.minSystolic,
                        analyticData.minDiastolic,
                        analyticData.minPulse
                    ),
                    RecordType.AVERAGE to RecordTypeData(
                        analyticData.averageSystolic,
                        analyticData.averageDiastolic,
                        analyticData.averagePulse
                    ),
                    RecordType.LATEST to RecordTypeData(
                        analyticData.latestSystolic,
                        analyticData.latestDiastolic,
                        analyticData.latestPulse
                    ),
                )
                val recordTypeData = recordTypes[uiState.value.currentRecordType]
                _uiState.update {
                    it.copy(
                        recordTypes = recordTypes,
                        currentRecordTypeData = recordTypeData
                    )
                }
            }
        }
    }

    fun getNextRecordType() {
        val currentRecordType = uiState.value.currentRecordType
        val currentIndex = currentRecordType.ordinal
        val totalItemCount = RecordType.values().size
        val newIndex = if (currentIndex == totalItemCount - 1) 0 else currentIndex + 1
        val newRecordType = RecordType.values()[newIndex]
        val newRecordTypeData = uiState.value.recordTypes?.get(newRecordType)
        _uiState.update {
            it.copy(
                currentRecordType = newRecordType,
                currentRecordTypeData = newRecordTypeData
            )
        }
    }

    fun getPreviousRecordType() {
        val currentRecordType = uiState.value.currentRecordType
        val currentIndex = currentRecordType.ordinal
        val totalItemCount = RecordType.values().size
        val newIndex = if (currentIndex == 0) totalItemCount - 1 else currentIndex - 1
        val newRecordType = RecordType.values()[newIndex]
        val newRecordTypeData = uiState.value.recordTypes?.get(newRecordType)
        _uiState.update {
            it.copy(
                currentRecordType = newRecordType,
                currentRecordTypeData = newRecordTypeData
            )
        }
    }

    fun setFilteredRecords(startDateLong: Long, endDateLong: Long) {
        val filteredRecords = filterRecordsByDateRange(startDateLong, endDateLong, uiState.value.records)
        val chartData = filterChartDataByDateRange(startDateLong, endDateLong)
        _uiState.update {
            it.copy(
                filteredRecords = filteredRecords,
                filterChartData = chartData
            )
        }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    private fun filterChartDataByDateRange(
        startDateLong: Long,
        endDateLong: Long
    ):  List<Record> {
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

        return _uiState.value.chartData?.filter { record ->
            try {
                val recordDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(record.date)
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

    fun showSetReminder(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    data class UiState(
        val chartData: List<Record>? = null,
        val filterChartData: List<Record> = emptyList(),
        val filteredRecords: Map<String, List<Record>> = mapOf(),
        val records: Map<String, List<Record>>? = null,
        val recordTypes: Map<RecordType, RecordTypeData>? = null,
        val currentRecordType: RecordType = RecordType.MAX,
        val currentRecordTypeData: RecordTypeData? = null,
        val isPurchased: Boolean = false,
        val analyticData: AnalyticData? = null,
        val sampleRecordId: Long = 0L,
        val showSetAlarmDialog: Boolean = false,
    )

    enum class RecordType {
        MAX, MIN, AVERAGE, LATEST
    }

    data class RecordTypeData(
        val systolic: Int,
        val diastolic: Int,
        val pulse: Int,
    )
}

fun filterRecordsByDateRange(
    startDateLong: Long,
    endDateLong: Long,
    records: Map<String, List<Record>>?
):  Map<String, List<Record>> {
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

    return records?.filter { record ->
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
    } ?: emptyMap()
}
