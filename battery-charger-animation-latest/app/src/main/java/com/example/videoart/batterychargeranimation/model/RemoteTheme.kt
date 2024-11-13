package com.example.videoart.batterychargeranimation.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class RemoteTheme(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("font_family")
    val font: String,
    @SerializedName("text_size")
    val textSize: Int,
    @SerializedName("text_color")
    val textColor: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("free")
    val free: Boolean,
    @SerializedName("thumb_url")
    val thumbUrl: String,
    @SerializedName("sound_url")
    val soundUrl: String,
    @SerializedName("animatation_url")
    val animationUrl: String
    ):  Parcelable {
        fun toTheme(): Theme {
            return Theme(
                id = id,
                thumbnail = thumbUrl,
                animation = animationUrl,
                sound = soundUrl,
                fontId = font,
                category = categoryId,
                fromRemote = true
            )
        }
    }
