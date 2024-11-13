package com.artgen.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtStyle(
    @SerialName("name")
    val name: String,
    @SerialName("style_id")
    val styleId: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String,
    @SerialName("original_url")
    val originalUrl: String?,
    @SerialName("styled_url")
    val styledUrl: String?,
)
