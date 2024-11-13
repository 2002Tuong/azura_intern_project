package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.RecordedInfo

class RecordedModel(private val recordedData: RecordedInfo) {
    val path = recordedData.recordFilePath
    var isSelected = false
    val startOffset = recordedData.startMs
    val endOffset = recordedData.endMs
    fun checkTime(timeMs:Int):Boolean {
        if(timeMs >= recordedData.startMs && timeMs <= recordedData.endMs) return true
        return false
    }
}