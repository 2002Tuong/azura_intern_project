package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.utils.BloodSugarUnit

class BloodSugarStateTypeConverters {
    @TypeConverter
    fun toMediaSource(ordinal: Int): BloodSugarStateType = BloodSugarStateType.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: BloodSugarStateType): Int = type.ordinal
}

class BloodSugarRateTypeConverters {
    @TypeConverter
    fun toMediaSource(ordinal: Int): BloodSugarRateType = BloodSugarRateType.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: BloodSugarRateType): Int = type.ordinal
}

class BloodSugarUnitTypeConverters {
    @TypeConverter
    fun toMediaSource(ordinal: Int): BloodSugarUnit = BloodSugarUnit.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: BloodSugarUnit): Int = type.ordinal
}
