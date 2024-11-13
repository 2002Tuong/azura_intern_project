package com.slideshowmaker.slideshow.data.models

data class ThemeLinkInfo constructor(
    val link: String,
    val fileName: String,
    val thumb: String,
    val name: String,
    val isPro: Boolean,
    val id: String = "",
    val lottieFileName: String,
    val lottieLink: String
)