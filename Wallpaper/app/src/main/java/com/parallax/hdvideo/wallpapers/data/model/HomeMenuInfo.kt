package com.parallax.hdvideo.wallpapers.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class HomeMenuInfo(val type: HomeMenuType,@DrawableRes val resIcon: Int,@StringRes val resTitle: Int)

enum class HomeMenuType {
    FEATURE, IMAGE_4D, LIVE, TOP_DOWNLOAD, TRENDING
}