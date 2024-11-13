package com.slideshowmaker.slideshow.data.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.slideshowmaker.slideshow.utils.extentions.orFalse

@Keep
data class InterstitialAdsConfigSetting(
    @SerializedName("enable")
    val enable: Boolean?,
    @SerializedName("share_video")
    val shareVideo: Boolean?,
    @SerializedName("create_slideshow")
    val createSlideshow: Boolean?,
    @SerializedName("edit_video")
    val editVideo: Boolean?,
    @SerializedName("my_video")
    val myVideo: Boolean?,
    @SerializedName("join_video")
    val joinVideo: Boolean?,
    @SerializedName("trim_video")
    val trimVideo: Boolean?,
) {

    val createSlideshowEnabled
        get() = enable.orFalse() && createSlideshow.orFalse()
    val myVideoEnabled
        get() = enable.orFalse() && myVideo.orFalse()

    val editVideoEnabled
        get() = enable.orFalse() && editVideo.orFalse()

    val joinVideoEnabled
        get() = enable.orFalse() && joinVideo.orFalse()

    val trimVideoEnabled
        get() = enable.orFalse() && trimVideo.orFalse()

    val shareVideoEnabled
        get() = enable.orFalse() && shareVideo.orFalse()

}
