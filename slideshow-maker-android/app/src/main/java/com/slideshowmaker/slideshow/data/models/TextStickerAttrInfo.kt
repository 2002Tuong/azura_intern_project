package com.slideshowmaker.slideshow.data.models

import com.slideshowmaker.slideshow.ui.custom.EditTextSticker

data class TextStickerAttrInfo(
    val text: String,
    val textColor: Int,
    val fontId: Int,
    val alignMode: EditTextSticker.AlignMode,
    val textStyle: Int,
    val flag: Int
)