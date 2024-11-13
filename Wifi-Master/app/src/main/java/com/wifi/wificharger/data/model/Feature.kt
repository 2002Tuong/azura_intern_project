package com.wifi.wificharger.data.model

import androidx.annotation.DrawableRes

data class Feature(
    val name: FeatureTitle,
    @DrawableRes
    val icon: Int
)

enum class FeatureTitle(val title: String) {
    CONNECT_WIFI("Connect Wifi"),
    SHOW_PASSWORD_SAVED_WIFI("Show Password"),
    WIFI_INFO("Wi-Fi Info"),
    GENERATE_PASSWORD("Generate Password"),
    WIFI_HOTSPOT("Wifi hotspot"),
    SCAN_QR("Scan QR"),
    SIGNAL_STRENGTH("Signal Strength"),
    SPEED_TEST("Speed Test"),
    CONNECTED_DEVICES("Connected Devices"),
}