package com.slideshowmaker.slideshow.ui.join_video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.VideoListInJoinerAdapter
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.models.VideoForJoinModel
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.custom.VideoControllerView
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import com.slideshowmaker.slideshow.utils.Utils
import kotlinx.android.synthetic.main.activity_join_video_screen.*
import java.io.File

class JoinVideoActivity2 : BaseActivity() {
    override fun getContentResId(): Int = R.layout.activity_join_video_screen

    private var videoIndex = 0

    private val videoSrcPathList = ArrayList<String>()
    private val videoJoinDataList = ArrayList<VideoForJoinModel>()

    private var timelineOffset = 0

    private val videoListInJoinerAdapter = VideoListInJoinerAdapter()
    private var countDownTimer: CountDownTimer? = null

    private var playing = true

    companion object {
        fun gotoActivity(activity: Activity, videoPathList: ArrayList<String>) {
            val intent = Intent(activity, JoinVideoActivity2::class.java)
            val bundle = Bundle().apply {
                putStringArrayList("videoPathList", videoPathList)
            }
            intent.putExtra("bundle", bundle)
            activity.startActivity(intent)
        }
    }


    override fun initViews() {
        AdsHelper.loadInterSaveJoinTrim(this)
        TrackingFactory.JoinVideo.viewEditor().track()
        setScreenTitle(getString(R.string.join_video_editor_label))
        videoPlayerView.useController = false
        intent.getBundleExtra("bundle")?.let {
            it.getStringArrayList("videoPathList")?.let { pathList ->
                if (pathList.size > 0) {
                    setupData(pathList)

                    // listenVideoPosition()
                }
            }
        }
        canShowDialog = true

        setRightTextButton(R.string.button_label_join_allcaps, false) {
            AdsHelper.requestNativeSaving(this)
            doPauseVideo()
            TrackingFactory.JoinVideo.clickSave().track()

            if (Utils.checkStorageSpace(videoSrcPathList)) {

                // mVideoSlideRenderer.releasePlayer()
                showLoadingDialog()

                Thread {
                    exoPlayer?.release()
                    exoPlayer = null
                    Thread.sleep(500)
                    runOnUiThread {
                        dismissLoadingDialog()
                        AdsHelper.forceShowInterSaveJoinTrim(this, onNextAction = {
                            val intent =
                                Intent(this@JoinVideoActivity2, ProcessVideoActivity::class.java)
                            intent.putStringArrayListExtra("joinVideoList", videoSrcPathList)
                            intent.putExtra(
                                ProcessVideoActivity.action,
                                ProcessVideoActivity.joinVideoActonCode
                            )
                            startActivity(intent)
                            doJoin = true
                        })
                    }
                }.start()


            } else {
                showToast(getString(R.string.lack_free_space))
            }
        }
        initBanner()
    }

