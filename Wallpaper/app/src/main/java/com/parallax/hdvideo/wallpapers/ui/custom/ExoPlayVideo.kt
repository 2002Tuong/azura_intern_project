package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RawRes
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.services.log.EventData
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.other.AES
import java.io.File

class ExoPlayVideo constructor(context: Context): Player.Listener /*VideoListener*/ {

    companion object {
        private var _simpleCache: SimpleCache? = null
        private val userAgent = "User-Agent: TPcom/3.0 " + System.getProperty("http.agent")
        private const val FILE_SIZE = 100 * 1024 * 1024L // 100Mb
        private const val TAG = "ExoPlayVideo"
        fun deleteDB() {
            val file = WallpaperApp.instance.getDatabasePath(ExoDatabaseProvider.DATABASE_NAME)
            if (file.exists()) {
                file.delete()
            }
        }

        val simpleCache: SimpleCache get() {
            return _simpleCache ?: synchronized(this) {
                val databaseProvider = ExoDatabaseProvider(WallpaperApp.instance)
                SimpleCache(
                    FileUtils.exoplayerCacheDirect,
                    LeastRecentlyUsedCacheEvictor(FILE_SIZE),
                    databaseProvider, AES.IV, true, true).also {
                    _simpleCache = it
                }
            }
        }
    }

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: SimpleExoPlayer
    private var lastPath = ""
    private var retryUrl: String? = null
    private var isFromLocalStorage: Boolean = false
    private var allowCache: Boolean = false
    private var listEventListener = mutableListOf<EventListener>()
    private var retryCount = 0
    private val maxRetryCount = 1

    init {
        setup(context)
    }

    @Synchronized
    private fun setup(context: Context) {
        playerView = PlayerView(context)
        playerView.useController = false
        playerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        playerView.elevation = 0f
        exoPlayer = SimpleExoPlayer.Builder(context).build()
        playerView.player = exoPlayer
        exoPlayer.volume = 0f
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        //exoPlayer.addVideoListener(this)
        exoPlayer.addListener(this)
    }

    @Synchronized
    fun play(parent: ViewGroup, file: File) : Boolean {
        try {
            if (parentView != null && lastPath == file.path) {
                return true
            }
            addToView(parent)
            lastPath = file.path
            val uri = Uri.fromFile(file)
            val dataSource = ProgressiveMediaSource.Factory(FileDataSource.Factory()).createMediaSource(MediaItem.fromUri(uri))
            exoPlayer.prepare(dataSource)
            exoPlayer.playWhenReady = true
            listEventListener.forEach {
                it.onStart()
            }
            return true
        } catch (e: Exception) {
            lastPath = ""
        }
        return false
    }

