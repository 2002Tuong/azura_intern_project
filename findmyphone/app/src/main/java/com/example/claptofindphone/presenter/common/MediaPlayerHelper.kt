package com.example.claptofindphone.presenter.common

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

interface AudioStatus {
    fun onDuringPlaying()
    fun onAudioFinish()
}

@Singleton
class MediaPlayerHelper @Inject constructor(
    @ApplicationContext var mContext: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var audioManagerInstance = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    private var isCalledStopMusicFromOutSide = false

    companion object {
        var isCallToStartFromService = false
    }

    fun initSound(sound: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(mContext, sound)
            mediaPlayer!!.isLooping = true
        }
    }

    fun setVolume(volume: Int) {
        audioManagerInstance?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    fun getMaxAudioVolume(): Int {
        return audioManagerInstance!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun playSound(
        duration: Int,
        volume: Int,
        audioStatus: AudioStatus,
        isPlayFromService: Boolean
    ) {
        isCallToStartFromService = isPlayFromService
        GlobalScope.launch(Dispatchers.IO) {
            isCalledStopMusicFromOutSide = false
            setVolume(volume)
            if (mediaPlayer != null) {
                mediaPlayer!!.start()
                val startTimeInMillis = System.currentTimeMillis()
                while (!isCalledStopMusicFromOutSide && System.currentTimeMillis() - startTimeInMillis < duration * 1000) {
                    audioStatus.onDuringPlaying()
                }
                // if press back to close the current fragment, the thread which is playing the audio is still running, after that,
                // call the call back to update view. But at this time, the view is destroyed
                // if service or fragment call stop sound, no need to call stop here
                if (!isCalledStopMusicFromOutSide) {
                    if (mediaPlayer != null) {
                        audioStatus.onAudioFinish()
                        stopSound(audioStatus)
                    }
                }
            }
        }
    }

    fun pauseSound() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun stopSound(audioStatus: AudioStatus?) {
        try {
            isCalledStopMusicFromOutSide = true
            isCallToStartFromService = false
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                audioStatus?.onAudioFinish()
            }
            mediaPlayer = null
        } catch (exception: IllegalStateException) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }

    }
}