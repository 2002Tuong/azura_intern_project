package com.slideshowmaker.slideshow.data.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.slideshowmaker.slideshow.utils.extentions.orFalse

@Keep
data class NativeAdsConfig(
    @SerializedName("enable")
    val enable: Boolean?,
    @SerializedName("home_banner")
    val homeBanner: Boolean?,
    @SerializedName("export")
    val export: Boolean?,
    @SerializedName("media_picker")
    val mediaPicker: Boolean?,
    @SerializedName("my_video")
    val myVideo: Boolean?,
    @SerializedName("bottom")
    val bottom: Boolean?,
    @SerializedName("cta_top_language")
    val isCtaTopLanguage: Boolean = false,
    @SerializedName("cta_top_onboard")
    val isCtaTopOnboard: Boolean = false
) {
    val homeBannerEnabled: Boolean
        get() = enable.orFalse() && homeBanner.orFalse()

    val exportEnabled: Boolean
        get() = enable.orFalse() && export.orFalse()

    val mediaPickerEnabled: Boolean
        get() = enable.orFalse() && mediaPicker.orFalse()

    val myVideoEnabled: Boolean
        get() = enable.orFalse() && myVideo.orFalse()
    val bottomEnabled: Boolean
        get() = enable.orFalse() && bottom.orFalse()
}
