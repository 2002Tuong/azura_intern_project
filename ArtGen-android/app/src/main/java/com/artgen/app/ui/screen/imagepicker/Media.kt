package com.artgen.app.ui.screen.imagepicker

import android.net.Uri

data class Media(
    val albumName: String,
    val uri: Uri,
    val dateAddedSecond: Long,
)
