package com.slideshowmaker.slideshow.ui.trim_video

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import android.widget.FrameLayout
import com.ads.control.ads.VioAdmob
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.custom.CropVideoTimeView
import com.slideshowmaker.slideshow.ui.custom.VideoControllerView
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.DimenUtils
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import kotlinx.android.synthetic.main.activity_trim_video_screen.*
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class TrimVideoActivity : BaseActivity(), MediaPlayer.OnCompletionListener {

    override fun getContentResId(): Int = R.layout.activity_trim_video_screen

    private var countDownTimer: CountDownTimer? = null

    var totalTime = 0

    private var startOffset = 0
    private var endOffset = 0
    private var videoSrcPath = ""

    companion object {
        fun gotoActivity(activity: Activity, videoPath: String) {
            val activityIntent = Intent(activity, TrimVideoActivity::class.java)
            activityIntent.putExtra("VideoPath", videoPath)
            activity.startActivity(activityIntent)
        }
    }

    var trimValid = true
    override fun initViews() {
        AdsHelper.loadInterSaveJoinTrim(this)
        TrackingFactory.TrimVideo.viewEditor().track()
        val scale = DimenUtils.videoScaleInTrim()
        Logger.e("scale in trim = $scale")
        bgView.layoutParams.width = (DimenUtils.screenWidth(this) * scale).toInt()
        bgView.layoutParams.height = (DimenUtils.screenWidth(this) * scale).toInt()
        canShowDialog = true
        setScreenTitle(getString(R.string.trim_video_editor_label))
        playerViewInTrim.hideController()
        val videoSrcPath = intent.getStringExtra("VideoPath")
        videoSrcPath?.let { videoPath ->
            val duration = MediaHelper.getAudioDuration(videoPath)
            startOffset = 0
            endOffset = duration.toInt()
            videoControllerView.setMaxDuration(duration.toInt())
            cropTimeView.loadImage(
                videoPath,
                DimenUtils.screenWidth(this) - (76 * DimenUtils.density(this)).roundToInt()
            )
        }
        videoControllerView.onChangeListenerCallback = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                // mRenderer?.seekTo(timeMilSec)

                exoPlayer?.seekTo(timeMilSec.toLong())
            }

            override fun onMove(progress: Float) {
                //   mRenderer?.seekTo((mTotalDuration*progress/100).roundToInt())
                exoPlayer?.seekTo((totalTime * progress / 100).toLong())
            }
        }

        cropTimeView.onChangeListener = object : CropVideoTimeView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                startOffset = startTimeMilSec.roundToInt()
                // mRenderer?.seekTo(startTimeMilSec.roundToInt())
                exoPlayer?.seekTo(startTimeMilSec.toLong())

            }

            override fun onUpLeft(startTimeMilSec: Float) {
                startOffset = startTimeMilSec.roundToInt()
                //mRenderer?.seekTo(startTimeMilSec.roundToInt())
                exoPlayer?.seekTo(startTimeMilSec.toLong())
                videoControllerView.changeStartPositionOffset(startTimeMilSec.roundToLong())
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                endOffset = endTimeMilSec.roundToInt()
                //mRenderer?.seekTo(endTimeMilSec.roundToInt()-5000)
                exoPlayer?.seekTo(endTimeMilSec.toLong() - 2000)
            }

            override fun onUpRight(endTimeMilSec: Float) {
                endOffset = endTimeMilSec.roundToInt()
                //mRenderer?.seekTo(endTimeMilSec.roundToInt()-5000)
                exoPlayer?.seekTo(endTimeMilSec.toLong() - 2000)
            }

        }

        buttonPlayAndPause.setOnClickListener {
            exoPlayer?.playWhenReady = !(exoPlayer?.playWhenReady ?: true)
        }
        bgView.setOnClickListener { exoPlayer?.playWhenReady = !(exoPlayer?.playWhenReady ?: true) }

        setRightTextButton(R.string.label_button_trim_allcaps, false) {
            AdsHelper.requestNativeSaving(this)
            TrackingFactory.TrimVideo.clickSave().track()
            if (checkCutTime()) {

                if (trimValid) {
                    trimValid = false
                    exoPlayer?.playWhenReady = false
                    exoPlayer?.release()
                    exoPlayer = null
                    AdsHelper.forceShowInterSaveJoinTrim(this, onNextAction = {
                        val gotoActivityintent = Intent(this, ProcessVideoActivity::class.java).apply {
                            putExtra(
                                ProcessVideoActivity.action,
                                ProcessVideoActivity.trimVideoActonCode
                            )
                            putExtra("path", this@TrimVideoActivity.videoSrcPath)
                            putExtra("startTime", startOffset)
                            putExtra("endTime", endOffset)
                        }
                        startActivity(gotoActivityintent)
                    })
                    object : CountDownTimer(1000, 1000) {
                        override fun onFinish() {
                            trimValid = true
                        }

                        override fun onTick(millisUntilFinished: Long) {

                        }

                    }.start()
                }

                /*showProgressDialog()
                val outVideoPath = FileUtils.getOutputVideoPath()
                val ffmpeg = FFmpeg(FFmpegCmd.cutVideo(mVideoPath, mStartOffset.toDouble(), mEndOffset.toDouble(), outVideoPath))
                ffmpeg.runCmd({
                    Logger.e("""$it / ${mEndOffset-mStartOffset}""")
                },{ runOnUiThread {
                    ShareVideoActivity.gotoActivity(this, outVideoPath)
                    dismissProgressDialog()
                }})*/
            }
        }
        initBanner()
    }

    private fun initBanner() {
        if (!SharedPreferUtils.proUser) {
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner, bannerListenerCallback)
        } else {
            frAdsTrim.visibility = View.GONE
        }
    }

    override fun reloadBanner() {
        super.reloadBanner()
        if (!SharedPreferUtils.proUser) {
            findViewById<FrameLayout>(R.id.fl_shimemr).visibility = View.INVISIBLE
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner, bannerListenerCallback)
        }
    }

    private val limitedTime = 1000
    private fun checkCutTime(): Boolean {

        if ((endOffset - startOffset) < limitedTime) {
            showToast(getString(R.string.toast_minimum_time_is_1s))
            return false
        }

        return true
    }


    private fun listenVideoPosition() {
        countDownTimer = object : CountDownTimer(120000000, 100) {
            override fun onFinish() {
                this.start()
            }

            override fun onTick(millisUntilFinished: Long) {
                val currentPos = exoPlayer?.currentPosition ?: 0
                if (currentPos > endOffset) {
                    //mRenderer?.seekTo(mStartOffset)
                    exoPlayer?.seekTo(startOffset.toLong())
                } else if (currentPos <= startOffset) {
                    exoPlayer?.seekTo(startOffset.toLong())
                }
                runOnUiThread {
                    videoControllerView.setCurrentDuration(exoPlayer?.currentPosition ?: 0)
                }
            }
        }.start()
    }

    override fun initActions() {

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }


    override fun onPause() {
        super.onPause()
        onPauseVideo()
        countDownTimer?.cancel()
        exoPlayer?.playWhenReady = false

    }

    override fun onResume() {
        super.onResume()
        // glPlayerView.onResume()
        countDownTimer?.start()
        val videoSrcPath = intent.getStringExtra("VideoPath")
        videoSrcPath?.let {
            this.videoSrcPath = it
            initVideoPlayer(this.videoSrcPath)

        }
    }

    private fun onPauseVideo() {

        buttonPlayAndPause.setImageResource(R.drawable.icon_play)
    }

    private fun onPlayVideo() {
        buttonPlayAndPause.setImageResource(R.drawable.icon_pause_24)
    }

    override fun onCompletion(mp: MediaPlayer?) {

    }

    private var exoPlayer: SimpleExoPlayer? = null
    private fun initVideoPlayer(path: String) {
        exoPlayer = SimpleExoPlayer.Builder(VideoMakerApplication.getContext()).build()

        playerViewInTrim.player = exoPlayer
        val defaultBandwidthMeter =
            DefaultBandwidthMeter.Builder(VideoMakerApplication.getContext()).build()
        val defaultDataSourceFactory = DefaultDataSourceFactory(this, "videomaker", defaultBandwidthMeter)
        val progressiveMediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory).createMediaSource(
            Uri.fromFile(
                File(path)
            )
        )
        exoPlayer?.playWhenReady = true
        //runUpdate()

        exoPlayer?.addListener(object : Player.EventListener {


            override fun onSeekProcessed() {

            }


            override fun onLoadingChanged(isLoading: Boolean) {

            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }


            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                onStateChange.invoke(playWhenReady)
                if (playbackState == Player.STATE_ENDED) {
                    onEndCallback.invoke()
                }

            }
        })
        playerViewInTrim.useController = false
        exoPlayer?.playWhenReady = false
        exoPlayer?.prepare(progressiveMediaSource, true, true)
        //mPlayer?.addListener(this)
        exoPlayer?.seekTo(startOffset.toLong())
        listenVideoPosition()
    }


    private val onEndCallback = {
        exoPlayer?.seekTo(startOffset.toLong())
        exoPlayer?.playWhenReady = false
    }

    private val onStateChange = { isPlay: Boolean ->
        if (isPlay) {
            onPlayVideo()
        } else {
            onPauseVideo()
        }
    }

    override fun onStop() {
        super.onStop()
    }


}
