package com.bloodpressure.app.screen.updateapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    @SerialName("max_version_show")
    val maxVersionShow: String?,
    @SerialName("status")
    val status: Int,
    @SerialName("title")
    val title: String?,
    @SerialName("message")
    val message: String?,
    @SerialName("url_update")
    val updateUrl: String?
)
