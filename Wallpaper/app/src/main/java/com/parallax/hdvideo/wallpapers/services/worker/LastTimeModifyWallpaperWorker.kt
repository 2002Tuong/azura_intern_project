package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class LastTimeModifyWallpaperWorker @AssistedInject constructor(private val storage: LocalStorage,
                                                                @Assisted context: Context,
                                                                @Assisted workerParams: WorkerParameters
) : Worker(context, workerParams) {

    //4 types of notification, each has 3 types of content
    private val listDay1NotificationContent = arrayOf(
        Pair(R.string.dayOneNotificationTitle001, R.string.dayOneNotificationMessage001),
        Pair(R.string.dayOneNotificationTitle002, R.string.dayOneNotificationMessage002),
        Pair(R.string.dayOneNotificationTitle003, R.string.dayOneNotificationMessage003)
    )

    private val listDay2NotificationContent = arrayOf(
        Pair(R.string.dayTwoNotificationTitle001, R.string.dayTwoNotificationMessage001),
        Pair(R.string.dayTwoNotificationTitle002, R.string.dayTwoNotificationMessage002),
        Pair(R.string.dayTwoNotificationTitle003, R.string.dayTwoNotificationMessage003)
    )

    private val listDay4NotificationContent = arrayOf(
        Pair(R.string.dayFourNotificationTitle001, R.string.dayFourNotificationMessage001),
        Pair(R.string.dayFourNotificationTitle002, R.string.dayFourNotificationMessage002),
        Pair(R.string.dayFourNotificationTitle003, R.string.dayFourNotificationMessage003)
    )

    private val listDay7NotificationContent = arrayOf(
        Pair(R.string.daySevenNotificationTitle001, R.string.daySevenNotificationMessage001),
        Pair(R.string.daySevenNotificationTitle002, R.string.daySevenNotificationMessage002),
        Pair(R.string.daySevenNotificationTitle003, R.string.daySevenNotificationMessage003)
    )

    private val listWeekNotificationContent = arrayOf(
        Pair(R.string.weeklyNotificationTitle001, R.string.weeklyNotificationMessage001),
        Pair(R.string.weeklyNotificationTitle002, R.string.weeklyNotificationMessage002),
        Pair(R.string.weeklyNotificationTitle003, R.string.weeklyNotificationMessage003),
        Pair(R.string.weeklyNotificationTitle004, R.string.weeklyNotificationMessage004)
    )

    override fun doWork(): Result {
        pushNotification()
        reschedule()
        return Result.success()
    }

    private fun reschedule() {
        try {
            val position = storage.contextChangedWallpaperIndex
            if (position < 4) storage.contextChangedWallpaperIndex = position + 1
            schedule()
        } catch (e: Exception) { }
    }

    private fun pushNotification() {
        try {
            val arrayIndex = storage.indexContentForNotificationType
            val curIndex = storage.contextChangedWallpaperIndex
            val contentIndex = arrayIndex[curIndex]
            val listData = when (curIndex) {
                0 -> listDay1NotificationContent
                1 -> listDay2NotificationContent
                2 -> listDay4NotificationContent
                3 -> listDay7NotificationContent
                else -> listWeekNotificationContent
            }[contentIndex]
            //3 - there are 4 listWeeklyNotificationContent -> when max index = 3 reset to 0 (first one)
            //2 - there are 3 day notification content -> when max index = 2 reset to 0 (first one)
            //check DWT-537 for more details
            arrayIndex[curIndex] = if (contentIndex >= 3 || (curIndex < 4 && contentIndex == 2)) {
                0
            } else {
                contentIndex + 1
            }
            storage.indexContentForNotificationType = arrayIndex
            val bundle = Bundle()
            bundle.putString(NotificationUtils.TITLE, applicationContext.getString(listData.first))
            bundle.putString(NotificationUtils.MESSAGE, applicationContext.getString(listData.second))
            bundle.putString(NotificationUtils.ACTION, TAGS)
            bundle.putString(NotificationUtils.DATA, curIndex.toString())
            NotificationUtils.push(bundle)
        } catch (e: Exception) {

        }
    }
    companion object {
        const val TAGS = "LastTimeChangedWallpaperWorker"
        // delay hours
        fun schedule() {
            val listHour = RemoteConfig.commonData.scenarioChangedWallpaper.split(",")
            if (listHour.isEmpty()) return
            val delay = listHour[WallpaperApp.instance.localStorage.contextChangedWallpaperIndex].toIntOrNull() ?: return
            if (delay <= 0) return
            val calendar = Calendar.getInstance()
            val curTime = calendar.timeInMillis
            calendar.add(Calendar.HOUR_OF_DAY, delay)
            val hourInDay = calendar[Calendar.HOUR_OF_DAY]
            if (hourInDay >= 22) {
                calendar.add(Calendar.HOUR_OF_DAY, -4) // from 18h to 20h
            } else if (hourInDay <= 7) {
                calendar[Calendar.HOUR_OF_DAY] = 7 // 8h
            }
            val delta = calendar.timeInMillis - curTime
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val work: OneTimeWorkRequest =
                    OneTimeWorkRequest.Builder(LastTimeModifyWallpaperWorker::class.java)
                            .setInitialDelay(delta, TimeUnit.MILLISECONDS)
                            .setConstraints(constraints)
                            .build()
            WorkManager.getInstance(WallpaperApp.instance).enqueueUniqueWork(TAGS, ExistingWorkPolicy.REPLACE, work)
        }

        fun cancel() {
            WorkManager.getInstance(WallpaperApp.instance).cancelUniqueWork(TAGS)
        }
    }

}