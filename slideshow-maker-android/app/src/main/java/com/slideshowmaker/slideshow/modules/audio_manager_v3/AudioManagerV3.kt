package com.slideshowmaker.slideshow.modules.audio_manager_v3

import com.slideshowmaker.slideshow.data.models.MusicReturnInfo

interface AudioManagerV3 {

    fun getAudioName(): String
    fun playAudio()
    fun pauseAudio()
    fun returnToDefault(currentTimeMs: Int)
    fun seekTo(currentTimeMs: Int)
    fun repeat()
    fun setVolume(volume: Float)
    fun getVolume(): Float
    fun changeAudio(musicReturnData: MusicReturnInfo, currentTimeMs: Int)
    fun changeMusic(path: String)
    fun getOutMusicPath(): String
    fun getOutMusic(): MusicReturnInfo

    fun useDefault()


    fun isPlaying(): Boolean
}