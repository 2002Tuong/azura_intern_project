package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.VideoInfo

class VideoModel {
    val filePath:String
    var count = 0
    val dateAdded:Long
    val duration:Long
    constructor(videoData: VideoInfo) {
        this.filePath = videoData.path
        this.dateAdded = videoData.dateAdded
        this.duration = videoData.duration
    }

    constructor(dateAdded:Long) {
        this.dateAdded = dateAdded
        filePath = ""
        duration = 0
    }

}