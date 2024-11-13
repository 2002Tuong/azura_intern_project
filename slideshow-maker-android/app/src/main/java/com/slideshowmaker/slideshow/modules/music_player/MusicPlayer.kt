package com.slideshowmaker.slideshow.modules.music_player

import com.slideshowmaker.slideshow.data.models.MusicReturnInfo

interface MusicPlayer {

    fun play()
    fun pause()
    fun changeState()
    fun changeMusic(audioFilePath:String)
    fun changeMusic(audioFilePath:String, startOffset: Int, length: Int)
    fun seekTo(offset:Int)
    fun changeStartOffset(startOffset:Int)
    fun changeLength(length:Int)
    fun release()
    fun getOutMusic(): MusicReturnInfo
}