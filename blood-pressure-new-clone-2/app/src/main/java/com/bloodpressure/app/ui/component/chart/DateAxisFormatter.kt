package com.bloodpressure.app.ui.component.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class DateAxisFormatter(private val dates: List<String>) : ValueFormatter() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return if (index >= 0 && index < dates.size) {
            val date = dates[index]
            formatDate(date)
        } else {
            ""
        }
    }

    private fun formatDate(date: String): String {

        return try {
            val parsedDate = dateFormat.parse(date)
            return parsedDate?.let { formattedDate ->
                SimpleDateFormat("MM/dd", Locale.getDefault()).format(formattedDate)
            } ?: ""
        } catch (e: Exception) {
            ""
        }


    }
}