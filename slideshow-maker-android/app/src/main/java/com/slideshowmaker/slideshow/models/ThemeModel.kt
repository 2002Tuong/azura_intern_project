package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.modules.theme.data.ThemeData

class ThemeModel(val themeData: ThemeData) {

    val themeName = themeData.themeName
    val videoPath = themeData.themeVideoFilePath
    var isSelected = false

}