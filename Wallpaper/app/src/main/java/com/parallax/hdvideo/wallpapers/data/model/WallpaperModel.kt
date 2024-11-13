package com.parallax.hdvideo.wallpapers.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration.INDEX_OF_ASPECT_RATIO
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration.RATIO_SCREEN_DATA
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.io.Serializable
import java.lang.reflect.Type

@Entity(tableName = "Wallpaper")
class WallpaperModel: Serializable {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    var id: String = ""

    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String? = null

    @ColumnInfo(name = "url")
    @SerializedName("url")
    var url: String? = null

    @ColumnInfo(name = "hashTag")
    @SerializedName("hashTag")
    var hashTag: String? = null

    @ColumnInfo(name = "categories")
    @SerializedName("categories")
    var categories: String? = null

    @ColumnInfo(name = "imgSize")
    @SerializedName("imgSize")
    var imgSize: String? = null

    @ColumnInfo(name = "count")
    @SerializedName("paralcount")
    var count: String? = null

    @ColumnInfo(name = "lang")
    @SerializedName("lang")
    var lang: String? = null

    @ColumnInfo(name = "hasShownRewardAd")
    @SerializedName("hasShownRewardAd")
    var hasShownRewardAd = false

    @ColumnInfo(name = "hasDownloaded")
    @SerializedName("hasDownloaded")
    var hasDownloaded = false

    @ColumnInfo(name = "isFavorite")
    @SerializedName("isFavorite")
    var isFavorite = false

    @ColumnInfo(name = "isPlaylist")
    @SerializedName("isPlaylist")
    var isPlaylist = false

    @ColumnInfo(name = "lastUpdated")
    @SerializedName("lastUpdated")
    var lastUpdated: Long = 0

    @ColumnInfo(name = "screenRatio")
    @SerializedName("screenRatio")
    var screenRatio = 0

    @ColumnInfo(name = "contentType")
    @SerializedName("contentType")
    var contentType: String? = null

    @ColumnInfo(name = "configParam")
    @SerializedName("configParam")
    var configParam: String? = null

    @Transient
    @Ignore
    var isVideoInternal: Boolean? = null

    @SerializedName("vipNumber")
    @ColumnInfo(name = "vipNumber")
    var vipNumber = 0

    @Transient
    @Ignore
    var listHashTags: List<HashTag>? = null

    val isVideo: Boolean get() = isVideoInternal ?: url?.let {u ->
         (u.endsWith(".mp4").or(u.contains("video"))).also { isVideoInternal = it }
    } ?: false

    val hasVideoFile: Boolean get() = pathCacheFullVideo != null

    @Transient
    @Ignore
    var pathCache: String? = null

    @Transient
    @Ignore
    var pathCacheFull: String? = null

    @Transient
    @Ignore
    var pathCacheVideo: String? = null

    @Transient
    @Ignore
    var pathCacheFullVideo: String? = null

    @Transient
    @Ignore
    var pathCacheParallaxPath: String? = null

    @Transient
    @Ignore
    var isFetchAndUnZip: Boolean = false

    @Transient
    @Ignore
    var soundIcon: Int = R.drawable.ic_no_sound_re

    @Transient
    @Ignore
    private var internalContainSound: Boolean? = null

    @ColumnInfo(name = "isTopDevices")
    @SerializedName("isTopDevices")
    var isTopDevices = false

    val isContainSound: Boolean get() = internalContainSound ?: url?.let {ur ->
        ur.endsWith("_Sound.mp4").also { internalContainSound = it }
    } ?: false

    val isMuted: Boolean get() = soundIcon != R.drawable.ic_sound_re

    @Transient
    @Ignore
    private var thumbUrlInternal: String? = null
    private val thumbUrl: String get() = thumbUrlInternal ?: (
            if (isVideo) {
                newUrl("/thumbs")
            } else
                "thumbnails/" + newUrl()
            ).also { thumbUrlInternal = it }

    @JvmField
    @Ignore
    var minThumbUrlInternal:String? = null
    private val minThumbUrl: String get() = minThumbUrlInternal ?: (
            if (isVideo) {
                newUrl("/thumbs").replace(".mp4", ".jpg")
            } else
                "minthumbnails/" + newUrl()
            ).also { minThumbUrlInternal = it }

