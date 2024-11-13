package com.parallax.hdvideo.wallpapers.di.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.parallax.hdvideo.wallpapers.data.db.BestHashTags
import com.parallax.hdvideo.wallpapers.extension.round
import com.parallax.hdvideo.wallpapers.extension.startsWith
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import kotlin.math.max

@Dao
abstract class BestHashTagsDao {

    private val commonHashTags = arrayOf("dotuoi", "gioitinh", "mausac")

    @Query("SELECT * FROM Best_Hash_Tags WHERE is_image = :isImage ORDER BY count DESC, avg DESC LIMIT :limit")
    abstract fun select(isImage: Boolean, limit: Int): List<BestHashTags>

    @Query("SELECT * FROM Best_Hash_Tags WHERE  is_image = :isImage AND hash_tags = :hashTags LIMIT 1")
    abstract fun selectByName(isImage: Boolean, hashTags: String): BestHashTags?

    @Query("SELECT * FROM Best_Hash_Tags WHERE is_image = :isImage")
    abstract fun selectAll(isImage: Boolean): List<BestHashTags>

    @Insert
    abstract fun insert(vararg models: BestHashTags)

    @Update
    abstract fun update(vararg models: BestHashTags)

    fun update(hashTags: String?, isImage: Boolean) {
        if (hashTags.isNullOrEmpty()) return
        val list = hashTags.replace(",,", ",").split(",").filter {
            val hash = it.trim()
            //remove common hashTags
            hash.isNotEmpty() && !commonHashTags.startsWith(hash)
        }
        val current = System.currentTimeMillis().toDouble()
        // remove parent hashTags,parent,
        for ((index, str) in list.withIndex()) {
            val textChild = list.withIndex().firstOrNull { it.index != index && it.value.contains(str) }
            if (textChild == null) {
                val itemStore = selectByName(isImage, str)
                if (itemStore != null) {
                    itemStore.avg = ((itemStore.avg * itemStore.count + current) / (itemStore.count + 1)).round(2)
                    itemStore.count += 1
                    update(itemStore)
                } else {
                    insert(BestHashTags(hashTags = str, isImage = isImage, count = 1, avg = current))
                }
            }
        }
    }

    fun getBestHashTags(isImage: Boolean, limit: Int): String {
        if (limit <= 0) return RemoteConfig.hashTagDefault
        val str = select(isImage, max(limit, 5)).foldIndexed("") {index, acc, bestHashTags ->
            if (index > 0) acc + "," + bestHashTags.hashTags
            else bestHashTags.hashTags
        }
        return if (str.isEmpty()) RemoteConfig.hashTagDefault else str
    }
}