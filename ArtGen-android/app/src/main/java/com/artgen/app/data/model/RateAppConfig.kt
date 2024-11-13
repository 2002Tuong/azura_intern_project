package com.artgen.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.annotation.concurrent.Immutable

@Immutable
@Serializable
data class RateAppConfig(
    @SerialName("exit_app_rating_session")
    val exitAppRatingSession: List<Int> = emptyList(),
    @SerialName("save_picture_rating_session")
    val savePictureRatingSession: List<Int> = emptyList(),
)
