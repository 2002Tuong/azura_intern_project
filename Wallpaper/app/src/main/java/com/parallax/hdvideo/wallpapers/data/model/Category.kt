package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import java.io.Serializable

open class Category(
    @SerializedName(value = "id")
    var id: String? = null,
    @SerializedName(value = "name")
    var name: String? = null,
    @SerializedName(value = "url")
    var url: String? = null
) : Serializable {

    @SerializedName(value = "count")
    var count: String? = null

    @SerializedName(value = "countries")
    var countries: String? = null

    val isVideo: Boolean get() = id == VIDEO_CATEGORY_ID
    var isTopTenDevice: Boolean = false
    var walls = mutableListOf<WallpaperModel>()

    open fun toUrl(isMin: Boolean = true, isThumb: Boolean = true): String {
        val storage = WallpaperURLBuilder.shared.getFullStorage(isMin)
        if (isMin) {
            return storage + "minthumbnails/$url"
        }
        return if (isThumb) {
            storage + "thumbnails/$url"
        } else storage.plus(url)
    }


    fun toWallpaperModel(): WallpaperModel {
        val model = WallpaperModel()
        model.name = this.name
        model.url = this.url
        model.id = this.id ?: ""
        return model
    }

    companion object {
        var VIDEO_CATEGORY_ID = "10000"
        const val CATEGORY_ID_ADVERTISE = "1999"
    }
}