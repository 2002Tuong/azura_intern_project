package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import com.bloodpressure.app.screen.record.BpType

class HeartRateTypeConverters {

    @TypeConverter
    fun toMediaSource(ordinal: Int): HeartRateType = HeartRateType.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: HeartRateType): Int = type.ordinal
}
