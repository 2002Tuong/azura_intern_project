package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.FontInfo

class FontModel (fontInfo: FontInfo) {
    val name = fontInfo.fontName
    val id = fontInfo.fontId
}