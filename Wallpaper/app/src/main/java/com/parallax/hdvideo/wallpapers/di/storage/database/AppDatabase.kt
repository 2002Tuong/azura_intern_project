package com.parallax.hdvideo.wallpapers.di.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.parallax.hdvideo.wallpapers.data.db.BestHashTags
import com.parallax.hdvideo.wallpapers.data.db.CacheModel
import com.parallax.hdvideo.wallpapers.data.model.SearchHistoryModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.storage.database.dao.BestHashTagsDao
import com.parallax.hdvideo.wallpapers.di.storage.database.dao.CacheDao
import com.parallax.hdvideo.wallpapers.di.storage.database.dao.SearchHistoryDao
import com.parallax.hdvideo.wallpapers.di.storage.database.dao.WallpaperDao
import javax.inject.Singleton


/**
 * Created by DatTV on 05,July,2020
 * trandatbkhn@gmail.com
 * Copyright © 2020 Dat Tran. All rights reserved.
 */

@Database(
    entities = [WallpaperModel::class,
        CacheModel::class,
        BestHashTags::class,
        SearchHistoryModel::class], version = 4
)
@Singleton
abstract class AppDatabase: RoomDatabase() {

    abstract fun wallpaper(): WallpaperDao

    abstract fun cacheDao(): CacheDao

    abstract fun hashTagsDao(): BestHashTagsDao

    abstract fun searchHistoryDao() : SearchHistoryDao
}