package com.slideshowmaker.slideshow.ui.share_video

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import com.ads.control.ads.VioAdmob
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivityShareVideoScreenBinding
import com.slideshowmaker.slideshow.modules.share.Share
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.custom.VideoControllerView
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity.Companion.joinVideoActonCode
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity.Companion.renderSlideActionCode
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity.Companion.renderVideoSlideActionCode
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity.Companion.trimVideoActonCode
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.MediaHelper
import com.slideshowmaker.slideshow.utils.extentions.isFirstInstall
import kotlinx.android.synthetic.main.activity_share_video_screen.exoPlayerView
import kotlinx.android.synthetic.main.activity_share_video_screen.icPlay
import kotlinx.android.synthetic.main.activity_share_video_screen.videoControllerView
import java.io.File

class ShareVideoActivity : BaseActivity() {
    private var videoSrcPath = ""
    private var countDownTimer: CountDownTimer? = null
    var totalTime = 0
    private lateinit var layoutBinding: ActivityShareVideoScreenBinding

    companion object {
        fun gotoActivity(
            activity: Activity,
            videoPath: String,
            showRating: Boolean = false,
            fromProcess: Boolean = false,
            fromAction: Int = 0
        ) {
            val openActivityIntent = Intent(activity, ShareVideoActivity::class.java)
            openActivityIntent.putExtra("VideoPath", videoPath)
            openActivityIntent.putExtra("ShowRating", showRating)
            openActivityIntent.putExtra("fromProcess", fromProcess)
            openActivityIntent.putExtra("fromAction", fromAction)
            activity.startActivity(openActivityIntent)
        }
    }

    override fun getContentResId(): Int = R.layout.activity_share_video_screen

    private fun logShareEvent() {
        when (intent.getIntExtra("fromAction", 0)) {
            renderSlideActionCode -> {
                if (SharedPreferUtils.shareSlideshowSuccessOnFirstLaunch && isFirstInstall()) {
                    SharedPreferUtils.shareSlideshowSuccessOnFirstLaunch = false
                    TrackingFactory.Common.shareSlideshowSuccessFirstLaunch().track()
                }
                TrackingFactory.SlideshowEditor.shareSuccess().track()
            }

            renderVideoSlideActionCode -> TrackingFactory.EditVideo.shareSuccess().track()
            joinVideoActonCode -> TrackingFactory.JoinVideo.shareSuccess().track()
            trimVideoActonCode -> TrackingFactory.TrimVideo.shareSuccess().track()
            else -> {}
        }
    }

    override fun initViews() {
        layoutBinding = ActivityShareVideoScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        val videoSrcPath = intent.getStringExtra("VideoPath")
        setScreenTitle(getString(R.string.share))

        videoSrcPath?.let {

            this.videoSrcPath = it
            try {
                totalTime = MediaHelper.getVideoDuration(this.videoSrcPath)
                videoControllerView.setMaxDuration(totalTime)
                initVideoPlayer(this.videoSrcPath)
            } catch (e: Exception) {
                totalTime = 1

            }

        }
        val isFromProcess = intent.getBooleanExtra("fromProcess", false)
        if (isFromProcess) {
            TrackingFactory.Common.showAdsInterstitial("exporting").track()
            exoPlayer?.playWhenReady = true
        }
        videoControllerView.onChangeListenerCallback = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {

                exoPlayer?.seekTo(timeMilSec.toLong())
            }

            override fun onMove(progress: Float) {


                exoPlayer?.seekTo((totalTime * progress / 100).toLong())
            }
        }

        AdsHelper.requestNativeSuccess(
            this,
            onLoaded = {
                VioAdmob.getInstance().populateNativeAdView(
                    this,
                    it,
                    layoutBinding.frAds,
                    layoutBinding.includeNative.shimmerContainerBanner
                )
            },
            onLoadFail = {
                layoutBinding.frAds.visibility = View.INVISIBLE
            }
        )

    }


    private fun listenVideoPosition() {
        countDownTimer = object : CountDownTimer(120000000, 100) {
            override fun onFinish() {
                this.start()
            }

            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    //videoControllerView.setCurrentDuration(mRenderer?.getCurrentPosition() ?: 0)
                    videoControllerView.setCurrentDuration(exoPlayer?.contentPosition ?: -1L)
                }

            }
        }
    }

    val share = Share()
    override fun initActions() {
        layoutBinding.imgBack.setOnClickListener { onBackPressed() }
        layoutBinding.imgHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("play-splash", false)
            }
            startActivity(intent)
        }
        layoutBinding.bgViewInShare.setOnClickListener {
            exoPlayer?.playWhenReady = !(exoPlayer?.playWhenReady ?: false)
        }

        layoutBinding.logoYouTube.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.YOUTUBE_PACKAGE_NAME)
        }

        layoutBinding.logoInstagram.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.INSTAGRAM_PACKAGE_NAME)
        }
        layoutBinding.logoFacebook.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.FACEBOOK_PACKAGE_NAME)
        }

        layoutBinding.logoTwitter.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.TWITTER_PACKAGE_NAME)
        }

        layoutBinding.logoTiktok.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.TIKTOK_PACKAGE_NAME)
        }

        layoutBinding.logoMessenger.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.MESSENGER_PACKAGE_NAME)
        }
        layoutBinding.logoWhatsapp.setOnClickListener {
            logShareEvent()
            share.shareTo(this, videoSrcPath, Share.WHATSAPP_PACKAGE_NAME)
        }
        layoutBinding.logoMore.setOnClickListener {
            logShareEvent()
            shareVideoFile(videoSrcPath)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        onPauseVideo()
        countDownTimer?.cancel()
        exoPlayer?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        countDownTimer?.start()
    }

    private fun onPauseVideo() {
        countDownTimer?.cancel()
        icPlay.visibility = View.VISIBLE
    }

    private fun onPlayVideo() {
        countDownTimer?.start()
        icPlay.visibility = View.GONE
    }

    private var exoPlayer: SimpleExoPlayer? = null
    private fun initVideoPlayer(path: String) {
        exoPlayer = SimpleExoPlayer.Builder(VideoMakerApplication.getContext()).build()
        exoPlayerView.player = exoPlayer
        val defaultBandwidthMeter =
            DefaultBandwidthMeter.Builder(VideoMakerApplication.getContext()).build()
        val defaultDataSourceFactory = DefaultDataSourceFactory(this, "videomaker-2", defaultBandwidthMeter)
        val progressiveMediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory).createMediaSource(
            Uri.fromFile(
                File(path)
            )
        )
        exoPlayer?.playWhenReady = false
        exoPlayerView.useController = false
        exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                onStateChange.invoke(playWhenReady)
                if (playbackState == Player.STATE_ENDED) {
                    onEndCallback.invoke()
                }

            }
        })

        exoPlayer?.prepare(progressiveMediaSource, true, true)
        listenVideoPosition()
//        Thread {
//            Thread.sleep(500)
//            runOnUiThread {
//                mPlayer?.playWhenReady = true
//            }
//        }.start()
    }


    private val onEndCallback = {
        exoPlayer?.seekTo(0L)
        exoPlayer?.playWhenReady = false
    }

    private val onStateChange = { isPlay: Boolean ->
        if (isPlay) {
            onPlayVideo()
        } else {
            onPauseVideo()
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("play-splash", false)
        }
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}
