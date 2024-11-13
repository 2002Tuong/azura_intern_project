package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.heartrate.add.GenderType

class GenderTypeConverters {

    @TypeConverter
    fun toMediaSource(ordinal: Int): GenderType = GenderType.values()[ordinal]

    @TypeConverter
    fun fromMediaSource(type: GenderType): Int = type.ordinal
}
