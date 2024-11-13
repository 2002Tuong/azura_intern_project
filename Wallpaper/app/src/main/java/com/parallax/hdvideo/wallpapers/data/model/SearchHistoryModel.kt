package com.parallax.hdvideo.wallpapers.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryModel(
    @PrimaryKey
    @ColumnInfo(name = "query")
    var query : String = "",
    @ColumnInfo(name = "time")
    var time : Long = 0
) {
    @ColumnInfo(name = "hashTag")
    var name: String? = null
    @ColumnInfo(name = "id")
    var id = 0L

}