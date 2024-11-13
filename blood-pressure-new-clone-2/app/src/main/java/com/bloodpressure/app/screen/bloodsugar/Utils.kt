package com.bloodpressure.app.screen.bloodsugar

import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.utils.ConvertUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

const val SCOPE_NAME = "Detail Scope"
const val SCOPE_ID = "viewModelScope"


fun Float.convertToMg(): Float =
    (this / ConvertUnit.COEFFICIENT_DL_L * 10).roundToInt() / 10f

fun Float.convertToMole() =
    (this * ConvertUnit.COEFFICIENT_DL_L * 10).roundToInt() / 10f

fun List<BloodSugarRecord>.filterRecordsByDateRange(
    startDateLong: Long,
    endDateLong: Long
): List<BloodSugarRecord> {
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
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return filter { record ->
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

            val isWithinRange = recordDateCalendar in startDateCalendar..endDateCalendar
            isWithinRange
        } catch (e: Exception) {
            false
        }
    }
}
