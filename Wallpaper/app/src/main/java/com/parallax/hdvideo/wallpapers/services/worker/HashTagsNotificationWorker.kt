package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import android.graphics.Bitmap
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.gson.Gson
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.DefaultManager
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.main.MainViewModel
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@HiltWorker
class HashTagsNotificationWorker @AssistedInject constructor(private val storage: LocalStorage,
                                                             private val api: ApiClient,
                                                             private val database: AppDatabase,
                                                             @Assisted context: Context,
                                                             @Assisted workerParams: WorkerParameters
) : RxWorker(context, workerParams) {


    override fun createWork(): Single<Result> {
        return Single.fromCallable {
            WallpaperURLBuilder.shared.setConfigUrl(storage)
            database.hashTagsDao()
                .getBestHashTags(isImage = true, limit = RemoteConfig.commonData.bestHashTagNum)
        }.flatMap {
            val expirationTime = 2 * 3600_000L
            val topDownUrl = WallpaperURLBuilder.shared.getTopDownUrl(storage.sex)
            if (it == RemoteConfig.hashTagDefault) {
                api.getListWallpapersFromCategory(topDownUrl, expirationTime)
            } else {
                val url = WallpaperURLBuilder.shared.getHashTagsUrl(it, storage.sex,
                    pageNumber = 1, isHashTag = true, isNotify = true)
                api.getListWallpapersFromCategory(url, expirationTime).flatMap { res ->
                    if (res.data.isEmpty()) {
                        api.getListWallpapersFromCategory(topDownUrl, expirationTime)
                    } else Single.just(res)
                }.onErrorReturnItem(ResponseModel<WallpaperModel>())
            }
        }.onErrorReturnItem(ResponseModel<WallpaperModel>())
            .map { res ->
                val wallpaperDao = database.wallpaper()
                val wallpaperModel = res.data.firstOrNull {
                    wallpaperDao.selectFirstItem(it.id) == null
                }
                if (wallpaperModel != null) {
                    wallpaperDao.insertItem(wallpaperModel)
                    val bitmap =
                        wallpaperModel.let { GlideSupport.getBitmap(it.toUrl(isMin = false, isThumb = true)) }
                    buildNotification(wallpaperModel, bitmap)
                }
                schedule(MainViewModel.setupNotificationHashTags(storage))
                Result.success()
            }
            .doOnError {
                schedule(MainViewModel.setupNotificationHashTags(storage))
            }.onErrorReturnItem(Result.success())
    }

    private fun buildNotification(wallpaperModel: WallpaperModel, bigPicture: Bitmap? = null) : Boolean {
        val notificationModel = DefaultManager.loadNotificationData() ?: return false
       return NotificationUtils.push(
            bundleOf(
                NotificationUtils.TITLE to notificationModel.name,
                NotificationUtils.MESSAGE to notificationModel.description,
                NotificationUtils.DATA to Gson().toJson(wallpaperModel),
                NotificationUtils.SUMMARY_TEXT to wallpaperModel.name,
                NotificationUtils.ACTION to TAGS
            ), bigPicture
        )
    }

    companion object {

        const val TAGS = "NotificationHashTagsWorker"

        // delay hours
        fun schedule(delay: Int) {
            if (delay <= 0) return
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
            val work = OneTimeWorkRequestBuilder<HashTagsNotificationWorker>()
                .setConstraints(constraints)
                .setInitialDelay(delay.toLong(), TimeUnit.HOURS)
            WorkManager.getInstance(WallpaperApp.instance)
                .enqueueUniqueWork(TAGS, ExistingWorkPolicy.REPLACE, work.build())
        }

        fun cancel() {
            WorkManager.getInstance(WallpaperApp.instance)
                .cancelUniqueWork(TAGS)
        }
    }
}