package com.bloodpressure.app.screen.waterreminder

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.data.repository.WaterReminderRepository
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

class WaterReminderViewModel(
    private val waterRepository: WaterReminderRepository,
    private val textFormatter: TextFormatter,
    private val dataStore: AppDataStore,
    private val alarmingManager: AlarmingManager
) : ViewModel() {
    companion object {
        const val convertRatio = 29.57f

        fun convertMlToOz(value: Int) = (value / convertRatio).roundToInt()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState
    var tempBottleSizeValue: Int by mutableStateOf(-1)

    init {
        TrackingManager.logTrackerScreenLaunchEvent()
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            waterRepository.getWaterCupRecordsToday()
                .distinctUntilChanged()
                .collectLatest { records ->
                    if (records.isNotEmpty()) {
                        _uiState.apply {
                            update { it.copy(listOfWaterCup = records) }
                            update { it.copy(numberOfCup = records.first().numberOfCup) }
                            update { it.copy(actualWater = records.first().actualWater) }
                            update { it.copy(lastDrink = records.first().bottleSize) }
                        }
                    }
                }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(currentBottleSize = dataStore.currentBottleSize) }
            _uiState.update { it.copy(dailyGoal = dataStore.waterDailyGoal) }
            _uiState.update { it.copy(isMl = dataStore.waterVolumeType) }
        }

        viewModelScope.launch {
            alarmingManager.alarmRepository.getAllRecords()
                .distinctUntilChanged()
                .collectLatest { records ->

                    val hasWaterReminderAlarm = records.any { it.type == AlarmType.WATER_REMINDER }
                    _uiState.update { it.copy(hasWaterReminderAlarm = hasWaterReminderAlarm) }
                }
        }
    }

    fun setDailyGoal(value: Int) {
        var valueInput = 0
        _uiState.update {
            valueInput = if (_uiState.value.isMl) value
            else (value * convertRatio).roundToInt()
            it.copy(
                dailyGoal = valueInput
            )
        }
        viewModelScope.launch {
            dataStore.setWaterDailyGoal(valueInput)
        }
    }

    fun showEditWaterGoalDialog(isShow: Boolean) {
        _uiState.update { it.copy(isShowEditGoalDialog = isShow) }
    }

    fun showEditSizeOfCup(isShow: Boolean) {
        _uiState.update { it.copy(isShowEditSizeOfCup = isShow) }
    }

    fun setBottleSize(value: Int) {
        var valueInput = 0
        _uiState.update {
            valueInput = if (it.isMl) value
            else (value * convertRatio).roundToInt()
            it.copy(
                currentBottleSize = valueInput
            )
        }
        viewModelScope.launch {
            dataStore.setCurrentBottleSize(valueInput)
        }
    }

    fun setVolumeType(isMl: Boolean) {
        _uiState.update { it.copy(isMl = isMl) }
        viewModelScope.launch {
            dataStore.setVolumeType(isMl)
        }
    }

    fun increaseWaterCup() {
        val calendar = Calendar.getInstance()
        val date = textFormatter.formatDate(calendar.timeInMillis)
        val time = textFormatter.formatTime(calendar.time)
        _uiState.update {
            it.copy(actualWater = it.actualWater + it.currentBottleSize)
        }
        _uiState.update {
            it.copy(numberOfCup = it.numberOfCup + 1)
        }

        val newRecord = WaterCupRecord(
            createdAt = System.currentTimeMillis(),
            bottleSize = _uiState.value.currentBottleSize,
            numberOfCup = _uiState.value.numberOfCup,
            actualWater = _uiState.value.actualWater,
            date = date,
            time = time
        )
        _uiState.update {
            val currentList = it.listOfWaterCup.toMutableList()
            currentList.add(newRecord)

            it.copy(listOfWaterCup = currentList)
        }
        viewModelScope.launch {
            waterRepository.insertWaterCupRecord(newRecord)
        }
    }

    fun decreaseWaterCup() {
        _uiState.apply {
            if (value.actualWater <= 0)
                return
            try {
                val currentList = value.listOfWaterCup.toMutableList()
                val lastCup = currentList.removeFirst()

                update { it.copy(listOfWaterCup = currentList) }
                update { it.copy(actualWater = it.actualWater - lastCup.bottleSize) }
                update { it.copy(numberOfCup = it.numberOfCup - 1) }
                update { it.copy(lastMinusDrinkInDisplay = convertDataToDisplayData(lastCup.bottleSize))}
                update { it.copy(lastDrink = if (currentList.isNotEmpty()) currentList.first().bottleSize else 0) }
            } catch (e: NoSuchElementException) {
                update { it.copy(lastDrink = 0) }
            }
        }

        viewModelScope.launch {
            waterRepository.deleteLatestRecord()
        }
    }

    private fun convertDataToDisplayData(value: Int): Int {
        return if (_uiState.value.isMl) value else (value / convertRatio).roundToInt()
    }

    fun showSetReminder(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    fun showAskSetAlarmDialog(shown: Boolean) {
        _uiState.update { it.copy(showAskSetAlarmDialog = shown) }
    }

    fun setTempBottleSize(isMl: Boolean, tempValue: Int) {
        val convertRatio = 29.57f
        tempBottleSizeValue =
            if (tempValue == -1) -1 else if (isMl) (tempValue * convertRatio).roundToInt()
            else (tempValue / convertRatio).roundToInt()
    }
    @Immutable
    data class UiState(
        val isShowEditGoalDialog: Boolean = false,
        val isShowEditSizeOfCup: Boolean = false,
        val actualWater: Int = 0,
        val dailyGoal: Int = 2000,
        val lastDrink: Int = 0,
        val listOfWaterCup: List<WaterCupRecord> = listOf(),
        val isMl: Boolean = true,
        val numberOfCup: Int = 0,
        val currentBottleSize: Int = 200,
        val lastMinusDrinkInDisplay: Int = 200,
        val showSetAlarmDialog: Boolean = false,
        val hasWaterReminderAlarm: Boolean = false,
        val showAskSetAlarmDialog: Boolean = false,
        val isPurchased: Boolean = false,
    ) {

        val bottleSizeInDisplay: Int
            get() = if (isMl) currentBottleSize else (currentBottleSize / convertRatio).roundToInt()
        val actualWaterInDisplay: Int
            get() = if (isMl) actualWater else (actualWater / convertRatio).roundToInt()
        val dailyGoalInDisplay: Int
            get() = if (isMl) (dailyGoal) else (dailyGoal / convertRatio).roundToInt()
        val lastDrinkInDisplay: Int
            get() = if (isMl) lastDrink else (lastDrink / convertRatio).roundToInt()
        val unitInDisplay: String
            get() = if (isMl) "ml" else "oz"

        fun getBottleSizeChanged(waterAddValue: String): String {
            val volume = if(waterAddValue == "+") {
                bottleSizeInDisplay
            } else {
                lastMinusDrinkInDisplay
            }

            return "$waterAddValue$volume $unitInDisplay"
        }
    }
}