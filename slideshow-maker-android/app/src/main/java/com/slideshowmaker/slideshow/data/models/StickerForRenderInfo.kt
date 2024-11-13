package com.slideshowmaker.slideshow.data.models

import java.io.Serializable

data class StickerForRenderInfo(val stickerPath: String, val startOffset: Int, val endOffset: Int) : Serializable
