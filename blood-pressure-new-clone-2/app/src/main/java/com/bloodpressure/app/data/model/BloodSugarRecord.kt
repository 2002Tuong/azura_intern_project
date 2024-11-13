package com.bloodpressure.app.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bloodpressure.app.data.local.BloodSugarRateTypeConverters
import com.bloodpressure.app.data.local.BloodSugarStateTypeConverters
import com.bloodpressure.app.data.local.BloodSugarUnitTypeConverters
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.data.local.TargetRangesConverters
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.screen.bloodsugar.type.TargetRange
import com.bloodpressure.app.utils.BloodSugarUnit
import kotlinx.serialization.Serializable

@Entity(tableName = "blood_sugar_record")
@Keep
@Serializable
data class BloodSugarRecord(
    @ColumnInfo("blood_sugar")
    val bloodSugar: Float,
    @ColumnInfo("blood_sugar_unit")
    @TypeConverters(BloodSugarUnitTypeConverters::class)
    val bloodSugarUnit: BloodSugarUnit,
    @ColumnInfo("time")
    val time: String,
    @ColumnInfo("date")
    val date: String,
    @ColumnInfo("state_type")
    @TypeConverters(BloodSugarStateTypeConverters::class)
    val bloodSugarStateType: BloodSugarStateType,
    @ColumnInfo("target_ranges")
    @TypeConverters(TargetRangesConverters::class)
    val targetRanges: List<TargetRange>,
    @ColumnInfo("blood_sugar_rate_type", defaultValue = "1")
    @TypeConverters(BloodSugarRateTypeConverters::class)
    val bloodSugarRateType: BloodSugarRateType = targetRanges
        .firstOrNull { it.bloodSugarStateType == bloodSugarStateType }
        ?.getBloodSugarRateType(bloodSugar) ?: BloodSugarRateType.NORMAL,
    @TypeConverters(NoteConverters::class)
    @ColumnInfo("note")
    val notes: Set<String>,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("row_id")
    val rowId: Long = 0
)
