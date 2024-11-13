package com.slideshowmaker.slideshow.models.ffmpeg

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.exceptions.FFmpegException
import com.slideshowmaker.slideshow.utils.Logger


class FFmpeg(val cmd: Array<String>) {

    @SuppressLint("InvalidWakeLockTag")
    fun runCmd(onComplete: (Boolean) -> Unit) {
        val pmInstance = VideoMakerApplication.getContext()
            .getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pmInstance.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Lock")
        wakeLock.acquire()
        FFmpegKitConfig.enableStatisticsCallback { newStatistics ->
            Logger.e("time = ${newStatistics.time} , speed = ${newStatistics.speed}")
            // publishProgress((newStatistics.time.toFloat() / totalDuration * 100).toInt())
        }
        FFmpegKit.executeWithArgumentsAsync(cmd) {
            val returnCode: ReturnCode = it.returnCode
            if (returnCode.isValueError) {
                if (!returnCode.isValueCancel) {
                    FirebaseCrashlytics.getInstance()
                        .recordException(FFmpegException("Unable complete ffmpeg because ${it.failStackTrace}, status code is :${returnCode}"))
                }
                onComplete.invoke(false)
            } else if (returnCode.isValueSuccess) {
                onComplete.invoke(true)
            } else {
                onComplete.invoke(false)
            }
        }

        /*            when (status) {
                        FFmpeg.RETURN_CODE_SUCCESS -> {

                        }
                        FFmpeg.RETURN_CODE_CANCEL -> {

                        }
                        else -> {
                            val op = FFmpeg.getLastCommandOutput()
                            Logger.e(op)
                        }
                    }*/
    }

    @SuppressLint("InvalidWakeLockTag")
    fun runCmd(onUpdateProgress: (Int) -> Unit, onComplete: (Boolean) -> Unit) {
        val pmInstance = VideoMakerApplication.getContext()
            .getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pmInstance.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Lock")
        wakeLock.acquire()
        FFmpegKitConfig.enableStatisticsCallback { newStatistics ->
            Logger.e("time = ${newStatistics.time} , speed = ${newStatistics.speed}")
            onUpdateProgress.invoke(newStatistics.time)
            // publishProgress((newStatistics.time.toFloat() / totalDuration * 100).toInt())
        }
        FFmpegKit.executeWithArgumentsAsync(cmd) {
            val returnCode: ReturnCode = it.returnCode
            if (returnCode.isValueError) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception("Unable complete ffmpeg because ${it.failStackTrace}"))
                onComplete.invoke(false)
            } else if (returnCode.isValueSuccess) {
                onComplete.invoke(true)
            } else if (returnCode.isValueSuccess) {
                onComplete.invoke(false)
            }
        }
    }

    fun cancel() {
        FFmpegKit.cancel()
    }

}