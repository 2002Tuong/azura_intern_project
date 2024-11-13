package com.example.videoart.batterychargeranimation.extension

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Calendar.getTimeFormattedString(simpleDateFormatString: String): String {
    val format = SimpleDateFormat(simpleDateFormatString, Locale.getDefault())

    return format.format(time)
}