package com.parallax.hdvideo.wallpapers.di.storage.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface WallpaperDao {

    @Query("SELECT * FROM Wallpaper")
    fun selectAll(): Single<List<WallpaperModel>>

    @Query("SELECT * FROM Wallpaper WHERE hasDownloaded = :hasDownloaded")
    fun selectHistoryDownload(hasDownloaded: Boolean = true): Flowable<List<WallpaperModel>>

    @Query("SELECT * FROM Wallpaper WHERE isFavorite = :isFavorite")
    fun selectFavorite(isFavorite: Boolean = true): Flowable<List<WallpaperModel>>

    @Query("SELECT * FROM Wallpaper WHERE isPlaylist = 1 ORDER BY lastUpdated DESC")
    fun retrievePlaylistQueue() : Flowable<List<WallpaperModel>>

    @Query("SELECT * FROM Wallpaper WHERE isPlaylist = 1 ORDER BY lastUpdated DESC")
    fun getPlaylist() : List<WallpaperModel>

    @Query("SELECT COUNT(id) FROM Wallpaper WHERE isPlaylist = 1")
    fun countPlaylist() : LiveData<Int>

    @Query("SELECT COUNT(id) FROM Wallpaper WHERE isPlaylist = 1")
    fun countItemPlaylist() : Int

    @Query("SELECT * FROM Wallpaper WHERE isPlaylist = 1")
    fun selectUrlPlaylist() : LiveData<List<WallpaperModel>>

    @Query("SELECT * FROM Wallpaper WHERE id = :id LIMIT 1")
    fun selectFirstItem(id: String): WallpaperModel?

    @Query("SELECT * from Wallpaper where id = :id")
    fun selectById(id: String): Single<WallpaperModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg model: WallpaperModel): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(vararg model: WallpaperModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: List<WallpaperModel>): Completable

    @Query("UPDATE Wallpaper SET hasDownloaded = :hasDownloaded WHERE id = :id")
    fun updateDownload(id: String, hasDownloaded: Boolean): Completable

    @Query("UPDATE Wallpaper SET hasShownRewardAd = :hasShownRewardAd WHERE id = :id")
    fun updateRewardAd(id: String, hasShownRewardAd: Boolean): Completable

    @Update
    fun update(vararg model: WallpaperModel): Completable

    @Query("UPDATE Wallpaper SET isFavorite = :isFavorite WHERE id = :id")
    fun updateFavorite(id: String, isFavorite: Boolean = true): Completable

    @Query("DELETE FROM WALLPAPER WHERE id = :id")
    fun delete(id: String): Completable

    @Delete
    fun delete(vararg model: WallpaperModel): Completable

    fun updatePlayList(vararg model: WallpaperModel): Completable {
        model.forEach { it.lastUpdated = System.currentTimeMillis() }
        return update(*model)
    }

    fun insertPlayList(vararg model: WallpaperModel): Completable {
        model.forEach { it.lastUpdated = System.currentTimeMillis() }
        return insert(*model)
    }

    fun insertPlayList(models: List<WallpaperModel>): Completable {
        models.forEach { it.lastUpdated = System.currentTimeMillis() }
        return insert(models)
    }
}