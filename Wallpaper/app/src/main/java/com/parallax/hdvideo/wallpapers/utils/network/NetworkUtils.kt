package com.parallax.hdvideo.wallpapers.utils.network

import android.net.NetworkCapabilities
import android.os.Build
import com.parallax.hdvideo.wallpapers.data.model.FailedResponse
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus


object NetworkUtils {

    @JvmStatic
    @Suppress("DEPRECATION")
    fun isNetworkConnected(): Boolean {
        val cm = AppConfiguration.connectivityManagerInstance
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = cm.activeNetwork ?: return false
            val nc = cm.getNetworkCapabilities(networkCapabilities) ?: return false
            when {
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetwork = cm.activeNetworkInfo
            activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }
    }

    fun pushNotification(isOnline: Boolean) {
        RxBus.push(FailedResponse(-1, null, null, online = isOnline, url = ""))
    }

    fun notifyOffline() {
        if (!isNetworkConnected())
            RxBus.push(FailedResponse(-1, null, null, online = false, url = ""))
    }

}