package com.slideshowmaker.slideshow.data.models

import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import java.io.Serializable

data class RenderImageSlideData(
    val imageList: ArrayList<String>,
    val bitmapHashMap:HashMap<String, String>,
    val videoQuality: Int,
    val delayTimeSec: Int,
    val themeData: ThemeData,
    val musicReturnData: MusicReturnInfo?,
    val gsTransition: GSTransition,
    val stickerAddedForRender : ArrayList<StickerForRenderInfo>
) :Serializable