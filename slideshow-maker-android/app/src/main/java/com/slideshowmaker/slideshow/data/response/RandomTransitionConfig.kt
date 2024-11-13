package com.slideshowmaker.slideshow.data.response

import com.google.gson.annotations.SerializedName

data class RandomTransitionConfig(
    @SerializedName("random_1")
    val random1: RandomTransition? = null,
    @SerializedName("random_2")
    val random2: RandomTransition? = null
)

data class RandomTransition(
    @SerializedName("ids")
    val ids: List<String>? = null,
    @SerializedName("is_pro")
    val isPro: Boolean = false
)