package com.slideshowmaker.slideshow.ui.custom

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.databinding.BottomSheetTrimMusicBinding
import com.slideshowmaker.slideshow.models.AudioModel
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpeg
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpegCmd
import com.slideshowmaker.slideshow.ui.base.BaseSlideShow
import com.slideshowmaker.slideshow.ui.slide_show_v2.ImageSlideShowActivity
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.Utils
import timber.log.Timber

class BottomSheetTrimMusicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var musicCallback: Callback? = null
    private lateinit var audioModel: AudioModel

    private val layoutBinding: BottomSheetTrimMusicBinding =
        BottomSheetTrimMusicBinding.inflate(LayoutInflater.from(context), this, true)

    private val animation =
        SlideAnimation(layoutBinding.rootView, SlideAnimation.ViewPosition.BOTTOM)

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.scrim))
        setOnClickListener {
            dismiss()
        }

        // Prevent dismissing view when click to content view
        layoutBinding.rootView.setOnClickListener { }

        layoutBinding.ibPlay.setOnClickListener {
            post {
                (context.getActivity() as? ImageSlideShowActivity)?.musicPlayer?.changeState()
            }
            val musicPlayer = (context.getActivity() as? ImageSlideShowActivity)?.musicPlayer
        }

        layoutBinding.rangeSeekbar.actionCallback =
            object : SeekBarRangedView.SeekBarRangedChangeCallback {
                override fun onChanged(minValue: Float, maxValue: Float) {
                    val startTime = (minValue * audioModel.duration / 100_000).toInt()
                    val endTime = (maxValue * audioModel.duration / 100_000).toInt()
                    Timber.d("Trim music to %d %d", startTime, endTime)
                    post {
                        (context.getActivity() as? ImageSlideShowActivity)?.musicPlayer?.changeStartOffset(
                            startTime * 1000
                        )
                        (context.getActivity() as? ImageSlideShowActivity)?.musicPlayer?.changeLength(
                            endTime * 1000
                        )
                        layoutBinding.tvStart.text =
                            Utils.convertSecToTimeString((startTime))
                        layoutBinding.tvEnd.text =
                            Utils.convertSecToTimeString((endTime))
                    }
                }

                override fun onChanging(minValue: Float, maxValue: Float) {

                }

            }

        layoutBinding.btnApply.setOnClickListener {
            val startTime =
                (layoutBinding.rangeSeekbar.selectedMinimumVal * audioModel.duration / 100).toInt()
            val endTime = (layoutBinding.rangeSeekbar.selectedMaximumVal * audioModel.duration / 100).toInt()
            performUseMusic(audioModel, audioModel.filePath, startTime.toLong(), endTime.toLong())
        }
    }

    private fun performUseMusic(
        audioModel: AudioModel, inputAudioPath: String, startOffset: Long, endOffset: Long
    ) {
        (context.getActivity() as? ImageSlideShowActivity)?.musicPlayer?.pause()
        (context.getActivity() as? ImageSlideShowActivity)?.showLoadingDialog()
        Thread {
            val outputMusicDir: String// = FileUtils.getTempAudioOutPutFile("m4a")
            val extension = "mp3"
            Logger.e("ex = $extension")
            outputMusicDir = if (extension != "m4a") {
                FileHelper.getTempAudioOutPutFile(extension)
            } else {
                FileHelper.getTempAudioOutPutFile("mp4")
            }

            TrackingFactory.Music.useMusic(inputAudioPath, startOffset, endOffset).track()
            Logger.e("out mp3 = $outputMusicDir")
            val ffmpeg =
                FFmpeg(FFmpegCmd.trimAudio(inputAudioPath, startOffset, endOffset, outputMusicDir))
            TrackingFactory.Common
            ffmpeg.runCmd {
                try {
                    val musicReturnInfo = MusicReturnInfo(
                        inputAudioPath,
                        outputMusicDir,
                        startOffset.toInt(),
                        endOffset.toInt() - startOffset.toInt(),
                        audioModel.fileId,
                        audioModel.name
                    )

                    context.getActivity()?.runOnUiThread {
                        (context.getActivity() as? BaseSlideShow)?.changeMusicData(musicReturnInfo)
                        (context.getActivity() as? ImageSlideShowActivity)?.dismissLoadingDialog()
                    }
                    dismiss()
                } catch (e: Exception) {
                    context.getActivity()?.runOnUiThread {
                        (context.getActivity() as? ImageSlideShowActivity)?.dismissLoadingDialog()
                        Timber.e(e)
                        Firebase.crashlytics.recordException(e)
                        Toast.makeText(
                            context.getActivity(),
                            context.getString(R.string.error_try_another_music_file),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }
        }.start()


    }

    tailrec fun Context.getActivity(): Activity? =
        this as? Activity ?: (this as? ContextWrapper)?.baseContext?.getActivity()

    fun showBottomSheet() {
        isVisible = true
        doOnLayout { animation.start(true) }
    }

    fun dismiss() {
        layoutBinding.rangeSeekbar.selectedMaximumVal = 100f
        layoutBinding.rangeSeekbar.selectedMinimumVal = 0f
        (context.getActivity() as ImageSlideShowActivity).musicPlayer.pause()
        animation.start(false) {
            isVisible = false
        }
        musicCallback?.onDismissed()
    }

    fun setCallback(callback: Callback?) {
        this.musicCallback = callback
    }

    fun setAudioData(audioInfo: AudioInfo) {
        val audioModel = AudioModel(audioInfo)
        (context.getActivity() as? ImageSlideShowActivity)?.musicPlayer?.changeMusic(audioInfo.filePath)
        this.audioModel = audioModel
        layoutBinding.tvMusicName.text = audioInfo.musicName
        layoutBinding.tvMusicDuration.text = audioModel.durationInString
        layoutBinding.tvStart.text = "00:00"
        layoutBinding.tvEnd.text = audioModel.durationInString
    }

    interface Callback {
        fun onDismissed()
    }
}
