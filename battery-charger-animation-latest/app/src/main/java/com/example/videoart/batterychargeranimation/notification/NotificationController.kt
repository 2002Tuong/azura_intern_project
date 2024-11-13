package com.example.videoart.batterychargeranimation.notification

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
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FLAG_ONGOING_EVENT
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.R

class NotificationController(
    private val context: Context
) {

    var notificationCreated: Boolean = false
    fun showMenuNotification() {
        createNotificationChannel()
        val customLayout =
            RemoteViews(context.packageName, R.layout.menu_notification_layout).apply {
            }


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
        ).setSmallIcon(R.drawable.app_icon)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null)
                notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
            val name = "Notification"
            val description = "Notification Channel"
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
            val name = "Notification"
            val description = "Reminder"
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
        const val FULLSCREEN_REMINDER_CHANNEL_ID = "calltheme_fullscreen_reminder_channel"
        private const val MENU_NOTIFICATION_ID = 1
    }
}
