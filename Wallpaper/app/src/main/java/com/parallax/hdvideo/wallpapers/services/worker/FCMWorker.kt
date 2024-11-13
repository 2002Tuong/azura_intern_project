package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.gson.Gson
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class FCMWorker @AssistedInject constructor(val storage: LocalStorage,
                                            val api: ApiClient,
                                            @Assisted context: Context,
                                            @Assisted workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val string = inputData.getString(TAGS)
        try {
            val inputData = Gson().fromJson(string, Map::class.java)
            NotificationUtils.push(
                bundleOf(NotificationUtils.TITLE to inputData[NotificationUtils.TITLE],
                    NotificationUtils.MESSAGE to inputData[NotificationUtils.MESSAGE],
                    NotificationUtils.DATA to string,
                    NotificationUtils.ACTION to TAGS
                )
                , image = inputData[NotificationUtils.IMAGE] as? String
            )
        } catch (e: Exception) {

        }
        return Result.success()
    }

    companion object {
        const val TAGS = "FCMWorker"

        /**
         * @param delay milliseconds
         */
        fun schedule(delay: Long, data: String) {
            val input = Data.Builder()
                .putString(TAGS, data)
                .build()
            val work = OneTimeWorkRequestBuilder<FCMWorker>()
                .setInputData(input)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            WorkManager.getInstance(WallpaperApp.instance)
                .enqueue(work.build())
        }
    }
}