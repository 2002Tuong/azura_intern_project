package com.slideshowmaker.slideshow.data.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.utils.extentions.appVersionToInt

@Keep
data class VersionConfig(
    @SerializedName("status")
    val status: Int?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("max_version_show")
    val maxVersionShow: String?,
    @SerializedName("url_update")
    val url: String?,
) {
    val isForceUpdate
        get() = status == 2 && BuildConfig.VERSION_NAME.appVersionToInt() <= (
            maxVersionShow?.appVersionToInt()
                ?: Int.MAX_VALUE
            )
    val isNewUpdate
        get() = status == 1 && BuildConfig.VERSION_NAME.appVersionToInt() <= (
            maxVersionShow?.appVersionToInt()
                ?: Int.MAX_VALUE
            )
}
