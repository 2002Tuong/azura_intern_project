package com.example.videoart.batterychargeranimation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.receiver.ChargerStateReceiver


class ChargerService : Service() {
    private lateinit var chargerReceiver: ChargerStateReceiver
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        chargerReceiver = ChargerStateReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(chargerReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        unregisterReceiver(chargerReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        //intent.addCategory(Intent.CATEGORY_LAUNCHER)
        //intent.action = Intent.ACTION_MAIN
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification =NotificationCompat.Builder(this, CHARGER_SERVICE_CHANNEL)
            .setSmallIcon(R.drawable.app_icon)
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .build()

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(CHARGER_SERVICE_NOTIFICATION_ID, notification)
            startForeground(CHARGER_SERVICE_NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foreServiceChannel = NotificationChannel(
                CHARGER_SERVICE_CHANNEL,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = "Foreground Service"
            }
            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(foreServiceChannel)
        }
    }

    companion object {
        const val CHARGER_SERVICE_CHANNEL = "CHARGER_SERVICE_CHANNEL"
        const val CHARGER_SERVICE_NOTIFICATION_ID = 1000
        var isServiceRunning = false
    }
}