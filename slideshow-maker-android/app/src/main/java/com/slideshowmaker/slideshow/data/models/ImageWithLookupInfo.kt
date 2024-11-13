package com.slideshowmaker.slideshow.data.models

import com.slideshowmaker.slideshow.utils.ImageFilterHelper

class ImageWithLookupInfo(val id:Int, val imagePath:String, var lookupType: ImageFilterHelper.LookupType=ImageFilterHelper.LookupType.DEFAULT) {
}