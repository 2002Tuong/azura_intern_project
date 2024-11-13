package com.artgen.app.data.model

data class CropAspectRatio(
    val title: String,
    val aspectRatio: AspectRatio = AspectRatio.Original,
    val iconRes: Int
)

data class AspectRatio(val value: Float) {
    companion object {
        val Original = AspectRatio(-1f)
    }
}