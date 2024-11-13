package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.gson.Gson
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.model.EventInfo
import com.parallax.hdvideo.wallpapers.data.model.NotificationModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.utils.AppConstants.PreferencesKey.LATEST_NOTIFICATION_ONLINE_DATA
import com.parallax.hdvideo.wallpapers.utils.AppConstants.PreferencesKey.LATEST_NOTIFICATION_ONLINE_ID
import com.parallax.hdvideo.wallpapers.utils.Logger.d
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class DownloadNotificationWorker @AssistedInject constructor(val storage: LocalStorage,
                                                             val api: ApiClient,
                                                             @Assisted context: Context,
                                                             @Assisted workerParams: WorkerParameters
) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        try {
            WallpaperURLBuilder.shared.setConfigUrl(storage)
            val newestId = storage.getString(LATEST_NOTIFICATION_ONLINE_ID) ?: ""
            val url = String.format(RemoteConfig.commonData.notifyUrl, RemoteConfig.countryName, RemoteConfig.appId, WallpaperURLBuilder.shared.languageCountry)
            val responseData = api.getSimpleString(url).execute()
            val bodyResponse = responseData.body()
            if (responseData.isSuccessful && bodyResponse != null) {
                val gson = Gson()
                val eventInfo = gson.fromJson(bodyResponse, EventInfo::class.java)
                if (eventInfo.id != newestId) {
                    storage.putString(LATEST_NOTIFICATION_ONLINE_ID, eventInfo.id)
                    val nextEvent = filterNotifyModel(eventInfo.dataEvents)
                    if (nextEvent != null) {
                        OnlineNotificationWorker.cancel()
                        OnlineNotificationWorker.schedule(nextEvent)
                        storage.putData(LATEST_NOTIFICATION_ONLINE_DATA, eventInfo.dataEvents)
                    }
                }
            }
        } catch (e: Exception) {
            d(TAG, e)
        }
        nextSchedule()
        return Result.success()
    }

    private fun nextSchedule() {
        val calendarInstance = Calendar.getInstance()
        val curTime = calendarInstance.timeInMillis
        calendarInstance.add(Calendar.DAY_OF_YEAR, 1)
        val delay = calendarInstance.timeInMillis - curTime
        schedule(delay)
    }

    companion object {
        const val TAG = "DownloadNotificationDataWorker"
        private fun schedule(millis: Long) {
            d("NotificationOnline", "delay = $millis")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work: OneTimeWorkRequest = OneTimeWorkRequest.Builder(
                DownloadNotificationWorker::class.java
            )
                .setInitialDelay(millis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(WallpaperApp.instance)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, work)
        }

        fun schedule() {
            WorkManager.getInstance(WallpaperApp.instance).cancelUniqueWork(TAG)
            schedule(10_000)
        }

        fun filterNotifyModel(list: MutableList<NotificationModel>) : NotificationModel? {
            val curCalendar = Calendar.getInstance()
            val curTime = curCalendar.timeInMillis
            val curHour = curCalendar[Calendar.HOUR_OF_DAY]
            val curMinute = curCalendar[Calendar.MINUTE]
            val oneDayInMillis = (1000 * 24 * 3600).toLong()
            var size = list.size
            var i = 0
            val delta = 30000 // 30 seconds
            var result: NotificationModel? = null
            // để lấy sự kiện gần nhất
            var minMillis = 0L
            while (size > i) {
                val model = list[i]
                val calendar = model.getCalendar()
                if (calendar != null) {
                    val millis = calendar.timeInMillis
                    if (calendar[Calendar.HOUR_OF_DAY] == 0) {
                        val count = millis / oneDayInMillis
                        if (count < curTime / oneDayInMillis) {
                            list.removeAt(i)
                        } else if (count > curTime / oneDayInMillis) {
                            i++
                            if (minMillis == 0L || minMillis > millis) {
                                minMillis = millis
                                result = model
                            }
                        } else {
                            // xóa nếu event cùng ngày hiện tại và giờ hiện tại > 21
                            if (curHour > 21) list.removeAt(i)
                            else {
                                i++
                                if (minMillis == 0L || minMillis > millis) {
                                    minMillis = millis
                                    result = model
                                }
                            }
                        }
                    } else {
                        if (millis < curTime + delta) {
                            // thời gian cùng ngày nhỏ hơn 21h20, khi đó thời gian sẽ dc hẹn + 30 phút
                            if (((curHour == 21 && curMinute <= 20) || curHour < 21)
                                && OnlineNotificationWorker.isSameDate(calendar, curCalendar)) {
                                model.removeHours()
                                i++
                                if (minMillis == 0L || minMillis > millis) {
                                    minMillis = millis
                                    result = model
                                }
                            } else list.removeAt(i)
                        } else {
                            i++
                            if (minMillis == 0L || minMillis > millis) {
                                minMillis = millis
                                result = model
                            }
                        }
                    }
                } else {
                    list.removeAt(i)
                }
                size = list.size
            }
            return result
        }
    }
}