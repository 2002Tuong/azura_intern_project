package com.slideshowmaker.slideshow.modules.music_player


import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.utils.MediaHelper
import java.io.File
import java.util.Timer
import java.util.TimerTask

class MusicPlayerImpl : MusicPlayer {

    private var musicPlayer = SimpleExoPlayer.Builder(VideoMakerApplication.getContext()).build()

    private var startOffset = 0
    private var length = 0
    private var timer: Timer? = null

    private var isAudioReady = false

    private var audioPath = ""
    private val defaultBandwidthMeter =
        DefaultBandwidthMeter.Builder(VideoMakerApplication.getContext()).build()
    private val defaultDataSourceFactory = DefaultDataSourceFactory(
        VideoMakerApplication.getContext(),
        "video-maker-v4",
        defaultBandwidthMeter
    )

    override fun play() {
        musicPlayer.playWhenReady = true
    }

    override fun pause() {
        musicPlayer.playWhenReady = false
    }

    override fun changeState() {
        musicPlayer.playWhenReady = !musicPlayer.playWhenReady
    }

    override fun changeMusic(audioFilePath: String) {
        isAudioReady = false
        audioPath = audioFilePath
        val mediaSource: MediaSource = ExtractorMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(Uri.fromFile(File(audioFilePath)))
        musicPlayer.prepare(mediaSource)
        startOffset = 0
        length = MediaHelper.getVideoDuration(audioFilePath)
        play()
        isAudioReady = true
        if (timer == null)
            onListen()

    }

    override fun changeMusic(audioFilePath: String, startOffset: Int, length: Int) {
        isAudioReady = false
        audioPath = audioFilePath
        val mediaSource: MediaSource = ExtractorMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(Uri.fromFile(File(audioFilePath)))


        musicPlayer.prepare(mediaSource)
        musicPlayer.seekTo(startOffset.toLong())
        this.startOffset = startOffset
        play()
        this.length = length
        isAudioReady = true
        if (timer == null)
            onListen()
    }

    override fun seekTo(offset: Int) {
        musicPlayer.seekTo(offset.toLong())
    }

    override fun changeStartOffset(startOffset: Int) {
        this.startOffset = startOffset
        musicPlayer.seekTo(this.startOffset.toLong())
    }

    override fun changeLength(length: Int) {
        this.length = length
    }

    override fun release() {
        timer?.cancel()
        musicPlayer.release()
    }

    override fun getOutMusic(): MusicReturnInfo {
        return MusicReturnInfo(audioPath, "", startOffset, length)
    }

    private fun onListen() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (isAudioReady)
                    if (musicPlayer.isPlaying) {
                        if (musicPlayer.currentPosition - startOffset >= length) {
                            restart()
                        }
                    }
            }
        }, 0, 100)
    }

    fun restart() {
        musicPlayer.seekTo(startOffset.toLong())
    }
}