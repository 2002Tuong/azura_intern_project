package com.slideshowmaker.slideshow.modules.image_slide_show.drawer

import com.slideshowmaker.slideshow.utils.ImageFilterHelper
import java.io.Serializable

class ImageSlideData constructor(
    val slideId: Long,
    val fromImagePath: String,
    val toImagePath: String,
    var lookupType: ImageFilterHelper.LookupType = ImageFilterHelper.LookupType.DEFAULT
) : Serializable