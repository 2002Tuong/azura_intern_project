package com.bloodpressure.app.utils

import android.content.Context
import org.joda.time.Period
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TextFormatter(private val context: Context) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM/yyy", Locale.getDefault())
    private val lineChartDateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.isLenient = false
        return timeFormat.format(cal.time)
    }

    fun formatTime(time: Date): String {
        return timeFormat.format(time)
    }

    fun formatDate(timeMs: Long): String {
        return dateFormat.format(timeMs)
    }

    fun getBpTypeName(nameRes: Int): String = context.getString(nameRes)

    fun parse(dateString: String): Date {
        return dateFormat.parse(dateString) ?: Date()
    }

    fun formatLineChartDate(date: Date): String {
        return lineChartDateFormat.format(date)
    }

    fun formatDateTime(inputDate: String, inputTime: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy, HH:mm", Locale.getDefault())

        val dateTimeString = "$inputDate $inputTime"
        val date = inputFormat.parse(dateTimeString)

        return if (date != null) {
            outputFormat.format(date)
        } else {
            ""
        }
    }


    fun convertToPeriodText(isoPeriod: String): String {
        return try {
            val period = Period.parse(isoPeriod)
            when {
                period.years > 0 -> "year"
                period.months > 0 -> "month"
                period.weeks > 0 -> "week"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
