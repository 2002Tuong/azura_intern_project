package com.artgen.app.data.remote.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ArtGenResponse(
    @SerializedName("image_id")
    val imageId: String?,
    @SerializedName("output_image")
    val outputImage: String,
)
