package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName

class EventInfo {
    @SerializedName("id")
    var id: String = ""

    @SerializedName("events")
    var dataEvents = mutableListOf<NotificationModel>()
}