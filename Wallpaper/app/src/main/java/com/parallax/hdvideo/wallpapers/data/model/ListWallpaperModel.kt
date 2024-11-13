package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName

class ListWallpaperModel {
    @SerializedName("country")
    var country: String? = null
    @SerializedName("pageId")
    var pageId: String? = null
    @SerializedName("wallpapers")
    var wallpapers = listOf<WallpaperModel>()
    @SerializedName("relateHashtags")
    var listHashTag = listOf<HashTag>()
}