    fun toUrl(isMin: Boolean = true, isThumb: Boolean = true, isVideo: Boolean = false, isDownloadingFullVideo: Boolean = false): String {
//        return if(isFromLocalStorage) {
//            url!!
//        }
//        else {
//            val storage: String = if (this.isVideo) {
//                if (isDownloadingFullVideo) {
//                    WallpaperApp.instance.localStorage.bestStoreVideoDownload ?: WallpaperURLBuilder.shared.videoStorage
//                } else WallpaperURLBuilder.shared.videoStorage
//            } else WallpaperURLBuilder.shared.getFullStorage(isMin)
//            return when {
//                is4DImage -> {
//                    val url = WallpaperURLBuilder.shared.getFullStorage(isMin).plus("parallax/").plus(url)
//                        .plus("thumb.jpg")
//                    Logger.d("parallax url", url)
//                    url
//                }
//                isMin || (this.isVideo && !isVideo) -> {
//                    storage.plus(minThumbUrl)
//                }
//                isThumb -> {
//                    storage.plus(thumbUrl)
//                }
//                else -> {
//                    storage.plus(newUrl())
//                }
//            }
//        }
        return "file:///android_asset/" + when {
            this.isVideo && !isVideo -> "thumbs/" + url?.replace(".mp4", ".jpeg")
            this.isVideo -> "video/$url"
            else -> "image/$url"
        }
    }

    val is4DImage: Boolean get() = this.count != null && this.count!!.toInt() > 1

    /**
     * size is string "/thumbs" or "/minthumbnails" or ""
     */
    private fun newUrl(size: String = "") : String {
        val newUrl = url ?: return ""
        var index = INDEX_OF_ASPECT_RATIO
        var text = ""
        if (index > 0 && screenRatio > 1) {
            if (index > screenRatio - 1) {
                index = screenRatio - 1
            }
            text = RATIO_SCREEN_DATA[index]
        }
        val last = newUrl.lastIndexOf('/')
        if (last > 0) {
            return newUrl.substring(0, last) + size + text + newUrl.substring(last)
        }
        return newUrl
    }

    val canCropImage: Boolean get() {
        return if (isTopDevices) false
        else !isVideo && screenRatio < 2 && INDEX_OF_ASPECT_RATIO > 0
    }
    @Transient
    @Ignore
    private var minThumbUrlFail: String? = null
    @Transient
    @Ignore
    private var thumbUrlFail: String? = null

    fun getWallpaperModelType() : WallpaperModelType{
        return when {
            is4DImage ->  WallpaperModelType.PARALLAX
            isVideo -> WallpaperModelType.VIDEO
            else -> WallpaperModelType.IMAGE
        }
    }

    fun getUrlFailMin(fromUrl: String) : String {
        return minThumbUrlFail ?: WallpaperURLBuilder.shared.getUrlFail(fromUrl, isVideo).also { minThumbUrlFail = it }
    }

    fun getUrlFailThumb(fromUrl: String) : String {
        return thumbUrlFail ?: WallpaperURLBuilder.shared.getUrlFail(fromUrl, isVideo).also { thumbUrlFail = it }
    }

    fun invert() : WallpaperModel {
        val model = WallpaperModel()
        model.id = "r_" + this.id
        model.name = this.name
        model.hashTag = this.hashTag
        model.url = url?.replace("_Left.", "_Right.")
        model.categories = this.categories
        model.imgSize = this.imgSize
        model.count = this.count
        model.lang = this.lang
        model.isTopDevices = this.isTopDevices
        return model
    }

    val realId get() = id.replaceFirst("r_", "")

    @Transient
    @Ignore
    private var internalCheckFromLocalFile: Boolean? = null

    val isFromLocalStorage: Boolean get() {
        return internalCheckFromLocalFile ?: url?.let { u ->
            u.contains(".mp4").also { internalCheckFromLocalFile = it }
        } ?: false
    }

    override fun equals(other: Any?): Boolean {
        return if (other != null && other is WallpaperModel) {
            url == other.url
        } else false
    }

    companion object {
        val listType: Type get() = object : TypeToken<List<WallpaperModel>>() {}.type
        const val NORMAL_CONTENT = 0
    }

    enum class WallpaperModelType {
        IMAGE, VIDEO, PARALLAX;
    }

}