    @Synchronized
    fun play(parent: ViewGroup, url: String,
                isFromLocalStorage: Boolean  = false,
                allowCache: Boolean = false,
                retryUrl: String? = null) : Boolean {
        if (parentView != null && lastPath == url) {
            return true
        }
        addToView(parent)
        retryCount = 0
        this.lastPath = url
        this.retryUrl = retryUrl
        this.allowCache = allowCache
        this.isFromLocalStorage = isFromLocalStorage
        val check = prepareInternal(url)
        try {
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {

        }
        return check
    }

    private fun prepareInternal(url: String) : Boolean {
        val view = parentView ?: return false
        return try {
            val uri = Uri.parse(url)
            val factory = when {
                isFromLocalStorage -> DataSource.Factory { AssetDataSource(view.context) }
                url.startsWith("http") -> {
                    if (allowCache) CacheDataSource.Factory().apply {
                        setCache(simpleCache)
                        setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory().apply { setUserAgent(userAgent) })
                    }
                    else DefaultHttpDataSource.Factory().apply { setUserAgent(userAgent) }
                }
                else -> FileDataSource.Factory()
            }
            val dataSource = ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri))
            exoPlayer.prepare(dataSource)
            listEventListener.forEach {
                it.onStart()
            }
            Logger.d(TAG, url)
            true
        } catch (e: Exception) {
            lastPath = ""
            false
        }
    }

    @Synchronized
    fun playRawFile(parent: ViewGroup, @RawRes id: Int) : Boolean {
        try {
            val uri = RawResourceDataSource.buildRawResourceUri(id)
            if (parentView != null && lastPath == uri.toString()) {
                return true
            }
            lastPath = uri.toString()
            addToView(parent)
            val factory = RawResourceDataSource(parent.context)
            val dataSource = ProgressiveMediaSource.Factory(DataSource.Factory { factory }).createMediaSource(MediaItem.fromUri(uri))
            exoPlayer.prepare(dataSource)
            exoPlayer.playWhenReady = true
            listEventListener.forEach {
                it.onStart()
            }
            return true
        } catch (e: Exception) {
            lastPath = ""
        }
        return false
    }

    fun playOrPause(isPlay : Boolean = true) {
        exoPlayer.playWhenReady = isPlay
    }

    private fun stop() {
        exoPlayer.stop()
        playerView.animate().cancel()
        playerView.alpha = 0f
        lastPath = ""
    }

    fun removeFromParent(animate: Boolean = false) {
        val parent = parentView
        if (animate && parent != null) {
            playerView.animate().setStartDelay(100).alpha(0f).setDuration(300).withEndAction {
                stop()
                parentView?.removeView(playerView)
                Logger.d(TAG, "removeFromParent")
            }.start()
        } else {
            stop()
            parent?.removeView(playerView)
            Logger.d(TAG, "removeFromParent")
        }
    }

    fun replaceParentView(viewGroup: ViewGroup) {
        parentView?.removeView(playerView)
        viewGroup.isHidden = false
        viewGroup.addView(playerView)
        playOrPause(true)
    }

    private fun addToView(parent: ViewGroup)  {
        stop()
        parentView?.removeView(playerView)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        parent.addView(playerView, params)
    }

    fun release() {
        stop()
        //exoPlayer.removeVideoListener(this)
        exoPlayer.removeListener(this)
        exoPlayer.release()
    }

    val isPlaying get() = exoPlayer.isPlaying

    val parentView get() = playerView.parent as? ViewGroup

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                listEventListener.forEach { it.stateBuffering() }
                Logger.d(TAG, "Buffering video.  playWhenReady = $playWhenReady")

            }
            Player.STATE_ENDED -> {
                listEventListener.forEach { it.onComplete() }
                Logger.d(TAG, "Video ended.   playWhenReady = $playWhenReady")
            }
            Player.STATE_IDLE -> {

            }
            Player.STATE_READY -> {
                listEventListener.forEach { it.onPlay(playWhenReady) }
                Logger.d(TAG,  "Ready to play. playWhenReady =  $playWhenReady")
            }
            else -> {

            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Logger.d(TAG, "onPlayerError", error.toString())
        val url = retryUrl
        if (NetworkUtils.isNetworkConnected()
            && url != null
            && retryCount < maxRetryCount) {
            retryCount++
            Logger.d(TAG, "retry", url)
            prepareInternal(url)
        } else {
            removeFromParent()
            listEventListener.forEach {
                it.onError()
            }
            NetworkUtils.notifyOffline()
        }
        TrackingSupport.recordEvent(EventData.VideoExoPlayerError, "exoPlaybackException" to error.message)
    }

    override fun onRenderedFirstFrame() {
        if (playerView.alpha == 0f) {
            playerView.animate().alpha(1f).setDuration(200).start()
            Logger.d(TAG, "onRenderedFirstFrame")
            listEventListener.forEach {
                it.onRenderedFirstFrame()
            }
        }
    }

    val currentPath: String get() = lastPath

    fun addEventListener(eventListener: EventListener?) {
        if (eventListener == null || listEventListener.contains(eventListener)) return
        listEventListener.add(eventListener)
    }

    fun setOnOffSound(on: Boolean) {
        if (this::exoPlayer.isInitialized)
            exoPlayer.volume = if (on) 1f else 0f
    }

    interface EventListener {

        fun onStart() {

        }

        fun stateBuffering() {

        }
        fun onPlay(playWhenReady: Boolean) {

        }
        fun onComplete() {

        }
        fun onError() {

        }
        fun onRenderedFirstFrame() {

        }
    }
}