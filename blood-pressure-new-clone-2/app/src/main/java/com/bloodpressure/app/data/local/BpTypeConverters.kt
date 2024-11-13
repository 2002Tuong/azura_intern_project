package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.record.BpType

class BpTypeConverters {

    @TypeConverter
    fun toMediaSource(ordinal: Int): BpType = BpType.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: BpType): Int = type.ordinal
}
