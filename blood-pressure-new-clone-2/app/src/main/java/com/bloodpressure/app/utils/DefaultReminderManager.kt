package com.bloodpressure.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bloodpressure.app.BuildConfig
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.receiver.CategoryDefaultReminder
import com.bloodpressure.app.receiver.DefaultBpReminderReceiver
import com.bloodpressure.app.receiver.ReminderMode
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DefaultReminderManager(
    val context: Context,
    val bpRepository: BpRepository
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val calendar = Calendar.getInstance()

    fun startDefaultReminder(reminderMode: ReminderMode) {

        if (reminderMode == ReminderMode.FULL) {
            scheduleDefaultBpAddReminder()
            scheduleDefaultBpLevelReminder()
            scheduleDefaultBpReviewReminder()
        }
        scheduleDefaultBpRegularlyReminder()
        scheduleDefaultBpRegularlyReminder(
            hour = CategoryDefaultReminder.BPREGULARLY_AT_11AM.defaultHour,
            minutes = CategoryDefaultReminder.BPREGULARLY_AT_11AM.defaultMinute,
            regularlyReminderCategory = CategoryDefaultReminder.BPREGULARLY_AT_11AM.value,
            regularlyReminderId = DEFAULT_BP_REGULARLY2_REMINDER_ID
        )
        scheduleDefaultBpRegularlyReminder(
            hour = CategoryDefaultReminder.BPREGULARLY_AT_5PM.defaultHour,
            minutes = CategoryDefaultReminder.BPREGULARLY_AT_5PM.defaultMinute,
            regularlyReminderCategory = CategoryDefaultReminder.BPREGULARLY_AT_5PM.value,
            regularlyReminderId = DEFAULT_BP_REGULARLY3_REMINDER_ID
        )
    }

    fun scheduleDefaultBpLevelReminder(
        hour: Int = CategoryDefaultReminder.BPLEVEL.defaultHour,
        minutes: Int = CategoryDefaultReminder.BPLEVEL.defaultMinute
    ) {
        val alarmTimeMillis = calendar.getTimeExactForDefaultReminderInMillis(
            hour = hour,
            minute = minutes
        )

        val intent = Intent(context, DefaultBpReminderReceiver::class.java)
        intent.putExtra(DefaultBpReminderReceiver.KEY, CategoryDefaultReminder.BPLEVEL.value)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEFAULT_BP_LEVEL_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        scheduleAlarmWithPendingIntent(pendingIntent, alarmTimeMillis)
    }

    fun scheduleDefaultBpRegularlyReminder(
        hour: Int = CategoryDefaultReminder.BPREGULARLY_AT_6AM.defaultHour,
        minutes: Int = CategoryDefaultReminder.BPREGULARLY_AT_6AM.defaultMinute,
        regularlyReminderCategory: Int = CategoryDefaultReminder.BPREGULARLY_AT_6AM.value,
        regularlyReminderId: Int = DEFAULT_BP_REGULARLY1_REMINDER_ID
    ) {
        val alarmTimeMillis = calendar.getTimeExactForDefaultReminderInMillis(
            hour = hour,
            minute = minutes,
        )
        val intent = Intent(context, DefaultBpReminderReceiver::class.java)
        intent.putExtra(DefaultBpReminderReceiver.KEY, regularlyReminderCategory)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            regularlyReminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        scheduleAlarmWithPendingIntent(pendingIntent, alarmTimeMillis)
        Timber.d("Schedule a reminder at ${alarmTimeMillis / 1000L / 60L}")
    }

    fun scheduleDefaultBpReviewReminder(
        hour: Int = CategoryDefaultReminder.BPREVIEW.defaultHour,
        minutes: Int = CategoryDefaultReminder.BPREVIEW.defaultMinute
    ) {
        val alarmTimeMillis = calendar.getTimeExactForDefaultReminderInMillis(
            hour = hour,
            minute = minutes,
        )

        val intent = Intent(context, DefaultBpReminderReceiver::class.java)
        intent.putExtra(DefaultBpReminderReceiver.KEY, CategoryDefaultReminder.BPREVIEW.value)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEFAULT_BP_REVIEW_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        scheduleAlarmWithPendingIntent(pendingIntent, alarmTimeMillis)
    }

    fun scheduleDefaultBpAddReminder(
        hour: Int = CategoryDefaultReminder.BPADD.defaultHour,
        minutes: Int = CategoryDefaultReminder.BPADD.defaultMinute
    ) {
        val alarmTimeMillis = if (BuildConfig.DEBUG)
            Calendar.getInstance().timeInMillis + 20_000L
        else calendar.getTimeExactForDefaultReminderInMillis(
            hour = hour,
            minute = minutes,
        )

        val intent = Intent(context, DefaultBpReminderReceiver::class.java)
        intent.putExtra(DefaultBpReminderReceiver.KEY, CategoryDefaultReminder.BPADD.value)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEFAULT_BP_ADD_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        scheduleAlarmWithPendingIntent(pendingIntent, alarmTimeMillis)
    }

    private fun scheduleAlarmWithPendingIntent(
        pendingIntent: PendingIntent,
        alarmTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExactAlarms =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (canScheduleExactAlarms) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(alarmTimeMillis, pendingIntent),
                pendingIntent
            )
        }

        Logger.d(
            "DefaultReminderManager: alarm scheduled at ${
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.US
                ).format(
                    Calendar.getInstance().apply {
                        timeInMillis = alarmTimeMillis
                    }.time
                )
            }"
        )
    }

    companion object {
        const val DEFAULT_BP_LEVEL_REMINDER_ID = 1
        const val DEFAULT_BP_REGULARLY1_REMINDER_ID = 2
        const val DEFAULT_BP_REVIEW_REMINDER_ID = 3
        const val DEFAULT_BP_ADD_REMINDER_ID = 4
        const val REPEATING_REMINDER_ID = 5
        const val DEFAULT_BP_REGULARLY2_REMINDER_ID = 6
        const val DEFAULT_BP_REGULARLY3_REMINDER_ID = 7
    }
}