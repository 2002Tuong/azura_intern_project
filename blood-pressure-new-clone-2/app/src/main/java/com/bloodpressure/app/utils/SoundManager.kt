package com.bloodpressure.app.utils

import android.content.Context
import android.media.MediaPlayer
import com.bloodpressure.app.R

class SoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
        private set

    fun startPlaying() {

        if (!isPlaying) {
            mediaPlayer = MediaPlayer.create(context, R.raw.steadyheartratemonitorloop1min)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun stopPlaying() {

        if (isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }
    }

    fun toggleSound() {
        if (isPlaying) {
            stopPlaying()
        } else {
            startPlaying()
        }
    }
}
