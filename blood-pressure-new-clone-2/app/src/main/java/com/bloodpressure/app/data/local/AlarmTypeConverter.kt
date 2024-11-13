package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.alarm.AlarmType

class AlarmTypeConverter {
    @TypeConverter
    fun fromAlarmType(alarmType: AlarmType): String {
        return alarmType.name
    }

    @TypeConverter
    fun toAlarmType(data: String): AlarmType {
        return enumValueOf(data)
    }
}
