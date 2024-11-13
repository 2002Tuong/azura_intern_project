package com.bloodpressure.app.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
@Keep
data class Note(
    @PrimaryKey
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("position")
    val position: Int
)
