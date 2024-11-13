package com.parallax.hdvideo.wallpapers.services.wallpaper

import android.content.Context
import android.net.Uri
import android.view.SurfaceHolder
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.ContentDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.services.log.EventData
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.dialog.SetSoundWallpaperDialog
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.io.File

class VideoEngine(context: Context, storage: LocalStorage) : WallpaperEngine(context, storage) {
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var pressedStartTime: Long = 0
    private var pressX = 0f
    private var pressY = 0f
    private var lastNameUrl: String? = null
//    private val mHandler = Handler(Looper.getMainLooper())
//    private val dataSourceRunnable = Runnable {
//        if (isVisible) {
//            playVideo()
//        } else {
//            stopVideo()
//        }
//    }
//    override fun onTouchEvent(event: MotionEvent) {
//        when (event.action) {
//
//            MotionEvent.ACTION_DOWN -> {
//                pressStartTime = System.currentTimeMillis()
//                pressedX = event.x
//                pressedY = event.y
//            }
//
//            MotionEvent.ACTION_UP -> {
//                val pressDuration = System.currentTimeMillis() - pressStartTime
//                if (pressDuration < 1_000
//                    && hypot(event.y - pressedY, event.x - pressedX) < 20F
//                    && isVisible) {
//                    playOrPause()
//                }
//            }
//        }
//    }


    @Synchronized
    private fun playVideo() {
        val surfaceHolder = surfaceHolder ?: return
        if (!visibility) return
        if (simpleExoPlayer == null) {
            Logger.d("VideoEngine playVideo")
            simpleExoPlayer = SimpleExoPlayer.Builder(context).build().apply {
                setVideoSurfaceHolder(surfaceHolder)
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Logger.d("onPlayerError", error.toString())
                        lastNameUrl = null
                        TrackingSupport.recordEvent(EventData.VideoEngineError)
                    }
                })
            }
        }
        try {
            val uri = getUri()
            val uriInString = uri.toString()
            if (lastNameUrl == uriInString) {
                simpleExoPlayer?.playWhenReady = true
                return
            }
            val factory = if (uriInString.startsWith("content://")) DataSource.Factory {
                ContentDataSource(WallpaperApp.instance).apply {
                    open(DataSpec(uri))
                }
            }
            else DataSource.Factory { FileDataSource() }
            val mediaSource = ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri))
            simpleExoPlayer?.apply {
                val isSound =
                    if (onPreview) SetSoundWallpaperDialog.isSoundVideo else storage.isSoundingVideo
                volume = if (isSound) 1f else 0f
                prepare(mediaSource)
                playWhenReady = true
                lastNameUrl = uriInString
            }
        } catch (e: Exception) {
            Logger.d(e.message)
        }
    }

    @Synchronized
    private fun release() {
        try {
            simpleExoPlayer?.release()
            lastNameUrl = null
            simpleExoPlayer = null
        } catch (e: Exception) {

        }
    }

    @Synchronized
    private fun stopVideo() {
        try {
            simpleExoPlayer?.stop()
            lastNameUrl = null
        } catch (e: Exception) {

        }
    }

    private fun playOrPause() {
        val player = simpleExoPlayer ?: return
        player.playWhenReady = !player.playWhenReady
    }

    override fun onSurfaceCreated(holder: SurfaceHolder) {
        TrackingSupport.recordEvent(EventData.VideoEngineSurfaceCreated)
    }

    override fun onVisibilityChanged(visible: Boolean) {
        if (visible) {
            playVideo()
        } else {
            stopVideo()
        }
    }

    override fun onSurfaceDestroyed(holder: SurfaceHolder) {
        release()
    }

    private fun getUri(): Uri {
        val model = DetailFragment.previewWallpaperModel?.second
        val dir = if (onPreview) model?.run { if (isFromLocalStorage) url else pathCacheFullVideo }
        else storage.getString(AppConstants.PreferencesKey.KEY_SAVE_VIDEO_PATH)
        val file = File(dir ?: "")
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Uri.parse(dir)
        }
    }
}