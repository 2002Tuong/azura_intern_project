package com.parallax.hdvideo.wallpapers.utils.other

import android.os.Build

enum class MANUFACTURER {
    VIVO, HUAWEI, XIAOMI, OPPO, OTHER
}

    fun getManufacturer(): MANUFACTURER {
        return when {
            Build.MANUFACTURER.contains("vivo", true) -> {
                MANUFACTURER.VIVO
            }
            Build.MANUFACTURER.contains("huawei", true) -> {
                MANUFACTURER.HUAWEI
            }
            Build.MANUFACTURER.contains("xiaomi", true) -> {
                MANUFACTURER.XIAOMI
            }
            Build.MANUFACTURER.contains("oppo", true) -> {
                MANUFACTURER.OPPO
            }
            else -> MANUFACTURER.OTHER
        }
    }