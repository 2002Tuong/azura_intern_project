package com.slideshowmaker.slideshow.data.response

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.utils.extentions.isUrl

@Keep
data class ThemeConfig(
    @SerializedName("name") val name: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("video") val video: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("is_pro") val isPro: Boolean?,
    @SerializedName("lottie") val lottie: String?,
) {
    val thumbnailUrl
        get() = if (thumbnail.isUrl()) thumbnail.orEmpty() else RemoteConfigRepository.appConfig?.cdnUrl + thumbnail
    val videoUrl
        get() = if (video.isUrl()) video.orEmpty() else RemoteConfigRepository.appConfig?.cdnUrl + video
    val lottieUrl
        get() = if (lottie.isUrl()) lottie.orEmpty() else RemoteConfigRepository.appConfig?.cdnUrl + lottie
    val fileName
        get() = videoUrl.split("/").last()
    val lottieFileName
        get() = lottieUrl.substringAfterLast("/")
}