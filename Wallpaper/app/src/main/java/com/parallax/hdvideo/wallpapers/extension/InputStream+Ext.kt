package com.parallax.hdvideo.wallpapers.extension

import com.parallax.hdvideo.wallpapers.WallpaperApp
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8) : String? {
    return bufferedReader(charset).use { it.readText() }
}

fun InputStream.readLineAndClose(charset: Charset = Charsets.UTF_8) : List<String> {
    return bufferedReader(charset).use { it.readLines() }
}

fun getStringFromAssets(name: String) : String? {
    return try {
        WallpaperApp.instance.run { assets.open(name).readTextAndClose() }
    }catch (e: IOException) {
        null
    }
}