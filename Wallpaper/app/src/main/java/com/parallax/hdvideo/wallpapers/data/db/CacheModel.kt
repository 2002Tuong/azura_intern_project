package com.parallax.hdvideo.wallpapers.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CacheModel(@PrimaryKey
                      @ColumnInfo(name = "id")
                      val id: String,
                      @ColumnInfo(name = "data")
                      val data: String = "",
                      @ColumnInfo(name = "last_updated")
                      val lastUpdated: String = "",
                      @ColumnInfo(name = "expired")
                      val expired: String = "")