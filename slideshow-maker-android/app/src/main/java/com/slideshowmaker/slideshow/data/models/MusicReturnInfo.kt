package com.slideshowmaker.slideshow.data.models

import java.io.Serializable

data class MusicReturnInfo(
    val audioFilePath: String,
    var outFilePath: String = "",
    val startOffset: Int,
    val length: Int,
    val fileId: String = "",
    val fileName: String = ""
) : Serializable