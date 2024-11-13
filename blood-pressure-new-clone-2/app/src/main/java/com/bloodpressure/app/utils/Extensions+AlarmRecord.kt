package com.bloodpressure.app.utils

import com.bloodpressure.app.data.model.AlarmRecord

fun AlarmRecord.formatTime(): String {
    val formattedHour = hour.toString().padStart(2, '0')
    val formattedMinute = minute.toString().padStart(2, '0')
    return "$formattedHour:$formattedMinute"
}
