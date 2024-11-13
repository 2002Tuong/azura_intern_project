package com.example.claptofindphone.presenter.common.audio

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationManagerCompat
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.presenter.ClapToFindApplication
import com.example.claptofindphone.presenter.MainActivity
import com.example.claptofindphone.presenter.common.AudioStatus
import com.example.claptofindphone.presenter.common.MediaPlayerHelper
import com.example.claptofindphone.presenter.common.ScreenUpSupporter
import com.example.claptofindphone.presenter.common.SoundRes
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.support.label.Category
import javax.inject.Inject


interface AudioClassificationListener {
    fun onError(error: String)
    fun onResult(results: List<Category>, inferenceTime: Long)
}

@AndroidEntryPoint
class ObserveClapService :
    Service(), AudioStatus {

    private lateinit var audioClassificationHelper: AudioClassificationHelper
    private val CLAP_SERVICE_CHANNEL_ID = "ForegroundServiceChannel"
    private var isFindingAudioPlaying = false
    private var preferenceSupplier: PreferenceSupplier =
        ClapToFindApplication.get().preferenceSupplier

    @Inject
    lateinit var mediaPlayerHelper: MediaPlayerHelper

    companion object {
        var serviceRunning = false
        const val CLAP_SERVICE_NOTIFICATION_ID = 100
    }

    private val listener = object : AudioClassificationListener {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onResult(results: List<Category>, inferenceTime: Long) {
            for (i in results.indices) {
                if (results[i].label == "Hands" || results[i].index == 56) {
                    if (!isFindingAudioPlaying) {
                        if (preferenceSupplier.isShouldLightUpScreen()) {
                            ScreenUpSupporter.wakeScreenUp(applicationContext)
                        }
                        if (preferenceSupplier.isShouldPlaySound()) {
                            startSound()
                        }
                        if (preferenceSupplier.isShouldVibration()) {
                            ClapToFindApplication.get().startVibrate(
                                preferenceSupplier.getVibrationMode(), true
                            )
                        }
                        if (preferenceSupplier.isShouldTurnOnFlash()) {
                            ClapToFindApplication.get().turnOnFlash(
                                preferenceSupplier.getFlashMode(),
                                preferenceSupplier.getDuration()
                            )
                        }

                    }
                }
            }
        }

        override fun onError(error: String) {
            Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
        }
    }

    // if audio is playing, do not call to start other audio
    override fun onDuringPlaying() {
        isFindingAudioPlaying = true
    }

    override fun onAudioFinish() {
        ClapToFindApplication.get().stopVibrate()
        isFindingAudioPlaying = false
    }

    private fun initMyMediaPlayerHelper() {
        mediaPlayerHelper.setVolume(ClapToFindApplication.get().preferenceSupplier.getVolume())
    }

    private fun initSound() {
        mediaPlayerHelper.initSound(SoundRes.getSound(preferenceSupplier.getSoundType()))
    }

    private fun startSound() {
        initSound()
        mediaPlayerHelper.playSound(
            preferenceSupplier.getDuration(),
            preferenceSupplier.getVolume(),
            this, true
        )
    }

    private fun stopSound() {
        mediaPlayerHelper.stopSound(this)
    }

    private fun pauseSound() {
        mediaPlayerHelper.pauseSound()
    }


    override fun onDestroy() {
        super.onDestroy()
        stopAudioClassification()
        stopSound()
        ClapToFindApplication.get().turnOfFlash()
        serviceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        serviceRunning = true
        setupAudioClassificationHelper()
        initMyMediaPlayerHelper()
//        initSound()
    }

    private fun setupAudioClassificationHelper() {
        audioClassificationHelper = AudioClassificationHelper(applicationContext, listener)
        audioClassificationHelper.currentModelName = AudioClassificationHelper.YAMNET_MODEL_NAME
        audioClassificationHelper.initClassifier()
        audioClassificationHelper.startAudioClassification()
    }

    private fun stopAudioClassification() {
        if (::audioClassificationHelper.isInitialized) {
            audioClassificationHelper.stopAudioClassification()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val foregroundServiceIntent = Intent(this, MainActivity::class.java)
        foregroundServiceIntent.putExtra("SHOULD_STOP_AUDIO", true)
        foregroundServiceIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        foregroundServiceIntent.action = Intent.ACTION_MAIN
        foregroundServiceIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, foregroundServiceIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val customLayout = RemoteViews(this.packageName, R.layout.clap_hand_service_notification)
        val collapsedCustomLayout =
            RemoteViews(this.packageName, R.layout.clap_hand_service_notification_collapsed)
        val notificationCompat: Notification = NotificationCompat.Builder(this, CLAP_SERVICE_CHANNEL_ID)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    setContent(collapsedCustomLayout)
                    setCustomBigContentView(customLayout)
                } else {
                    setContent(customLayout)
                }
            }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setCategory(CATEGORY_SERVICE)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(CLAP_SERVICE_NOTIFICATION_ID, notificationCompat)
            startForeground(CLAP_SERVICE_NOTIFICATION_ID, notificationCompat)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val foreServiceChannel = NotificationChannel(
            CLAP_SERVICE_CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            this.description = "Foreground Service"
            setSound(null, null)
            enableVibration(false)
        }
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(foreServiceChannel)
    }
}