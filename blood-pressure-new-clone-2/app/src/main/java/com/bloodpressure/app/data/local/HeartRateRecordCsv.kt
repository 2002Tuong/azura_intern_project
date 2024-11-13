package com.bloodpressure.app.data.local

import androidx.annotation.Keep

@Keep
data class HeartRateRecordCsv(
    val heartRate: Int,
    val time: String,
    val date: String,
    val typeName: String,
    val note: String,
    val genderType: String,
    val createdAt: Long
)
