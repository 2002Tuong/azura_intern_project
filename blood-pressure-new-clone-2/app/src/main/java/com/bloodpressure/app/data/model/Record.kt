package com.bloodpressure.app.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bloodpressure.app.data.local.BpTypeConverters
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.screen.record.BpType

@Entity(tableName = "record")
@Keep
data class Record(
    @ColumnInfo("systolic")
    val systolic: Int,
    @ColumnInfo("diastolic")
    val diastolic: Int,
    @ColumnInfo("pulse")
    val pulse: Int,
    @ColumnInfo("time")
    val time: String,
    @ColumnInfo("date")
    val date: String,
    @ColumnInfo("type")
    @TypeConverters(BpTypeConverters::class)
    val type: BpType,
    @ColumnInfo("type_name")
    val typeName: String,
    @TypeConverters(NoteConverters::class)
    @ColumnInfo("note")
    val notes: Set<String>,
    @PrimaryKey
    @ColumnInfo("created_at")
    val createdAt: Long
)
