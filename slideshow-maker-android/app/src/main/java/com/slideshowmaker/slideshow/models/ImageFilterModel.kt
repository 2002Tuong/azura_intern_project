package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.ImageFilterInfo

class ImageFilterModel(private val mImageFilterData: ImageFilterInfo) {
    val imgFilterName
    get() = mImageFilterData.name

    val lookupType
    get() = mImageFilterData.lookupType
}