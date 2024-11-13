package com.slideshowmaker.slideshow.data.models

data class AudioInfo(
    val filePath: String,
    val musicName: String,
    val mineType: String,
    val duration: Long,
    val fileId: String,
    val thumbnail:String = ""
)