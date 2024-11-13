package com.bloodpressure.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bloodpressure.app.data.local.AlarmTypeConverter
import com.bloodpressure.app.data.local.DayOfWeekConverter
import com.bloodpressure.app.data.local.ReminderTypeConverter
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.waterreminder.ReminderTime
import com.bloodpressure.app.utils.WeekDays

@Entity(tableName = "alarm_record", indices = [Index(value = ["reminder_time"], unique = true)])
@TypeConverters(DayOfWeekConverter::class, AlarmTypeConverter::class, ReminderTypeConverter::class)
data class AlarmRecord(
    @ColumnInfo(name = "repeat")
    val repeat: Boolean,
    @ColumnInfo(name = "days")
    val days: List<WeekDays>,
    @ColumnInfo(name = "hour")
    val hour: Int,
    @ColumnInfo(name = "minute")
    val minute: Int,
    @ColumnInfo(name = "sound_enabled")
    val soundEnabled: Boolean,
    @ColumnInfo(name = "vibrate_enabled")
    val vibrateEnabled: Boolean,
    @ColumnInfo(name = "type")
    val type: AlarmType,
    @ColumnInfo(name = "reminder_time")
    val reminderTime: ReminderTime? = null,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0
)