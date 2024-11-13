package com.bloodpressure.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bloodpressure.app.data.local.BMITypeConverters
import com.bloodpressure.app.data.local.GenderTypeConverters
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.screen.bmi.add.BMIType
import com.bloodpressure.app.screen.heartrate.add.GenderType

@Entity(tableName = "bmi_record")
data class BMIRecord(
    @ColumnInfo("bmi")
    val bmi: Float,
    @ColumnInfo("height")
    val height: Float,
    @ColumnInfo("weight")
    val weight: Float,
    @ColumnInfo("time")
    val time: String,
    @ColumnInfo("date")
    val date: String,
    @ColumnInfo("type")
    @TypeConverters(BMITypeConverters::class)
    val type: BMIType,
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
