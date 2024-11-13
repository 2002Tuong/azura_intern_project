package com.artgen.app.extension

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun Context.loadImageBitmapFromUri(uri: Uri): ImageBitmap {

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(this.contentResolver, uri)
            ) { decoder, info, source ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }.asImageBitmap()
    } catch (e: Exception) {
        // In case of any exception or unsupported URI, fall back to using MediaStore.getBitmap
        MediaStore.Images.Media.getBitmap(this.contentResolver, uri).asImageBitmap()
    }
}

fun Context.getDrawable(drawableName: String): Uri {
    val resId = resources.getIdentifier(drawableName, "drawable", packageName)
    return Uri.parse("android.resource://$packageName/$resId")
}