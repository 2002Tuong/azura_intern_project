package com.parallax.hdvideo.wallpapers.utils.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build
import androidx.lifecycle.LiveData
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import java.lang.ref.WeakReference

class NetworkListener: LiveData<Boolean>() {

    private var weakReferenceObject: WeakReference<Context>? = WeakReference(WallpaperApp.instance)
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            postValue(true)
        }
        override fun onLost(network: Network) {
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) request()
        else {
            updateConnection()
            weakReferenceObject?.get()?.registerReceiver(networkReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        }
    }

    override fun onInactive() {
        super.onInactive()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppConfiguration.connectivityManagerInstance.unregisterNetworkCallback(callback)
        } else {
            weakReferenceObject?.get()?.unregisterReceiver(networkReceiver)
        }
        weakReferenceObject?.clear()
        weakReferenceObject = null
    }

    private fun request() {
        val networkBuilder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        AppConfiguration.connectivityManagerInstance.registerNetworkCallback(networkBuilder.build(), callback)
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun updateConnection() {
        val activeNetwork: NetworkInfo? = AppConfiguration.connectivityManagerInstance.activeNetworkInfo
        postValue(activeNetwork?.isConnectedOrConnecting == true)
    }
}