    private fun initBanner() {
        if (!SharedPreferUtils.proUser) {
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner, bannerListenerCallback)
        } else {
            frAdsJoin.visibility = View.GONE
        }
    }

    override fun reloadBanner() {
        super.reloadBanner()
        if (!SharedPreferUtils.proUser) {
            findViewById<FrameLayout>(R.id.fl_shimemr).visibility = View.INVISIBLE
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner, bannerListenerCallback)
        }
    }

    private var exoPlayer: ExoPlayer? = null

    private fun changeVideo(path: String) {
        //mPlayer?.release()
        // mPlayer=null
        videoListInJoinerAdapter.highlightItem(videoJoinDataList[videoIndex].id)
        if (exoPlayer == null) {

            exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
            videoPlayerView.player = exoPlayer
            exoPlayer?.playWhenReady = false
            exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
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

                    if (playbackState == Player.STATE_ENDED) {
                        //onEnd.invoke()
                        Logger.e("on end video ----> Player.STATE_ENDED ${videoIndex}")
                        if (videoIndex == videoSrcPathList.size - 1) {
                            doRestartVideo()
                        } else {
                            onNextVideo()
                        }

                    } else {

                    }

                }
            })
        }

        val defaultBandwidthMeter = DefaultBandwidthMeter()
        val defaultDataSourceFactory = DefaultDataSourceFactory(this, "videomaker", defaultBandwidthMeter)
        val extractorMediaSource = ExtractorMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(Uri.fromFile(File(path)))

        exoPlayer?.prepare(extractorMediaSource)
        listenVideoTime()
    }

    private fun listenVideoTime() {
        countDownTimer = object : CountDownTimer(60 * 60 * 1000, 40) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    videoControllerView.setCurrentDuration(
                        timelineOffset + (exoPlayer?.currentPosition ?: 0)
                    )
                }
            }

        }.start()
    }

    private fun doRestartVideo() {
        timelineOffset = 0
        videoIndex = 0
        changeVideo(videoSrcPathList[videoIndex])
        doPlayVideo()
    }

    override fun initActions() {
        videoControllerView.onChangeListenerCallback = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }


        buttonJoinVideo.setOnClickListener {
            doPauseVideo()

            if (Utils.checkStorageSpace(videoSrcPathList)) {

                // mVideoSlideRenderer.releasePlayer()
                showLoadingDialog()

                Thread {
                    exoPlayer?.release()
                    exoPlayer = null
                    Thread.sleep(500)
                    runOnUiThread {
                        dismissLoadingDialog()
                        val intent =
                            Intent(this@JoinVideoActivity2, ProcessVideoActivity::class.java)
                        intent.putStringArrayListExtra("joinVideoList", videoSrcPathList)
                        intent.putExtra(
                            ProcessVideoActivity.action,
                            ProcessVideoActivity.joinVideoActonCode
                        )
                        startActivity(intent)
                        doJoin = true
                    }
                }.start()


            } else {
                showToast(getString(R.string.lack_free_space))
            }


        }

        bgView.setOnClickListener {
            if (doJoin) {
                doRestartVideo()
            } else {
                if (playing) doPauseVideo()
                else doPlayVideo()
            }

        }

        videoListInJoinerAdapter.itemClickCallback = {
            onSelectItem(it.id)
        }

    }

    private fun doSeekTo(timeMilSec: Int) {
        var totalTime = 0
        var targetIndex = 0
        for (item in videoJoinDataList) {
            val duration = MediaHelper.getVideoDuration(item.filePath)
            if (totalTime + duration > timeMilSec) {
                videoIndex = targetIndex

                //videoPlayerView.seekTo(VideoInSlideData(mVideoPathList[mCurrentVideoIndex]), timeMilSec - time,mIsPlaying)
                changeVideo(videoSrcPathList[videoIndex])
                exoPlayer?.seekTo((timeMilSec - totalTime).toLong())
                videoListInJoinerAdapter.highlightItem(videoJoinDataList[videoIndex].id)
                doPlayVideo()
                break
            } else {
                targetIndex++
                totalTime += duration
            }
        }
        updateTimelineOffset()
    }

    private fun onSelectItem(videoId: Int) {
        var totalTime = 0
        var targetIndex = 0
        icPlay.visibility = View.GONE
        playing = true
        for (item in videoJoinDataList) {
            if (item.id == videoId) {
                videoIndex = targetIndex
                //videoPlayerView.seekTo(VideoInSlideData(mVideoPathList[mCurrentVideoIndex]), 0,true)
                changeVideo(videoSrcPathList[videoIndex])
                videoListInJoinerAdapter.highlightItem(videoJoinDataList[videoIndex].id)
            } else {
                targetIndex++
                totalTime += MediaHelper.getVideoDuration(item.filePath)
            }
        }
        updateTimelineOffset()
    }

    private fun setupData(imageList: ArrayList<String>) {


        showLoadingDialog()
        Thread {
            videoSrcPathList.clear()
            videoSrcPathList.addAll(imageList)

            var totalDuration = 0
            for (item in videoSrcPathList) {
                videoJoinDataList.add(VideoForJoinModel(item))
                totalDuration += (MediaHelper.getVideoDuration(item))
            }
            runOnUiThread {
                setupListView()
                changeVideo(videoSrcPathList[0])
                dismissLoadingDialog()
                videoControllerView.setMaxDuration(totalDuration)
            }


        }.start()

    }

    private var duration = 0
    private fun setupListView() {
        videoListView.apply {
            adapter = videoListInJoinerAdapter
            layoutManager =
                LinearLayoutManager(this@JoinVideoActivity2, LinearLayoutManager.HORIZONTAL, false)
        }
        for (item in videoJoinDataList) {
            videoListInJoinerAdapter.addItem(item)
        }
        videoListInJoinerAdapter.highlightItem(videoJoinDataList[0].id)

    }


    private fun onNextVideo() {
        /*Logger.e("current index = $mCurrentVideoIndex")
        if (mCurrentVideoIndex + 1 >= mVideoPathList.size) {
            mCurrentVideoIndex = 0
            doSeekTo(0)
            updateTimelineOffset()
           // videoPlayerView.changeVideo(VideoInSlideData(mVideoPathList[mCurrentVideoIndex]) )
            mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)
        } else {

            mCurrentVideoIndex++
            updateTimelineOffset()
          //  videoPlayerView.changeVideo(VideoInSlideData(mVideoPathList[mCurrentVideoIndex]))
            mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)

        }*/
        if (videoIndex == videoSrcPathList.size - 1) {
            onRestartVideo()
            //doSeekTo(0)
        } else {
            ++videoIndex
            updateTimelineOffset()
            changeVideo(videoSrcPathList[videoIndex])
        }

    }

    private fun updateTimelineOffset() {
        timelineOffset = 0
        if (videoIndex == 0) timelineOffset = 0
        else
            for (index in 0 until videoIndex) {
                timelineOffset += (MediaHelper.getVideoDuration(videoSrcPathList[index]))
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        countDownTimer?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }


    override fun onPause() {
        super.onPause()
        doPauseVideo()
    }

    private var doJoin = false
    private fun onRestartVideo() {
        doJoin = false
        playing = true
        icPlay.visibility = View.GONE
        doSeekTo(duration)

    }

    private fun doPauseVideo() {
        playing = false
        //mVideoSlideRenderer.onPause()
        exoPlayer?.playWhenReady = false
        countDownTimer?.cancel()
        icPlay.visibility = View.VISIBLE
    }

    private fun doPlayVideo() {
        playing = true
        // mVideoSlideRenderer.onPlayVideo()
        exoPlayer?.playWhenReady = true
        countDownTimer?.start()
        icPlay.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (doJoin) {
            doJoin = false
            showLoadingDialog()
            Thread {
                Thread.sleep(500)
                runOnUiThread {
                    dismissLoadingDialog()
                }
            }.start()

        }
    }

}
