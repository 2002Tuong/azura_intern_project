package com.artgen.app.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.artgen.app.utils.NotificationController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShowMenuNotificationBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val notificationController: NotificationController by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            notificationController.showMenuNotification()
        }
    }
}
