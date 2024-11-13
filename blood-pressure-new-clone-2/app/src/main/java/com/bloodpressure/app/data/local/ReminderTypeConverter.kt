package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.waterreminder.ReminderTime

class ReminderTypeConverter {
    @TypeConverter
    fun fromReminderTime(reminderTime: ReminderTime): String {
        return reminderTime.name
    }

    @TypeConverter
    fun toReminderTime(data: String): ReminderTime {
        return enumValueOf(data)
    }
}