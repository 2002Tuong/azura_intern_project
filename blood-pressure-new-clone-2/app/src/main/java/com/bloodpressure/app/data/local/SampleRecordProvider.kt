package com.bloodpressure.app.data.local

import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.screen.record.BpType
import com.bloodpressure.app.utils.TextFormatter
import java.util.Calendar

class SampleRecordProvider(private val textFormatter: TextFormatter) {

    fun provide(): Record {
        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)
        return Record(
            systolic = 100,
            diastolic = 78,
            pulse = 80,
            time = formattedTime,
            date = formattedDate,
            type = BpType.NORMAL,
            typeName = textFormatter.getBpTypeName(BpType.NORMAL.nameRes),
            notes = emptySet(),
            createdAt = System.currentTimeMillis()
        )
    }
}
