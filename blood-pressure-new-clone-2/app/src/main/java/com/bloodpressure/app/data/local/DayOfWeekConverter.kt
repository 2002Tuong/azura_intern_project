package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.utils.WeekDays
import java.time.DayOfWeek

class DayOfWeekConverter {
    @TypeConverter
    fun fromDayOfWeekList(dayOfWeekList: List<WeekDays>): String {
        return dayOfWeekList.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toDayOfWeekList(dayOfWeekString: String): List<WeekDays> {

        if (dayOfWeekString.isEmpty()) return emptyList()
        return dayOfWeekString.split(",").map { WeekDays.valueOf(it) }
    }
}