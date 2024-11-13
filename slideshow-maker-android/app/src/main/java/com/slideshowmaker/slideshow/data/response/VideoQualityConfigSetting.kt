package com.slideshowmaker.slideshow.data.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VideoQualityConfigSetting(
    @SerializedName("quality")
    val quality: String,
    @SerializedName("status_show")
    val statusShow: Boolean,
    @SerializedName("type_ads")
    val typeAds: String,
    @SerializedName("is_default")
    val isDefault: Boolean
)