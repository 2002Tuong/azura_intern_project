package com.parallax.hdvideo.wallpapers.data.model

import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import java.io.Serializable

data class HashTag(
    @SerializedName("id")
    var id: Long = 0,
    @SerializedName("hashtag")
    var hashtag: String? = null,
    @SerializedName("hashTag")
    var hashTag: String? = null,
    @SerializedName("name")
    var name: String? = null
): Serializable {
    @SerializedName("keyword")
    var keyword: String? = null
    @SerializedName("url")
    var url: String? = null
    @Ignore
    @SerializedName("walls")
    var walls: List<WallpaperModel>? = null

    val topDown: Boolean get() = id == ID_TOP_DOWN
    val couple: Boolean get() = id == ID_COUPLE
    val topTrending: Boolean get() = hashtag == "toptrending"

    @JvmField
    @Transient
    @Ignore
    var isHistory : Boolean = false
    private fun getThumbUrl(): String? = this.url?.let { "thumbnails/$it" }

    private fun getMinThumbUrl(mUrl: String? = null): String? = (mUrl ?: url)?.let { "minthumbnails/$it" }

    fun toUrl(isMin: Boolean = true, isThumb: Boolean = true): String {
        val storageString: String = WallpaperURLBuilder.shared.getFullStorage(isMin)
        return when {
            isMin -> {
                storageString.plus(getMinThumbUrl())
            }
            isThumb -> {
                storageString.plus(getThumbUrl())
            }
            else -> storageString.plus(url)
        }
    }

    fun toUrl(position: Int, isMin: Boolean = true): String? {
        if (position >= walls?.size ?: 0) return null
        val url = walls?.get(position)?.url ?: return null
        val storage: String = WallpaperURLBuilder.shared.getFullStorage(isMin)
        return storage.plus(getMinThumbUrl(url))
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        const val ID_TOP_DOWN = -1L
        const val ID_COUPLE = -2L
        const val ID_TOP_NEW = -3L
        const val WORLD_TOP_DOWN = -4L
        const val TAG_TOP_DOWN = "topdown"
        const val TAG_TOP_TRENDING = "toptrending"
    }
}