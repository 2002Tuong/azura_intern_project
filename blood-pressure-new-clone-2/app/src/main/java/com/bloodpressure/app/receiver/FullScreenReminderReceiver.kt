package com.bloodpressure.app.receiver

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.bloodpressure.app.BuildConfig
import com.bloodpressure.app.FullscreenReminderActivity
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.OpenAdsManager
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.screen.fullscreenreminder.FULLSCREEN_REMINDER_TYPE_1
import com.bloodpressure.app.screen.fullscreenreminder.FULLSCREEN_REMINDER_TYPE_2
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.ReminderUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class FullScreenReminderReceiver : BroadcastReceiver(), KoinComponent {
    private val openAdsManager: OpenAdsManager by inject()
    private val notificationController: NotificationController by inject()
    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d("FullScreenReminderReceiver onReceived")
        val keyguardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (!openAdsManager.isActivityInForeground && (!(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive || keyguardManager.isKeyguardLocked)) {
            val type = if (currentHour > 3) {
                FULLSCREEN_REMINDER_TYPE_2
            } else {
                FULLSCREEN_REMINDER_TYPE_1
            }
            val intent2 = Intent(context, FullscreenReminderActivity::class.java)
            intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val extras = intent?.extras ?: Bundle()
            extras.putInt(FullscreenReminderActivity.ARG_REMINDER_TYPE, type)
            intent2.putExtras(extras)

            val activity =
                PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val fullScreenIntent = NotificationCompat.Builder(
                context,
                NotificationController.FULLSCREEN_REMINDER_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_notification_round)
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