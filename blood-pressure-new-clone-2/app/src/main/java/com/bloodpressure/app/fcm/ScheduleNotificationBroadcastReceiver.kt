package com.bloodpressure.app.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bloodpressure.app.data.remote.RemoteConfig

open class ScheduleNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val remoteConfig = RemoteConfig(context)
            val timeMillis = remoteConfig.getShowNotificationTimeMillis()
            if (timeMillis > 0) {
                val notificationScheduler = NotificationScheduler(context)
                notificationScheduler.scheduleNotification(timeMillis)
            }
        }
    }
}
