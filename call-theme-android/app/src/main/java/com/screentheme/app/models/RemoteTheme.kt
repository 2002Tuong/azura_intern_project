package com.screentheme.app.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.screentheme.app.utils.extensions.generateUniqueId
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class RemoteTheme(
    @SerializedName("id")
    val id: String,
    @SerializedName("theme_name")
    val themeName: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("background")
    val background: String,
    @SerializedName("accept_call_icon")
    val acceptCallIcon: String,
    @SerializedName("decline_call_icon")
    val declineCallIcon: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("watch_ads")
    val watchAds: Boolean,
    @SerializedName("premium")
    val isPremium: Boolean
) : Parcelable

@Parcelize
data class ThemeConfig(
    val id: String = generateUniqueId(),
    val themeName: String = generateThemeName(),
    var background: String,
    var avatar: String,
    var acceptCallIcon: String,
    var declineCallIcon: String,
    var ringtone: String = "",
    val flashEnabled: Boolean = false
) : Parcelable {
    companion object {
        private fun generateThemeName(): String {
            val prefix = "Theme"
            val uniqueId = generateUniqueId().substring(0, 8)
            return "$prefix $uniqueId"
        }
    }
}