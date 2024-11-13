package com.bloodpressure.app.data.local

import androidx.room.TypeConverter

class NoteConverters {
    @TypeConverter
    fun toList(note: String): Set<String> {
        return if (note.isNotEmpty()) {
            note.split(JOIN_CHAR).toSet()
        } else {
            emptySet()
        }
    }

    @TypeConverter
    fun toString(notes: Set<String>): String {
        return if (notes.isNotEmpty()) {
            notes.joinToString(separator = JOIN_CHAR)
        } else {
            ""
        }
    }

    companion object {
        const val JOIN_CHAR = " #"
    }
}
