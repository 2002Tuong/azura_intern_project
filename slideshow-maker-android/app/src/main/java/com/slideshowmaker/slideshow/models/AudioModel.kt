package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.utils.Utils

class AudioModel(private val audioInfo: AudioInfo) {

    val name: String
        get() = audioInfo.musicName

    val durationInString: String
        get() = Utils.convertSecToTimeString(audioInfo.duration.toInt())

    val duration: Long
        get() = audioInfo.duration * 1000L

    var isSelected = false

    val filePath: String
        get() = audioInfo.filePath

    var startOffset = 0
    var length = audioInfo.duration * 1000L

    var isPlaying = false

    val fileType: String = audioInfo.mineType

    var isDownloaded = false

    var fileId: String = audioInfo.fileId

    var thumbnailUrl: String = audioInfo.thumbnail

/*
    init {
        val regex = "[a-zA-z]+/([a-zA-Z0-9]+)".toRegex()
        val result = regex.find(audioData.mineType)
        fileType = if(result != null) {
            if(result.groupValues[1].toLowerCase() == "mpeg") {
                "mp4"
            } else {
                result.groupValues[1]
            }
        } else {
            "mp3"
        }
    }
*/

    fun reset() {
        startOffset = 0
        length = duration
    }

}