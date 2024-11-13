package com.parallax.hdvideo.wallpapers.di.storage.database.dao

import androidx.room.*
import com.parallax.hdvideo.wallpapers.data.db.CacheModel

@Dao
abstract class CacheDao {

    @Query("SELECT * FROM CacheModel WHERE id = :id LIMIT 1")
    abstract fun findCache(id: String): CacheModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg cacheModel: CacheModel)

    @Delete
    abstract fun delete(vararg cacheModel: CacheModel)

    @Delete
    abstract fun delete(models: List<CacheModel>)

    @Query("SELECT * FROM CacheModel")
    abstract fun selectAll() : List<CacheModel>

    @Query("SELECT * FROM CacheModel")
    abstract fun selectExpiredTime() : List<CacheModel>

    @Query("DELETE FROM CacheModel")
    abstract fun deleteAll()
}