package com.bloodpressure.app.fcm

import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DeleteMenuNotificationBroadcastReceiver : ScheduleNotificationBroadcastReceiver(),
    KoinComponent {
    private val notificationController: NotificationController by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            notificationController.cancelMenuNotification()
        }
        super.onReceive(context, intent)
    }
}
