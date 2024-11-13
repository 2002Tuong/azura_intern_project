package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import android.graphics.Bitmap
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.model.NotificationModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.utils.AppConstants.PreferencesKey.LATEST_NOTIFICATION_ONLINE_DATA
import com.parallax.hdvideo.wallpapers.utils.Logger.d
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

@HiltWorker
class OnlineNotificationWorker @AssistedInject constructor(val storage: LocalStorage,
                                                           val api: ApiClient,
                                                           @Assisted context: Context,
                                                           @Assisted workerParams: WorkerParameters
) :
    RxWorker(context, workerParams) {
    var textSearch = ""
    override fun createWork(): Single<Result> {
        val string = storage.getString(LATEST_NOTIFICATION_ONLINE_DATA) ?: ""
        return if (string.isNotEmpty()) {
            Single.just(string).flatMap {
                var notificationModel: NotificationModel? = null
                var listModel: MutableList<NotificationModel> = mutableListOf()
                try {
                    WallpaperURLBuilder.shared.setConfigUrl(storage)
                    val id = inputData.getString(TAGS) ?: ""
                    listModel = Gson().fromJson(it, object : TypeToken<List<NotificationModel>>() {}.type)
                    notificationModel = getEvent(listModel, id)
                } catch (e: Exception) { }
                if (notificationModel != null) {
                    textSearch = notificationModel.objectId.replace("keysearch:", "")
                    val url = WallpaperURLBuilder.shared.getSearchUrl(textSearch, storage.sex)
                    api.getListWallpapersFromCategory(url, 2 * 3600_000L)  // 2 hours
                        .map { res ->
                            val size = min(res.data.size, 10)
                            if (size > 0) {
                                val wallpaperModel = res.data[Random().nextInt(size)]
                                val bitmap = GlideSupport.getBitmap(wallpaperModel.toUrl(isMin = false, isThumb = true))
                                pushNotification(notificationModel, wallpaperModel, bitmap)
                            }
                            nextSchedule(listModel)
                            Result.success()
                        }.onErrorReturn {
                            nextSchedule(listModel)
                            Result.success()
                        }
                } else {
                    nextSchedule(listModel)
                    Single.just(Result.success())
                }
            }
        } else Single.just(Result.success())
    }

    private fun nextSchedule(list: MutableList<NotificationModel>) {
        val nextModel = DownloadNotificationWorker.filterNotifyModel(list)
        if (nextModel != null) {
            storage.putData(LATEST_NOTIFICATION_ONLINE_DATA, list)
            schedule(nextModel)
        }
    }

    private fun getEvent(list: MutableList<NotificationModel>, eventId: String): NotificationModel? {
        for (i in list.indices) {
            val model: NotificationModel = list[i]
            if (model.id == eventId) {
                return list.removeAt(i)
            }
        }
        return null
    }

    private fun pushNotification(model: NotificationModel, wallpaperModel: WallpaperModel, bitmap: Bitmap?) {
        val couple = textSearch == RemoteConfig.commonData.hashTagCouple
        NotificationUtils.push(
            bundleOf(NotificationUtils.TITLE to model.name,
                NotificationUtils.MESSAGE to model.description,
                NotificationUtils.DATA to if (couple) textSearch else Gson().toJson(wallpaperModel),
                NotificationUtils.ACTION to if (couple) NotificationWorker.TAGS else TAGS
            ), bitmap
        )
    }

    companion object {
        const val TAGS = "NotificationOnlineWorker"
        fun schedule(model: NotificationModel?) {
            if (model == null) return
            val calendarInstance: Calendar = model.getCalendar() ?: return
            val curCalendar = Calendar.getInstance()
            val curTime = curCalendar.timeInMillis
            val curHour = curCalendar.get(Calendar.HOUR_OF_DAY)
            if (calendarInstance[Calendar.HOUR_OF_DAY] == 0) {
                if (isSameDate(curCalendar, calendarInstance)) {
                    calendarInstance.timeInMillis = curTime
                    if (curHour < 7) {
                        calendarInstance.set(Calendar.HOUR_OF_DAY, 7)
                    } else {
                        calendarInstance.add(Calendar.MINUTE, 30)
                    }
                } else {
                    val hour = 7 + Random().nextInt(10)
                    calendarInstance.add(Calendar.HOUR_OF_DAY, hour)
                }
            }
            val inputData = Data.Builder().putString(TAGS, model.id).build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val delay = calendarInstance.timeInMillis - curTime
            val work: OneTimeWorkRequest =
                OneTimeWorkRequest.Builder(OnlineNotificationWorker::class.java)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build()
            WorkManager.getInstance(WallpaperApp.instance)
                .enqueueUniqueWork(TAGS, ExistingWorkPolicy.REPLACE, work)
            d(TAGS, "schedule")
        }

        fun isSameDate(calendarA: Calendar, calendarB: Calendar): Boolean {
            return (calendarA[Calendar.DAY_OF_YEAR] == calendarB[Calendar.DAY_OF_YEAR]
                    && calendarA[Calendar.YEAR] == calendarB[Calendar.YEAR])
        }

        fun cancel() {
            WorkManager.getInstance(WallpaperApp.instance).cancelUniqueWork(TAGS)
        }
    }
}