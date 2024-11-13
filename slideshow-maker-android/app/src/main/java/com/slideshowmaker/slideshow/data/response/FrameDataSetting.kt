package com.slideshowmaker.slideshow.data.response

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.utils.extentions.isUrl

@Keep
data class FrameDataSetting(
    @SerializedName("name")
    val name: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("pack_link")
    val packLink: String?,
    @SerializedName("ratio_extension")
    val ratioExtension: String?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("is_pro")
    val isPro: Boolean?,
) {
    val thumbnailUrl
        get() = if (thumbnail.isUrl()) thumbnail.orEmpty() else RemoteConfigRepository.appConfig?.cdnUrl + thumbnail

    val frameLink
        get() = if (link.isUrl()) link.orEmpty() else RemoteConfigRepository.appConfig?.cdnUrl + link

    val framePackLink
        get() = if (packLink.isNullOrBlank() || packLink.isUrl()) packLink.orEmpty() else RemoteConfigRepository.appConfig?.cdnUrl + packLink

    val frameFileName
        get() = frameLink.substringAfterLast("/")
}