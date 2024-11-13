package com.example.videoart.batterychargeranimation.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class Category(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
) : Parcelable
