package com.artgen.app.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.S
import android.text.Html
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FLAG_ONGOING_EVENT
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import com.artgen.app.R
import com.artgen.app.fcm.DeleteMenuNotificationBroadcastReceiver
import com.artgen.app.fcm.ScheduleNotificationBroadcastReceiver
import com.artgen.app.ui.MainActivity

class NotificationController(
    private val context: Context
) {

    var notificationCreated: Boolean = false
    fun showMenuNotification() {
        createNotificationChannel()
        val customLayout =
            RemoteViews(context.packageName, R.layout.menu_notification_layout).apply {
            }

        customLayout.setTextViewText(
            R.id.title,
            Html.fromHtml(context.getString(R.string.notification_your_best))
        )

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = NotificationCompat.Builder(
            context, NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_notification_small)
            .apply {
                if (Build.VERSION.SDK_INT >= S) {
                    setCustomContentView(customLayout)
                    setCustomBigContentView(customLayout)
                } else {
                    setContent(customLayout)
                }
            }
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .build().apply {
                flags = Notification.FLAG_NO_CLEAR or FLAG_ONGOING_EVENT
            }
        showNotification(notification, MENU_NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context?, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Alarm Channel"
            val channelDescription = "Notifications for triggered alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = context?.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun cancelMenuNotification() {
        with(NotificationManagerCompat.from(context)) {
            cancel(MENU_NOTIFICATION_ID)
        }
    }

    fun cancelMenuNotification(channelId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(channelId)
        }
    }

    private fun showNotification(notification: Notification, id: Int) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(id, notification)
            notificationCreated = true
        }
    }


    private fun createDismissMenuNotificationPendingIntent(): PendingIntent {
        val intent = Intent(context, DeleteMenuNotificationBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createDeleteMenuNotificationPendingIntent(): PendingIntent {
        val intent = Intent(context, ScheduleNotificationBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null)
                notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
            val name = context.getString(R.string.notification_channel_name)
            val description = context.getString(R.string.notification_channle_des)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                this.description = description
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)

        }
    }


    private fun createFullscreenReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val description = context.getString(R.string.reminder_channel_des)
            val channel = NotificationChannel(
                FULLSCREEN_REMINDER_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = description
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notify(context: Context, id: Int, build: Notification) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                createFullscreenReminderChannel()
                notify(id, build)
            }

        }
    }


    companion object {
        const val NOTIFICATION_CHANNEL_ID = "main_notification_channel"
        const val FULLSCREEN_REMINDER_CHANNEL_ID = "fullscreen_reminder_channel"
        private const val MENU_NOTIFICATION_ID = 1
    }
}
