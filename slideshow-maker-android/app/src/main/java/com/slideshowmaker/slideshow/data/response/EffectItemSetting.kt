package com.slideshowmaker.slideshow.data.response

import com.google.gson.annotations.SerializedName

data class ItemEffect(
    @SerializedName("id") val id: String?,
    @SerializedName("is_pro") val isPro: Boolean?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?
)

data class ItemTransition(
    @SerializedName("id") val id: String?,
    @SerializedName("is_pro") val isPro: Boolean?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("watch_video_ads") val watchVideoAds: Boolean?,
)