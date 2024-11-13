package com.bloodpressure.app.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_cup_record")
@Keep
data class WaterCupRecord(
    @PrimaryKey
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("number_of_cup")
    val numberOfCup: Int,
    @ColumnInfo("bottle_size")
    val bottleSize: Int,
    @ColumnInfo("time")
    val time: String,
    @ColumnInfo("date")
    val date: String,
    @ColumnInfo("actual_water")
    val actualWater: Int
)
