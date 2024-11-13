package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class ConfigInfo {

    @SerializedName(value = "commonInfo")
    var commonData: CommonData? = null

    companion object {
        fun newInstance(json: String): ConfigInfo{
            return Gson().fromJson(json, ConfigInfo::class.java)
        }
    }
}