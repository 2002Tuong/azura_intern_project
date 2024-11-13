package com.bloodpressure.app.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bloodpressure.app.data.local.GenderTypeConverters
import com.bloodpressure.app.data.local.HeartRateTypeConverters
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import kotlinx.serialization.Serializable

@Entity(tableName = "heart_rate_record")
@Keep
@Serializable
data class HeartRateRecord(
    @ColumnInfo("heart_rate")
    val heartRate: Int,
    @ColumnInfo("time")
    val time: String,
    @ColumnInfo("date")
    val date: String,
    @ColumnInfo("type")
    @TypeConverters(HeartRateTypeConverters::class)
    val type: HeartRateType,
    @ColumnInfo("type_name")
    val typeName: String,
    @TypeConverters(NoteConverters::class)
    @ColumnInfo("note")
    val notes: Set<String>,
    @ColumnInfo("age")
    val age: Int,
    @ColumnInfo("gender_type")
    @TypeConverters(GenderTypeConverters::class)
    val genderType: GenderType,
    @PrimaryKey
    @ColumnInfo("created_at")
    val createdAt: Long
)
