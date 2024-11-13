package com.wifi.wificharger.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import com.thanosfisherman.wifiutils.WifiUtils
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.data.model.toWifiList
import com.wifi.wificharger.ui.main.MainActivity

@SuppressLint("MissingPermission")
fun MainActivity.scanWifi(
    onSuccess: (List<Wifi>) -> Unit,
    onFailure: (List<Wifi>) -> Unit,
) {
    requestLocationPermissionIfNeeded {
        if (it) {
            WifiUtils.withContext(this).enableWifi()
            val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager

            val wifiScanReceiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    if (success) {
                        onSuccess(wifiManager.scanResults.toWifiList())
                    } else {
                        onFailure(wifiManager.scanResults.toWifiList())
                    }
                }
            }

            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            registerReceiver(wifiScanReceiver, intentFilter)

            val success = wifiManager.startScan()
            if (!success) {
                // scan failure handling
                onFailure(wifiManager.scanResults.toWifiList())
            }
        }
    }
}

fun Context.getIpAddress(wifiInfo: WifiInfo?): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager is ConnectivityManager) {
            val linkAddresses = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
                ?.linkAddresses
            if (linkAddresses.isNullOrEmpty()) null
            else if (linkAddresses.size == 1) linkAddresses[0]?.address?.hostAddress
            else linkAddresses[1]?.address?.hostAddress
        } else ""
    } else @Suppress("DEPRECATION") {
        wifiInfo ?: return ""
        val ipAddressInt = wifiInfo.ipAddress
        val ipAddress: String? = Formatter.formatIpAddress(ipAddressInt)
        if (ipAddress.isNullOrBlank() || ipAddress.isEmpty()) {
            return String.format(
                "%d.%d.%d.%d", ipAddressInt and 0xff, ipAddressInt shr 8 and 0xff,
                ipAddressInt shr 16 and 0xff, ipAddressInt shr 24 and 0xff
            )
        }
        ""
    }
}