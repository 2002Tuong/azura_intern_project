package com.slideshowmaker.slideshow.data.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.slideshowmaker.slideshow.utils.extentions.orFalse

@Keep
data class AppOpenAdsConfig(
    @SerializedName("enable")
    val enable: Boolean,
    @SerializedName("show_on_startup")
    val showOnStartUp: Boolean,
    @SerializedName("show_on_startup_first_launch")
    val showOnStartUpFirstLaunch: Boolean,
    @SerializedName("show_on_resume")
    val showOnResume: Boolean,
    @SerializedName("show_on_resume_first_launch")
    val showOnResumeFirstLaunch: Boolean,
    @SerializedName("type_show_open_ads")
    val adType: String?,
) {
    val showOnStartUpFirstLaunchEnabled: Boolean
        get() = enable.orFalse() && showOnStartUpFirstLaunch.orFalse()
    val showOnStartUpEnabled: Boolean
        get() = enable.orFalse() && showOnStartUp.orFalse()
    val showOnResumeEnabled: Boolean
        get() = enable.orFalse() && showOnResume.orFalse()
    val showOnResumeFirstLaunchEnabled: Boolean
        get() = enable.orFalse() && showOnResumeFirstLaunch.orFalse()

    val shouldShowInterAds: Boolean
        get() = adType?.equals(AD_TYPE_INTER, ignoreCase = true).orFalse()

    companion object {
        private const val AD_TYPE_INTER = "inter"
    }
}
