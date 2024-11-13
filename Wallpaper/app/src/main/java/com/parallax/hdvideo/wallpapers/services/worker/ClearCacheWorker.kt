package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ClearCacheWorker @AssistedInject constructor(val database: AppDatabase,
                                                   @Assisted context: Context,
                                                   @Assisted workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    override fun doWork(): Result {
        try {
            FileUtils.deleteAppCache()
            database.cacheDao().deleteAll()
//            GlideHelper.clearDiskCache()
        }catch (e: Exception) { }
        Logger.d("end ClearDataWorker")
        return Result.success()
    }

    companion object {
        fun start() {
            Logger.d("start ClearDataWorker")
            val work = OneTimeWorkRequestBuilder<ClearCacheWorker>()
            WorkManager.getInstance(WallpaperApp.instance)
                .enqueueUniqueWork("ClearDataWorker",
                    ExistingWorkPolicy.REPLACE,
                    work.build())
        }
    }
}