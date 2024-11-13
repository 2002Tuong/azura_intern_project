package com.wifi.wificharger.data.model

import android.net.wifi.ScanResult
import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wifi.wificharger.R
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.content.QRContent

@Entity(tableName = "wifi")
data class Wifi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @DrawableRes
    val icon: Int = R.drawable.wifi_icon,
    val password: String = "",
    val securityType: String = "",
    val signalStrength: Int = 0,
    val rewardedEarn: Boolean = false
)

data class ConnectedWifi(
    val ipAddress: String?
)

fun List<ScanResult>.toWifiList(): List<Wifi> {
    return filterNot {
        it.SSID.isNullOrEmpty()
    }.map {
        it.mapToWifi()
    }
}

fun ScanResult.mapToWifi(): Wifi {
    return Wifi(
        name = this.SSID,
        securityType = when {
            capabilities.isNullOrEmpty() -> ""
            capabilities.contains("WPS") -> "Encrypted (WPS available)"
            else -> "Encrypted"
        },
        signalStrength = getWifiSignalStrength(level)
    )
}

fun QRResult.QRSuccess.mapToWifi(): Wifi? {
    val data = this.content as? QRContent.Wifi ?: return null
    return Wifi(
        name = data.ssid,
        password = data.password
    )
}

fun getWifiSignalStrength(rssi: Int): Int {
    val MIN_RSSI = -100
    val MAX_RSSI = -55
    val levels = 101
    return if (rssi <= MIN_RSSI) {
        0
    } else if (rssi >= MAX_RSSI) {
        levels - 1
    } else {
        val inputRange = (MAX_RSSI - MIN_RSSI).toFloat()
        val outputRange = (levels - 1).toFloat()
        ((rssi - MIN_RSSI).toFloat() * outputRange / inputRange).toInt()
    }
}
