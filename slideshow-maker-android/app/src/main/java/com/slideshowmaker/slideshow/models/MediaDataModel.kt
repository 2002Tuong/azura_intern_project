package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.MediaInfo

class MediaDataModel(mMediaData: MediaInfo):Comparable<MediaDataModel> {

    val filePath = mMediaData.filePath
    val dateAdded = mMediaData.dateAdded
    var count = 0
    val kind = mMediaData.mediaKind
    val duration = mMediaData.duration
    override fun compareTo(other: MediaDataModel): Int = other.dateAdded.compareTo(dateAdded)
}