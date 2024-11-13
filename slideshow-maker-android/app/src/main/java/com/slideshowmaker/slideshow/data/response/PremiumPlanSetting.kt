package com.slideshowmaker.slideshow.data.response

import androidx.annotation.Keep
import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName

@Keep
data class PremiumPlan(
    @SerializedName("product_id")
    val productId: String?,
    @SerializedName("discount")
    val discount: String?,
    @SerializedName("is_best")
    val isBest: Boolean?,
    @SerializedName("index")
    val index: Int,
    @SerializedName("show")
    val show: Boolean,
    @SerializedName("type")
    val type: String,
) {
    val isSubscription: Boolean
        get() = type.equals(ProductType.SUBSCRIPTION, true)

    val isInApp: Boolean
        get() = type.equals(ProductType.LIFETIME, true)
}

@Retention(AnnotationRetention.SOURCE)
@StringDef(ProductType.SUBSCRIPTION, ProductType.LIFETIME)
annotation class ProductType {

    companion object {
        const val SUBSCRIPTION = "subscription"
        const val LIFETIME = "lifetime"
    }
}
