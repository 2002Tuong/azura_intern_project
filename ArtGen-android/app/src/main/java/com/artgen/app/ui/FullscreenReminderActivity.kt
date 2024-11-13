package com.artgen.app.ui

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.artgen.app.ui.screen.navigation.DeeplinkRoute.IMAGE_PICKER
import com.artgen.app.ui.screen.reminder.FullScreenReminderScreenType2
import com.artgen.app.utils.NotificationController
import com.artgen.app.utils.ReminderUtils
import org.koin.android.ext.android.inject
import java.util.Calendar

class FullscreenReminderActivity : AppCompatActivity() {
    private val notificationController: NotificationController by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationController.cancelMenuNotification(10000)
        ReminderUtils.isShowing = true
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        setContent {
            FullScreenReminderScreenType2(
                modifier = Modifier.fillMaxSize(),
                openApp = this::openApp,
                showUploadImage = currentHour >= 18
            )
        }
        turnScreenOnAndKeyguardOff()
    }

    private fun hideNavigationBar() {
        val decorView = window.decorView
        val uiOptions =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decorView.systemUiVisibility = uiOptions

    }


    override fun onResume() {
        super.onResume()
    }


    private fun openApp() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(IMAGE_PICKER),
            this,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("SOURCE", "REMINDER")
        finish()
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ReminderUtils.isShowing = false
        turnScreenOffAndKeyguardOn()
    }

    private fun Activity.turnScreenOnAndKeyguardOff() {
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

    private fun Activity.turnScreenOffAndKeyguardOn() {
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
}