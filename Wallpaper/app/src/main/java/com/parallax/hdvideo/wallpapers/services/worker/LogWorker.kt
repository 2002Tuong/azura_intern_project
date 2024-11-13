package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.TPMetricsLoggerSupport
import com.parallax.hdvideo.wallpapers.utils.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LogWorker @AssistedInject constructor(private val localStorage: LocalStorage,
                                            @Assisted context: Context,
                                            @Assisted workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        Logger.d("start LoggerWork")
        val tagValue = inputData.getString(TPMetricsLoggerSupport.KEY_LOGGER_WORK) ?: return Result.success()
        val info = localStorage.getString(tagValue) ?: return Result.success()
        Logger.d("LoggerWork data = ", info)
        WallpaperURLBuilder.shared.setConfigUrl(localStorage)
//        val list = fromJson<List<TPMetricsLoggerHelper.LogEvent>>(data)
//        TPMetricsLoggerHelper.pushData(list)
        localStorage.remove(tagValue)
        return Result.success()
    }
}