package com.example.videoart.batterychargeranimation.ui.reminder

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.databinding.ActivityFullScreenReminderBinding
import com.example.videoart.batterychargeranimation.extension.getTimeFormattedString
import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.example.videoart.batterychargeranimation.utils.ReminderUtils
import org.koin.android.ext.android.inject
import java.util.Calendar

class FullScreenReminderActivity : AppCompatActivity() {
    private var viewBinding: ActivityFullScreenReminderBinding? = null
    private val viewModel: FullScreenReminderViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOpenManager.getInstance().disableAppResume()
        viewBinding = ActivityFullScreenReminderBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        ReminderUtils.isShowing = true
        turnScreenOnAndKeyguardOff()
    }

    override fun onResume() {
        super.onResume()

        val calendar = Calendar.getInstance()
        Handler(Looper.getMainLooper()).postDelayed({
            calendar.timeInMillis = System.currentTimeMillis()
            viewBinding?.tvTime?.text = calendar.getTimeFormattedString("HH:mm")
            viewBinding?.tvDay?.text = calendar.getTimeFormattedString("EEEE dd MMMM")
        }, 1000)

        viewBinding?.run {
            icBtnClose.setOnClickListener {
                finish()
            }

            if(viewModel.theme.value.size >= 2) {
                Glide.with(this@FullScreenReminderActivity)
                    .load(viewModel.theme.value[0].thumbUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imvOrigin)

                Glide.with(this@FullScreenReminderActivity)
                    .load(viewModel.theme.value[1].thumbUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imvAddPhoto)

                cvOrigin.setOnClickListener {
                    openApp(viewModel.theme.value[0])
                }
                cvAddPhoto.setOnClickListener {
                    openApp(viewModel.theme.value[1])
                }
            }

            layoutItem.setOnClickListener {
                openApp(null)
            }
            btnOpenApp.setOnClickListener {
                openApp(null)
            }

            hideNavigationBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
        ReminderUtils.isShowing = false
        turnScreenOffAndKeyguardOn()
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

    private fun openApp(theme: RemoteTheme?) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            null,
            this,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("SOURCE", theme)
        finish()
        startActivity(intent)
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

        with(getSystemService(KEYGUARD_SERVICE) as KeyguardManager) {
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