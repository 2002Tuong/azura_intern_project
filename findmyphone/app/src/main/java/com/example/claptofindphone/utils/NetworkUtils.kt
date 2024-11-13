package com.example.claptofindphone.utils

import android.content.Context
import android.net.ConnectivityManager
import com.example.claptofindphone.presenter.ClapToFindApplication


object NetworkUtils {

    fun isOnline(context: Context): Boolean {
        val connectivityManagerInstance =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfoValue = if (connectivityManagerInstance != null) {
            connectivityManagerInstance.activeNetworkInfo
        } else null
        return activeNetworkInfoValue != null && activeNetworkInfoValue.isConnected
    }

    fun isInternetAvailable(): Boolean {
        return isOnline(ClapToFindApplication.getContext())

    }
}