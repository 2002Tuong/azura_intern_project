package com.example.videoart.batterychargeranimation.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class Theme(
    @SerializedName("id")
    val id: String="",
    @SerializedName("thumbnail")
    val thumbnail: String="",
    @SerializedName("animation")
    val animation: String="",
    @SerializedName("sound")
    val sound: String="",
    @SerializedName("font")
    val fontId: String="",
    @SerializedName("category")
    val category: String="",
    @SerializedName("from_remote")
    val fromRemote: Boolean = true
) : Parcelable
