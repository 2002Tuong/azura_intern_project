package com.artgen.app.data.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class PremiumConfig(
    @SerialName("product_id")
    val id: String,
    @SerialName("show")
    val show: Boolean,
    @SerialName("discount")
    val discount: String?,
    @SerialName("is_best")
    val isBest: Boolean?,
    @SerialName("index")
    val index: String?,
    @SerialName("type")
    val type: String?
) {
    val isSubscription: Boolean
        get() = type?.equals("subscription", ignoreCase = true) == true
}
