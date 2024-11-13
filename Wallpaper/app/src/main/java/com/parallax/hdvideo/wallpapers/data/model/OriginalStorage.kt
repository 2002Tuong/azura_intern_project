package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName

class OriginalStorage {
    @SerializedName("lang")
    var language: String? = null
    @SerializedName("storageOrigin")
    var storageOrigin: String? = null
    @SerializedName("fullStorage")
    var fullStorage: String? = null
}