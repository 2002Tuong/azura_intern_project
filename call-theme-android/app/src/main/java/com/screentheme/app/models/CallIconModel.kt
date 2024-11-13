package com.screentheme.app.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.screentheme.app.utils.extensions.generateUniqueId
import java.util.UUID

@Keep
data class CallIconModel (
    @SerializedName("id")
    val id: String = generateUniqueId(),
    @SerializedName("accept_call_icon")
    val accept_call_icon: String,
    @SerializedName("decline_call_icon")
    val decline_call_icon: String
)