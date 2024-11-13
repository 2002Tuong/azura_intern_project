package com.slideshowmaker.slideshow.data.response

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ItemAudio(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    @SerializedName("audio_url")
    val audioUrl: String?
)