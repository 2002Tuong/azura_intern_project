package com.example.videoart.batterychargeranimation.helper.audio_manager

import android.net.Uri
import com.example.videoart.batterychargeranimation.App
import com.example.videoart.batterychargeranimation.helper.FileHelper
import com.example.videoart.batterychargeranimation.helper.MediaHelper
import com.example.videoart.batterychargeranimation.model.MusicReturnInfo
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

import java.io.File

class AudioManagerImpl : AudioManager {

    private var audioName = "none"
    private var mediaPlayer =
        SimpleExoPlayer.Builder(App.instance.applicationContext).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
    private val defaultBandwidthMeter =
        DefaultBandwidthMeter.Builder(App.instance.applicationContext).build()
    private val defaultDataSourceFactory = DefaultDataSourceFactory(
        App.instance.applicationContext,
        "video-maker-v4",
        defaultBandwidthMeter
    )
    private var length = 0
    private var startOffset = 0
    private var currentVolume = 1f
    private var currentAudioData: MusicReturnInfo? = null
    private var musicPath = ""

    init {

        mediaPlayer.addListener(object : Player.Listener {


            override fun onLoadingChanged(isLoading: Boolean) {

            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

            }
        })
    }

    override fun getAudioName(): String = audioName

    override fun playAudio() {
        mediaPlayer.playWhenReady = true
    }

    override fun pauseAudio() {
        mediaPlayer.playWhenReady = false
    }

    override fun returnToDefault(currentTimeMs: Int) {
        audioName = "none"
        musicPath = ""
        val progressiveMediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.fromFile(File(""))))
        mediaPlayer.prepare(progressiveMediaSource)
    }

    override fun seekTo(currentTimeMs: Int) {
        try {
            mediaPlayer.seekTo((currentTimeMs % length).toLong())
        } catch (e: Exception) {
            mediaPlayer.seekTo(0L)
        }

    }

    override fun repeat() {
        mediaPlayer.seekTo(0)
    }

    override fun setVolume(volume: Float) {
        currentVolume = volume
        mediaPlayer.setVolume(volume)
    }

    override fun getVolume(): Float = currentVolume

    override fun changeAudio(musicReturnData: MusicReturnInfo, currentTimeMs: Int) {
        val isAutoPlay = mediaPlayer.isPlaying
        currentAudioData = musicReturnData
        audioName = File(musicReturnData.audioFilePath).name
        length = musicReturnData.length
        musicPath = musicReturnData.outFilePath
        val mediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.fromFile(File(musicReturnData.outFilePath))))
        mediaPlayer.prepare(mediaSource)
        setVolume(currentVolume)
        if (isAutoPlay) playAudio()
        else pauseAudio()
        seekTo(currentTimeMs)
    }

    override fun changeMusic(path: String) {
        val isAutoPlay = mediaPlayer.isPlaying
        musicPath = path
        length = MediaHelper.getVideoDuration(path)
        musicPath = path
        val mediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.fromFile(File(path))))
        mediaPlayer.prepare(mediaSource)
        setVolume(currentVolume)
        if (isAutoPlay) playAudio()
        else pauseAudio()
        seekTo(0)
    }

    override fun getOutMusicPath(): String = musicPath

    override fun getOutMusic(): MusicReturnInfo {
        return MusicReturnInfo(musicPath, "", startOffset, length)
    }

    override fun useDefault() {
        musicPath = FileHelper.defaultAudio
        val mediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.fromFile(File(FileHelper.defaultAudio))))
        mediaPlayer.prepare(mediaSource)
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

}