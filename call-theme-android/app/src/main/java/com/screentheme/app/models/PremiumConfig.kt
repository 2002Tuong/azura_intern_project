package com.screentheme.app.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PremiumConfig(
    @SerializedName("id")
    val id: String,
    @SerializedName("show")
    val show: Boolean,
    @SerializedName("discount")
    val discount: String,
    @SerializedName("is_best")
    val isBest: Boolean?,
    @SerializedName("index")
    val index: Int,
    @SerializedName("type")
    val type: String
) {
    val isSubscription: Boolean
        get() = type?.equals("subscription", ignoreCase = true) == true
}
