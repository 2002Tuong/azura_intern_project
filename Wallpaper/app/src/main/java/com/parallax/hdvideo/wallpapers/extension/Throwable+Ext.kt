package com.parallax.hdvideo.wallpapers.extension

import com.parallax.hdvideo.wallpapers.data.model.FailedResponse
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils

fun Throwable.isOnline() : Boolean {
    val it = this as? FailedResponse ?: return NetworkUtils.isNetworkConnected()
    return it.online
}