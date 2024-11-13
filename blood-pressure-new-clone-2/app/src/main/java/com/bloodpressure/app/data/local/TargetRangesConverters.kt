package com.bloodpressure.app.data.local

import androidx.room.TypeConverter
import com.bloodpressure.app.screen.bloodsugar.type.TargetRange
import com.bloodpressure.app.utils.JsonProvider
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TargetRangesConverters {
    @TypeConverter
    fun toList(note: String): List<TargetRange> {
        return try {
            JsonProvider.json.decodeFromString(note)
        } catch (e: IllegalArgumentException) {
            emptyList()
        } catch (e: SerializationException) {
            emptyList()
        }
    }

    @TypeConverter
    fun toString(targetRanges: List<TargetRange>): String {
        return try {
            Json.encodeToString(targetRanges)
        } catch (e: IllegalArgumentException) {
            ""
        } catch (e: SerializationException) {
            ""
        }
    }
}