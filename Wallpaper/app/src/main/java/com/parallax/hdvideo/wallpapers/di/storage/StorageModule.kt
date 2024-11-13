package com.parallax.hdvideo.wallpapers.di.storage

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.database.DatabaseInfo
import com.parallax.hdvideo.wallpapers.di.storage.database.dao.CacheDao
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.di.storage.frefs.SharedPreferencesStorage
import com.parallax.hdvideo.wallpapers.utils.LanguageUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class StorageModule {

    @Singleton
    @Provides
    fun localStorage(pre: SharedPreferencesStorage) : LocalStorage = pre

    @Singleton
    @Provides
    fun appDatabase(@ApplicationContext context: Context, @DatabaseInfo dbName: String) : AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                .addMigrations(migrationFrom1To2)
                .addMigrations(migrationFrom2To3)
                .addMigrations(migrationFrom3To4)
                .build()
    }

    @Singleton
    @Provides
    fun cacheDao(database: AppDatabase): CacheDao = database.cacheDao()

    private val migrationFrom1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `search_history` ADD COLUMN `id` INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val migrationFrom2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `Wallpaper` ADD COLUMN `screenRatio` INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE `Wallpaper` ADD COLUMN `contentType` TEXT")
        }
    }

    private val migrationFrom3To4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `Wallpaper` ADD COLUMN `vipNumber` INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Singleton
    @Provides
    fun provideLanguageUtils(preferencesStorage: SharedPreferencesStorage) = LanguageUtils(preferencesStorage)
}