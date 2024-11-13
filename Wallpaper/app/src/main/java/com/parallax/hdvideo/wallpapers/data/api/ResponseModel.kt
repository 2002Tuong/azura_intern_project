package com.parallax.hdvideo.wallpapers.data.api

import com.google.gson.annotations.SerializedName

class ResponseModel<T> constructor(@SerializedName("data")
                                    var data: MutableList<T> = mutableListOf()) {
    @SerializedName("message")
    var message: String? = null
    @SerializedName("status")
    var status: Int? = null
    @SerializedName("pageId")
    var pageId: String? = null
    @SerializedName("hasnext")
    var hasNext: Boolean = false
    @SerializedName("nextoffset")
    var nextoffset: Int? = null
}