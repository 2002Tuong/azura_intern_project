package com.bloodpressure.app.data.model

import androidx.room.ColumnInfo

data class AnalyticData(
    @ColumnInfo("max_systolic")
    val maxSystolic: Int,
    @ColumnInfo("min_systolic")
    val minSystolic: Int,
    @ColumnInfo("average_systolic")
    val averageSystolic: Int,
    @ColumnInfo("latest_systolic")
    val latestSystolic: Int,
    @ColumnInfo("max_diastolic")
    val maxDiastolic: Int,
    @ColumnInfo("min_diastolic")
    val minDiastolic: Int,
    @ColumnInfo("average_diastolic")
    val averageDiastolic: Int,
    @ColumnInfo("latest_diastolic")
    val latestDiastolic: Int,
    @ColumnInfo("max_pulse")
    val maxPulse: Int,
    @ColumnInfo("min_pulse")
    val minPulse: Int,
    @ColumnInfo("average_pulse")
    val averagePulse: Int,
    @ColumnInfo("latest_pulse")
    val latestPulse: Int
)
