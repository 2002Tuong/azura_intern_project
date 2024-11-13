package com.slideshowmaker.slideshow.data.response

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AppConfig(@SerializedName("cdn_url") val cdnUrl: String?)
