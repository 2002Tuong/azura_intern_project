package com.parallax.hdvideo.wallpapers.di.storage.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.parallax.hdvideo.wallpapers.data.model.SearchHistoryModel
import io.reactivex.Completable
import io.reactivex.Single

@Dao
abstract class SearchHistoryDao {

    @Query("SELECT * FROM search_history")
    abstract fun getAllSearchHistory() : LiveData<List<SearchHistoryModel>>

    @Query("SELECT * FROM search_history ORDER BY time DESC LIMIT 20")
    abstract fun getAll() : Single<List<SearchHistoryModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun add(model : SearchHistoryModel): Completable

    @Query("SELECT * FROM search_history where hashTag = :name LIMIT 1")
    abstract fun selectByName(name: String) : SearchHistoryModel?

    @Delete
    abstract fun deleteQuery(query: SearchHistoryModel)

    @Query("DELETE FROM search_history WHERE `query` = :query")
    abstract fun deleteQuery(query : String) : Int

    @Query("UPDATE search_history SET time = :time, id = :id WHERE `query` = :key")
    abstract fun updateTime(key: String, time : Long, id: Long): Completable

    fun updateOrInsert(text: String, name: String?, id: Long) : Completable {
        val model = SearchHistoryModel()
        model.name = name
        model.query = text
        model.time = System.currentTimeMillis()
        model.id = id
        return Single.just(model).flatMapCompletable {
            val s = model.name ?: ""
            selectByName(s)?.let {res ->
                updateTime(res.query, it.time, it.id)
            } ?: add(it)
        }
    }
}