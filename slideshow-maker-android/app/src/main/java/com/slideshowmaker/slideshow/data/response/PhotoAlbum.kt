package com.slideshowmaker.slideshow.data.response

import android.net.Uri

data class PhotoAlbum(
    val id: String,
    val name: String,
    val thumbnailUri: Uri?,
    val imageCount: Int,
) {
    companion object {
        internal const val ID_ALL = "-1000L"
    }
}
