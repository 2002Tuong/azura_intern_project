package com.slideshowmaker.slideshow.data.models

data class VideoInfo(
    val path: String,
    val dateAdded: Long,
    val duration: Long,
    val folderContainId: String,
    val folderContainName: String
)