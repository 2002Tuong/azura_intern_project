package com.slideshowmaker.slideshow.data.response

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SuggestedAppConfig(
    @SerializedName("package_id")
    val packageId: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("logo_url")
    val logoUrl: String?
)