package com.wifi.wificharger.utils

import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder

fun Int.convertToIpAddress(): String? {
    val ip = if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) Integer.reverseBytes(this) else this
    val bytes = BigInteger.valueOf(ip.toLong()).toByteArray()
    val address = InetAddress.getByAddress(bytes)
    return address.hostAddress
}