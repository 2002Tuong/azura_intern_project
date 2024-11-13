package com.bloodpressure.app.screen.waterreminder

import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.WeekDays

enum class ReminderTime(val titleRes: Int) {
    AFTER_WAKE_UP(R.string.after_wake_up),
    BEFORE_BREAKFAST(R.string.before_breakfast),
    AFTER_BREAKFAST(R.string.after_breakfast),
    BEFORE_LUNCH(R.string.before_lunch),
    AFTER_LUNCH(R.string.after_lunch),
    BEFORE_DINNER(R.string.before_dinner),
    AFTER_DINNER(R.string.after_dinner),
    BEFORE_GOING_TO_BED(R.string.before_going_to_bed),
}

data class WaterAlarmRecord(
    val isChecked: Boolean = false,
    val reminderTime: ReminderTime,
    val alarmRecord: AlarmRecord,
)

fun initListRecord(): List<WaterAlarmRecord> {
    return ReminderTime.values().map {
        val (hour, minutes) = when (it) {
            ReminderTime.AFTER_WAKE_UP -> 8 to 0
            ReminderTime.BEFORE_BREAKFAST -> 8 to 30
            ReminderTime.AFTER_BREAKFAST -> 9 to 0
            ReminderTime.BEFORE_LUNCH -> 11 to 30
            ReminderTime.AFTER_LUNCH -> 13 to 30
            ReminderTime.BEFORE_DINNER -> 18 to 0
            ReminderTime.AFTER_DINNER -> 20 to 0
            ReminderTime.BEFORE_GOING_TO_BED -> 22 to 30
        }

        WaterAlarmRecord(
            reminderTime = it,
            alarmRecord = AlarmRecord(
                repeat = true,
                days = WeekDays.values().toList(),
                hour = hour,
                minute = minutes,
                soundEnabled = false,
                vibrateEnabled = false,
                type = AlarmType.WATER_REMINDER,
                createdAt = System.currentTimeMillis(),
                reminderTime = it
            )
        )
    }
}