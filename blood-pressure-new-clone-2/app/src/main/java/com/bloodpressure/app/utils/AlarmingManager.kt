package com.bloodpressure.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.repository.AlarmRepository
import com.bloodpressure.app.receiver.AlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlarmingManager(val context: Context, val alarmRepository: AlarmRepository) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val vibrationManager = VibrationManager(context)
    private val calendar = Calendar.getInstance()

    suspend fun insertRecord(record: AlarmRecord) =
        withContext(Dispatchers.IO) {

            alarmRepository.insertRecord(record)
            scheduleAlarm(record)
        }

    suspend fun updateRecord(record: AlarmRecord) =

        withContext(Dispatchers.IO) {

            cancelAlarm(record)
            alarmRepository.updateRecord(record)
            scheduleAlarm(record)
        }

    suspend fun deleteRecord(record: AlarmRecord) =
        withContext(Dispatchers.IO) {
            cancelAlarm(record)
            alarmRepository.deleteRecord(record)
        }
    private fun cancelAlarm(alarmRecord: AlarmRecord) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarmId", alarmRecord.createdAt)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRecord.createdAt.toInt(),
            intent,
            FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }


    fun playSound(soundResource: Int) {
        val mediaPlayer = MediaPlayer.create(context, soundResource)
        mediaPlayer.setOnCompletionListener { player ->
            player.release()
        }
        mediaPlayer.start()
    }

    fun vibrateNotification() {
        vibrationManager.vibrateNotification()
    }

    private fun scheduleAlarm(alarmRecord: AlarmRecord) {

        if (alarmRecord.days.isEmpty()) {
            return
        }

        val alarmTimeMillis = calendar.getTimeExactForAlarmInMilliseconds(
            hour = alarmRecord.hour,
            minute = alarmRecord.minute,
            weekDays = alarmRecord.days
        )

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarmId", alarmRecord.createdAt)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRecord.createdAt.toInt(),
            intent,
            FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )
        val canScheduleExactAlarms =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canScheduleExactAlarms) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            }
        }
    }
}