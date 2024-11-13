package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName

class ServerInfoModel {
    @SerializedName("ServerInfo")
    var ServerInfo = mutableListOf<ListWallpaperModel>()
}