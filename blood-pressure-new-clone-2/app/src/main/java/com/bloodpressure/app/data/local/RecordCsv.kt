package com.bloodpressure.app.data.local

import androidx.annotation.Keep

@Keep
data class RecordCsv(
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val time: String,
    val date: String,
    val typeName: String,
    val note: String,
    val createdAt: Long
)
