package com.artgen.app.ui.screen.imagepicker

import android.net.Uri

data class ImageAlbum(
    val id: String,
    val name: String,
    val thumbnailUri: Uri?,
    val imageCount: Int,
) {
    companion object {
        internal const val ID_ALL = "-1000L"
    }
}
