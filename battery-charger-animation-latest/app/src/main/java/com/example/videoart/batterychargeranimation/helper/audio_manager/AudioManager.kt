package com.example.videoart.batterychargeranimation.helper.audio_manager

import com.example.videoart.batterychargeranimation.model.MusicReturnInfo

interface AudioManager {

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