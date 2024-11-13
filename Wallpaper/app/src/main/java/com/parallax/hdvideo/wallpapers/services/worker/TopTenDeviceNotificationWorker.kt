package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class TopTenDeviceNotificationWorker @AssistedInject constructor(
    private val localStorage: LocalStorage,
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val hashtags = workerParams.inputData.getString(TAG_HASHTAG)
        val nameOfDevice = workerParams.inputData.getString(TAG_DEVICE_NAME)
        if(!localStorage.didNotifyTopTenDevice) {
            val deviceSpec = hashtags to nameOfDevice
            val notificationInfo = Bundle()
            notificationInfo.putString(NotificationUtils.TITLE, "Exclusive Wallpaper made for your ${deviceSpec.second} ")
            notificationInfo.putString(NotificationUtils.DATA, deviceSpec.second)
            notificationInfo.putString(NotificationUtils.MESSAGE, "Open the app to explore them!")
            notificationInfo.putString(NotificationUtils.ACTION, ACTION)
            NotificationUtils.push(notificationInfo)
            localStorage.didNotifyTopTenDevice = true
        }
        return Result.success()
    }


    companion object {
        private const val TAG_HASHTAG = "hashtag"
        private const val TAG_DEVICE_NAME = "deviceName"
        const val ACTION = "DEVICE_WALLPAPER_SEARCH"
        fun schedule() {
            val deviceInfo = AppConfiguration.topTenDeviceInfo
            val context = WallpaperApp.instance
            val calendarInstance = Calendar.getInstance()
            val curDate = Date()
            calendarInstance.set(Calendar.HOUR_OF_DAY, 7 + RemoteConfig.commonData.deviceWallpaperNotificationRecurrenceRate)
            val distanceHours = (calendarInstance.time.time - curDate.time) / 3600
            val worker = OneTimeWorkRequestBuilder<TopTenDeviceNotificationWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(TAG_HASHTAG,deviceInfo?.first)
                        .putString(TAG_DEVICE_NAME, deviceInfo?.second)
                        .build()
                )
                .setInitialDelay(
                    if(BuildConfig.DEBUG) 2 else distanceHours,
                    if(BuildConfig.DEBUG) TimeUnit.MINUTES else TimeUnit.HOURS
                )
                .build()
            WorkManager.getInstance(context)
                .beginWith(worker)
                .enqueue()
            }
        }
    }