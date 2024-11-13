package com.artgen.app.receiver

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.artgen.app.R
import com.artgen.app.log.Logger
import com.artgen.app.ui.FullscreenReminderActivity
import com.artgen.app.utils.NotificationController
import com.artgen.app.utils.ReminderUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class FullScreenReminderReceiver : BroadcastReceiver(), KoinComponent {
    private val notificationController: NotificationController by inject()
    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d("FullScreenReminderReceiver onReceive")
        val keyguardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!ReminderUtils.isShowing && (!(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive || keyguardManager.isKeyguardLocked)) {
            val intent2 = Intent(context, FullscreenReminderActivity::class.java)
            intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val extras = intent?.extras ?: Bundle()
            intent2.putExtras(extras)

            val activity =
                PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            val fullScreenIntent = NotificationCompat.Builder(
                context,
                NotificationController.FULLSCREEN_REMINDER_CHANNEL_ID
            )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(activity, true)
            notificationController.cancelMenuNotification(10000)
            val build = fullScreenIntent.build()
            notificationController.notify(context, 10000, build)
        }

        ReminderUtils.scheduleNextFullScreenReminder(context)
    }

}