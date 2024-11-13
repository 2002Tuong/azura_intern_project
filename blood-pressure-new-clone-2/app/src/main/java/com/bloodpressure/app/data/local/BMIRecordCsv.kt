package com.bloodpressure.app.data.local

import androidx.annotation.Keep
@Keep
data class BMIRecordCsv(
    val weight: Float,
    val height: Float,
    val bmi: Float,
    val date: String,
    val time: String,
    val typeName: String,
    val note: String,
    val createdAt: Long
)
