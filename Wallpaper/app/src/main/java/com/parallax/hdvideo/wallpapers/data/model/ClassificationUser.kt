package com.parallax.hdvideo.wallpapers.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClassificationUser(
    @SerializedName("topCountry")
    @Expose
    var topCountry: String = "",
    @SerializedName("topDevice")
    @Expose
    val topDevice: String = ""
) : Parcelable