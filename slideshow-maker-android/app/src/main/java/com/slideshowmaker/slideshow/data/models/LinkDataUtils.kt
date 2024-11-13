package com.slideshowmaker.slideshow.data.models

object LinkDataUtils {

    const val DEFAULT_RATIO_EXTENSION = ".png"

    fun getRatioStr(ratio: Float): String {
        return when (ratio) {
            16f / 9f -> "_16_9"
            9f / 16f -> "_9_16"
            else -> "_1_1"
        }
    }
}
