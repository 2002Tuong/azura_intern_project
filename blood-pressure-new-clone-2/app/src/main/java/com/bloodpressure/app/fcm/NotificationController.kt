package com.bloodpressure.app.fcm

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
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.bloodpressure.app.MainActivity
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.screen.UriPattern
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.utils.formatTime
import com.bloodpressure.app.utils.getTimeFormattedString
import java.util.Calendar

class NotificationController(
    private val context: Context,
    private val bpRepository: BpRepository,
    private val hrRepository: HeartRateRepository
) {

    /*
        fun showMenuNotification() {
            val startMainActivityPendingIntent = createStartMainActivityPendingIntent()
            val notificationLayout =
                RemoteViews(context.packageName, R.layout.notification_custom_layout).apply {
                    setOnClickPendingIntent(R.id.custom_layout, startMainActivityPendingIntent)
                }
            val notificationLayoutExpanded =
                RemoteViews(context.packageName, R.layout.notification_custom_layout_expanded).apply {
                    setOnClickPendingIntent(R.id.go, startMainActivityPendingIntent)
                    setOnClickPendingIntent(R.id.not_now, createDismissMenuNotificationPendingIntent())
                }

            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDeleteIntent(createDeleteMenuNotificationPendingIntent())
                .setAutoCancel(false)
                .build().apply {
                    flags = Notification.FLAG_NO_CLEAR
                }

            showNotification(notification, MENU_NOTIFICATION_ID)
        }
    */

    fun showAlarmNotification(context: Context, alarmRecord: AlarmRecord) {
        val channelId = BP_DEFAULT_ALARM_CHANNEL_ID
        val notificationId = alarmRecord.createdAt.toInt()
        val pendingIntentUri = when (alarmRecord.type) {
            AlarmType.BLOOD_PRESSURE -> UriPattern.ADD_RECORD
            AlarmType.HEART_RATE -> UriPattern.HEART_RATE
            AlarmType.BLOOD_SUGAR -> UriPattern.BLOOD_SUGAR
            AlarmType.WEIGHT_BMI -> UriPattern.BMI
            AlarmType.WATER_REMINDER -> UriPattern.WATER_REMINDER
        }

        val pendingIntent = createStartSpecificScreenPendingIntent(pendingIntentUri)

        createNotificationChannel(context, channelId)

        val customNotificationView =
            RemoteViews(context.packageName, R.layout.alarm_notification_layout)
        customNotificationView.setTextViewText(R.id.tv_time, alarmRecord.formatTime())
        customNotificationView.setTextViewText(R.id.tv_desc, getContentText(alarmRecord.type))
        customNotificationView.setImageViewResource(R.id.iv_icon, alarmRecord.type.iconRes)
//
//        val pendingIntent =
//            PendingIntent.getActivity(
//                context,
//                notificationId,
//                Intent(context, MainActivity::class.java).apply {
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                },
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//            )
        customNotificationView.setOnClickPendingIntent(R.id.tv_action, pendingIntent)

        val customBigNotificationView =
            RemoteViews(context.packageName, R.layout.big_alarm_notification_layout)
        customBigNotificationView.setTextViewText(R.id.tv_time, alarmRecord.formatTime())
        customBigNotificationView.setTextViewText(R.id.tv_desc, getContentText(alarmRecord.type))
        customBigNotificationView.setInt(R.id.iv_img, "setBackgroundResource", alarmRecord.type.iconRes)

        val pendingIntentBig =
            PendingIntent.getActivity(
                context,
                notificationId,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        customBigNotificationView.setOnClickPendingIntent(R.id.tv_action, pendingIntent)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setColor(context.resources.getColor(android.R.color.white))
            .setColorized(true)

        notificationBuilder.setSmallIcon(R.drawable.ic_notification_small)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(context.getString(R.string.app_name))
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setCustomContentView(customNotificationView)
                .setCustomHeadsUpContentView(customNotificationView)
                .setCustomBigContentView(customBigNotificationView)
        } else {
            customNotificationView.setViewPadding(R.id.root_view, 32, 0, 32, 0)
            customBigNotificationView.setViewPadding(R.id.root_view, 32, 0, 32, 0)
            notificationBuilder.setCustomContentView(customNotificationView)
                .setCustomHeadsUpContentView(customBigNotificationView)
                .setCustomBigContentView(customBigNotificationView)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun buildDefaultReminderCustomNotificationLayout(
        title: String,
        content: String,
        pendingIntent: PendingIntent,
        ctaBtn: String,
        alarmVisible: Boolean = false
    ): RemoteViews {
        val pendingIntentHome = createStartSpecificScreenPendingIntent(UriPattern.HOME)
        val customNotificationView =
            RemoteViews(context.packageName, R.layout.default_reminder_custom_layout)
        customNotificationView.setTextViewText(R.id.title, title)
        customNotificationView.setTextViewText(R.id.content, content)
        customNotificationView.setTextViewText(R.id.tv_action, ctaBtn)
        customNotificationView.setViewVisibility(
            R.id.ivAlarm,
            if (alarmVisible) View.VISIBLE else View.GONE
        )
        customNotificationView.setOnClickPendingIntent(R.id.tv_action, pendingIntent)
        customNotificationView.setOnClickPendingIntent(R.id.root_view, pendingIntentHome)
        val currentTime = Calendar.getInstance().getTimeFormattedString("HH:mm")
        customNotificationView.setTextViewText(
            R.id.tvHeadline,
            context.getString(
                R.string.reminder_title, context.getString(R.string.app_name),
                currentTime
            )
        )
        return customNotificationView
    }

    private fun buildDefaultReminderNotification(
        context: Context,
        channelId: String,
        customView: RemoteViews,
        title: String,
        content: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setColor(context.resources.getColor(android.R.color.white))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(title)
            .setContentText(content)
            .setCustomContentView(customView)
            .setCustomBigContentView(customView)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())

    }

    fun showDefaultBpLevelReminderNotification(
        context: Context,
        title: String,
        content: String,
        btnString: String,
        uri: String
    ) {
        val notificationId = BP_DEFAULT_ALARM_NOTIFICATION_ID
        val channelId = BP_DEFAULT_ALARM_CHANNEL_ID
        val pendingIntent = createStartSpecificScreenPendingIntent(uri)

        val customNotificationView = buildDefaultReminderCustomNotificationLayout(
            title = title,
            content = content,
            pendingIntent = pendingIntent,
            ctaBtn = btnString
        )

        val notificationBuilder =
            buildDefaultReminderNotification(
                context,
                channelId,
                customNotificationView,
                title,
                content
            )

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && !canUseFullScreenIntent()
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun showDefaultBpRegularlyReminderNotification(
        context: Context,
        title: String,
        content: String
    ) {
        val notificationId = BP_DEFAULT_ALARM_NOTIFICATION_ID
        val channelId = BP_DEFAULT_ALARM_CHANNEL_ID
        val pendingIntent = createStartSpecificScreenPendingIntent(UriPattern.ADD_RECORD)

        val customNotificationView = buildDefaultReminderCustomNotificationLayout(
            title = title,
            content = content,
            pendingIntent = pendingIntent,
            ctaBtn = context.getString(R.string.record)
        )

        val notificationBuilder =
            buildDefaultReminderNotification(
                context,
                channelId,
                customNotificationView,
                title,
                content
            )

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && !canUseFullScreenIntent()
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun showDefaultBpReviewReminderNotification(context: Context, title: String, content: String) {
        val notificationId = BP_DEFAULT_ALARM_NOTIFICATION_ID
        val channelId = BP_DEFAULT_ALARM_CHANNEL_ID
        val pendingIntent = createStartSpecificScreenPendingIntent(UriPattern.HISTORY)

        val customNotificationView = buildDefaultReminderCustomNotificationLayout(
            title = title,
            content = content,
            pendingIntent = pendingIntent,
            ctaBtn = context.getString(R.string.review)
        )

        val notificationBuilder =
            buildDefaultReminderNotification(
                context,
                channelId,
                customNotificationView,
                title,
                content
            )

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && !canUseFullScreenIntent()
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun showDefaultBpAddReminderNotification(context: Context, title: String, content: String) {
        val notificationId = BP_DEFAULT_ALARM_NOTIFICATION_ID
        val channelId = BP_DEFAULT_ALARM_CHANNEL_ID
        val pendingIntent = createStartSpecificScreenPendingIntent(UriPattern.ADD_RECORD)

        val customNotificationView = buildDefaultReminderCustomNotificationLayout(
            title = title,
            content = content,
            pendingIntent = pendingIntent,
            ctaBtn = context.getString(R.string.record)
        )

        val notificationBuilder =
            buildDefaultReminderNotification(
                context,
                channelId,
                customNotificationView,
                title,
                content
            )

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && !canUseFullScreenIntent()
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun showMenuNotification() {
        val startAddRecordPendingIntent =
            createStartSpecificScreenPendingIntent(UriPattern.ADD_RECORD)
        val startHeartRatePendingIntent =
            createStartSpecificScreenPendingIntent(UriPattern.HEART_RATE)
        val startBloodSugarPendingIntent =
            createStartSpecificScreenPendingIntent(UriPattern.BLOOD_SUGAR)
        val startBMIPendingIntent =
            createStartSpecificScreenPendingIntent(UriPattern.BMI)
        val customLayout =
            RemoteViews(context.packageName, R.layout.menu_notification_layout).apply {
                setOnClickPendingIntent(R.id.blood_pressure_item, startAddRecordPendingIntent)
                setOnClickPendingIntent(R.id.heart_rate_item, startHeartRatePendingIntent)
                setOnClickPendingIntent(R.id.blood_sugar_item, startBloodSugarPendingIntent)
                setOnClickPendingIntent(R.id.bmi_item, startBMIPendingIntent)
            }

        val expandedCustomLayout =
            RemoteViews(context.packageName, R.layout.menu_notification_layout_expanded).apply {
                setOnClickPendingIntent(R.id.blood_pressure_item, startAddRecordPendingIntent)
                setOnClickPendingIntent(R.id.heart_rate_item, startHeartRatePendingIntent)
                setOnClickPendingIntent(R.id.blood_sugar_item, startBloodSugarPendingIntent)
                setOnClickPendingIntent(R.id.bmi_item, startBMIPendingIntent)
            }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customLayout)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDeleteIntent(createDeleteMenuNotificationPendingIntent())
            .setAutoCancel(false)
            .build().apply {
                flags = Notification.FLAG_NO_CLEAR
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

    private fun getContentText(alarmType: AlarmType): String {
        return when (alarmType) {
            AlarmType.BLOOD_PRESSURE -> "It's time to measure your blood pressure."
            AlarmType.HEART_RATE -> "It's time to check your heart rate."
            AlarmType.BLOOD_SUGAR -> "Time to monitor your blood sugar levels."
            AlarmType.WEIGHT_BMI -> "Time to track your weight and BMI."
//            AlarmType.CHOLESTEROL -> "It's time to monitor your cholesterol levels."
            AlarmType.WATER_REMINDER -> "Stay hydrated! Time to drink some water."
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
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(id, notification)
        }
    }

    private fun createStartMainActivityPendingIntent(): PendingIntent {
        val resultIntent = Intent(context, MainActivity::class.java)
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun createDismissMenuNotificationPendingIntent(): PendingIntent {
        val intent = Intent(context, DeleteMenuNotificationBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createDeleteMenuNotificationPendingIntent(): PendingIntent {
        val intent = Intent(context, ScheduleNotificationBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            2,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createFullscreenReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val description = context.getString(R.string.reminder_channel_des)
            val channel = NotificationChannel(
                FULLSCREEN_REMINDER_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
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

    fun createDefaultReminderNotificationChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Reminder Channel"
            val description = "this is an default reminder channel"
            val channel = NotificationChannel(
                BP_DEFAULT_ALARM_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                this.description = description
            }
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createStartSpecificScreenPendingIntent(uri: String): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            uri.toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun canUseFullScreenIntent(): Boolean {
        // Pending
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
//            notificationManager?.canUseFullScreenIntent() != false
//        } else true

        return true
    }

    fun notify(context: Context, id: Int, build: Notification) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(id, build)
            }

        }
    }


    companion object {
        const val NOTIFICATION_CHANNEL_ID = "main_notification_channel"
        const val FULLSCREEN_REMINDER_CHANNEL_ID = "fullscreen_reminder_channel"
        private const val ALARM_NOTIFICATION_CHANNEL_ID = "alarm_notification_channel"
        private const val MENU_NOTIFICATION_ID = 1
        private const val BP_DEFAULT_ALARM_NOTIFICATION_ID = 2
        private const val BP_DEFAULT_ALARM_CHANNEL_ID = "bp_default_alarm_channel"
    }
}
