package com.bloodpressure.app

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.screen.UriPattern.ADD_RECORD
import com.bloodpressure.app.screen.UriPattern.BLOOD_SUGAR
import com.bloodpressure.app.screen.UriPattern.MEASURE_HEART_RATE
import com.bloodpressure.app.screen.fullscreenreminder.FULLSCREEN_REMINDER_TYPE_1
import com.bloodpressure.app.screen.fullscreenreminder.ReminderContent
import org.koin.android.ext.android.inject


class FullscreenReminderActivity : AppCompatActivity() {

    private val notificationController: NotificationController by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationController.cancelMenuNotification(10000)
        val reminderType = intent.getIntExtra(ARG_REMINDER_TYPE, FULLSCREEN_REMINDER_TYPE_1)
        setContent {
            ReminderContent(type = reminderType,
                openHeartRate = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(MEASURE_HEART_RATE),
                        this,
                        MainActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    finish()
                    startActivity(intent)
                },
                openBloodPressure = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(ADD_RECORD),
                        this,
                        MainActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    finish()
                    startActivity(intent)
                },
                openBloodSugar = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(BLOOD_SUGAR),
                        this,
                        MainActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    finish()
                    startActivity(intent)
                },
                openApp = {
                    val intent = Intent(
                        this,
                        MainActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    finish()
                    startActivity(intent)
                }
            )
        }
        turnScreenOnAndKeyguardOff()
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOffAndKeyguardOn()
    }

    fun Activity.turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
            }
        }
    }

    fun Activity.turnScreenOffAndKeyguardOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    companion object {
        const val ARG_REMINDER_TYPE = "ARG_REMINDER_TYPE"
    }
}