package com.parallax.hdvideo.wallpapers.utils.file

import java.io.File

fun File.lastName() : String {
    return try {
        path.substring(path.lastIndexOf("/") + 1)
    }catch (e: IndexOutOfBoundsException) {
        ""
    }
}