package com.slideshowmaker.slideshow.modules.theme.data

import java.io.Serializable

class ThemeData constructor(
    val themeVideoFilePath: String = "none",
    val themeType: ThemType = ThemType.NOT_REPEAT,
    val themeName: String = "None",
    val isPro: Boolean = false,
    val id: String = "",
    val lottieFileName: String = "none",
    val lottieLink: String = "none"
) : Serializable {

    enum class ThemType {
        REPEAT, NOT_REPEAT
    }
}