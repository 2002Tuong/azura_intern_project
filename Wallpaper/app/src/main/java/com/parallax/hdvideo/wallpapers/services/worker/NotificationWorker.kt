package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.DefaultManager
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(private val localStorage: LocalStorage,
                                                     @Assisted context: Context,
                                                     @Assisted workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        buildNotification()
        val nextTimeInMillis = calculatedNextTime
        val currentTimeInMillis = Calendar.getInstance().timeInMillis
        localStorage.putData(AppConstants.PreferencesKey.LAST_TIME_NOTIFY, currentTimeInMillis)
        val distance = nextTimeInMillis - currentTimeInMillis
        schedule(distance)
        Logger.d("NotificationWorker Schedule Replay = ", nextTimeInMillis, distance)
        return Result.success()
    }

    companion object {
        const val TAGS = "NotificationWeekendWorker"
        private const val CURRENT_TAG = "CURRENT_TAG"

        // delay MILLISECONDS
        fun schedule(delay: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints.build())
                .setInputData(workDataOf(CURRENT_TAG to TAGS))
                .build()
            WorkManager.getInstance(WallpaperApp.instance)
                .enqueueUniqueWork(TAGS, ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel() {
            WorkManager.getInstance(WallpaperApp.instance).cancelUniqueWork(TAGS)
        }

        fun startSchedule(storage: LocalStorage) {

            val calendar = Calendar.getInstance()
            when(calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.FRIDAY, Calendar.SATURDAY -> calendar.add(Calendar.DATE, 1)
                Calendar.SUNDAY -> {
                    calendar.add(Calendar.DATE, 6) // NEXT SATURDAY
                }
                else -> calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            }
            val hour = 7 + Random().nextInt(15) // from 7h to 21h
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            val lastTime = storage.getData(AppConstants.PreferencesKey.LAST_TIME_NOTIFY, Long::class) ?: 0
            // Kiểm tra lần notification gần nhất.
            // Nếu trùng tuần của năm thì cho vào tuần sau
            if (lastTime > 0) {
                val lastCalendar = Calendar.getInstance()
                lastCalendar.timeInMillis = lastTime
                if (lastCalendar.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR)
                    && lastCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            val timeInMillis = calendar.timeInMillis
            val delay = timeInMillis - Calendar.getInstance().timeInMillis
            Logger.d("NotificationWorker startSchedule = ", timeInMillis, delay)
            schedule(delay)
        }

        private val calculatedNextTime: Long get() {
            val cal = Calendar.getInstance()
            when(cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                else -> cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            }
            val hour = 7 + Random().nextInt(15) // from 7h to 21h
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, 0)
            return cal.timeInMillis
        }

        private val weekend: Boolean get() {
            val calendar = Calendar.getInstance()
            val todayInWeek = calendar.get(Calendar.DAY_OF_WEEK)
            return (todayInWeek == Calendar.FRIDAY
                    || todayInWeek == Calendar.SATURDAY
                    || todayInWeek == Calendar.SUNDAY)
        }

        private fun buildNotification() {
            if (weekend) {
                val notificationModel = DefaultManager.loadNotificationData() ?: return
                NotificationUtils.push(
                    bundleOf(NotificationUtils.TITLE to notificationModel.name,
                        NotificationUtils.MESSAGE to notificationModel.description,
                        NotificationUtils.DATA to notificationModel.objectId,
                        NotificationUtils.ACTION to TAGS))
            }
        }
    }
}