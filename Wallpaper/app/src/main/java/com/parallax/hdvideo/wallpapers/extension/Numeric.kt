package com.parallax.hdvideo.wallpapers.extension

fun Double.round(decimals: Int): Double {
    var multiple = 1.0
    repeat(decimals) { multiple *= 10 }
    return ( kotlin.math.round(x = this * multiple)) / multiple
}

fun Int.toBoolean() : Boolean {
    return this == 1
}

fun Float.round(decimals: Int): Float = this.toDouble().round(decimals).toFloat()