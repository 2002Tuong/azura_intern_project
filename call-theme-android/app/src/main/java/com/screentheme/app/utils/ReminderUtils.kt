package com.screentheme.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.screentheme.app.BuildConfig
import com.screentheme.app.notification.receiver.LockScreenReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderUtils {
    var isShowing: Boolean = false

    fun scheduleFullscreenReminder(context: Context, requestCode: Int, timeInMs: Long) {
        val intent =
            Intent(context, LockScreenReminderReceiver::class.java)
        val broadcast = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExactAlarms =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMs,
                broadcast
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMs,
                broadcast
            )
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMs
        Log.d("ReminderUtils",
            "ScheduleFullscreenReminder: alarm scheduled at ${
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.US
                ).format(
                    calendar.time
                )
            }"
        )
    }

    fun scheduleNextFullScreenReminder(context: Context) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val nextReminderHour = when {
            currentHour < 3 -> 3
            currentHour < 18 -> 18
            else -> 3
        }
        if (nextReminderHour == 3 && currentHour >= 18) {
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, nextReminderHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val reminderTimeMs = if (BuildConfig.DEBUG) Calendar.getInstance().timeInMillis + 10_000L else calendar.timeInMillis

        scheduleFullscreenReminder(context, 10000, reminderTimeMs)
    }

}