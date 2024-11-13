package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.ImageWithLookupInfo

class ImageWithLookupModel(private val mImageWithLookupData: ImageWithLookupInfo) {

    val imagePath
    get() = mImageWithLookupData.imagePath

    val id
    get() = mImageWithLookupData.id

    var lookupType = mImageWithLookupData.lookupType
}