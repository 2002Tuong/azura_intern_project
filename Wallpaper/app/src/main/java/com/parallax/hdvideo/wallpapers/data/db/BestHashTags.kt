package com.parallax.hdvideo.wallpapers.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Best_Hash_Tags")
data class BestHashTags(
    @ColumnInfo(name = "hash_tags", defaultValue = "")
    var hashTags: String = "",
    @ColumnInfo(name = "is_image")
    var isImage: Boolean = true,
    @ColumnInfo(name = "count")
    var count: Int = 0,
    @ColumnInfo(name = "avg")
    var avg: Double = 0.0) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}