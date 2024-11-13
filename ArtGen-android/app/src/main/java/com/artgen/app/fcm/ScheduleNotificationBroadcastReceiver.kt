package com.artgen.app.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

open class ScheduleNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val timeMillis = 1000L
            if (timeMillis > 0) {
                val notificationScheduler = NotificationScheduler(context)
                notificationScheduler.scheduleNotification(timeMillis)
            }
        }
    }
}
