package com.slideshowmaker.slideshow.data.response

import android.net.Uri

data class MediaModel(
    val albumName: String,
    val uri: Uri,
    val dateAddedSecond: Long,
)
