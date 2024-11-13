package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.ImageInfo

class ImageDataModel {
    val filePath:String
    var count = 0
    val dateAdded:Long
    constructor(imageData: ImageInfo) {
        this.filePath = imageData.filePath
        this.dateAdded = imageData.dateAdded
    }

    constructor(dateAdded:Long) {
        this.dateAdded = dateAdded
        filePath = ""
    }
}