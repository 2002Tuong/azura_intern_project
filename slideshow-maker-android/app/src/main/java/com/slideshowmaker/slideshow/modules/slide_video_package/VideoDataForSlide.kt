package com.slideshowmaker.slideshow.modules.slide_video_package

import android.util.Size

class VideoDataForSlide(
    val path: String,
    var startOffset: Long,
    var endOffset: Long,
    var size: Size
)