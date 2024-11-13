package com.screentheme.app.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import com.google.gson.annotations.SerializedName
import com.screentheme.app.utils.extensions.generateUniqueId
import java.util.UUID

@Parcelize
@Keep
data class AvatarModel(
    @SerializedName("id")
    val id: String = generateUniqueId(),
    @SerializedName("avatar")
    val avatar: String
) : Parcelable
