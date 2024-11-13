package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.bmi.add.BMIType

class BMITypeConverters {
    @TypeConverter
    fun toMediaSource(ordinal: Int): BMIType = BMIType.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: BMIType): Int = type.ordinal
}