package com.screentheme.app.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.screentheme.app.utils.extensions.generateUniqueId
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
@Keep
data class BackgroundModel(
    @SerializedName("id")
    val id: String = generateUniqueId(),
    @SerializedName("background")
    val background: String
) : Parcelable
