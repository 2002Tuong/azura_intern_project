package com.parallax.hdvideo.wallpapers.di

import com.parallax.hdvideo.wallpapers.di.storage.database.DatabaseInfo
import com.parallax.hdvideo.wallpapers.di.storage.frefs.PreferenceInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ConfigModule {

    @Singleton
    @Provides
    @PreferenceInfo
    fun preferencesName(): String {
        return "config"
    }

    @Singleton
    @Provides
    @DatabaseInfo
    fun databaseName(): String {
        return "my_database.db"
    }

}