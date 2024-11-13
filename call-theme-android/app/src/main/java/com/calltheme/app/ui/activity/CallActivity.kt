package com.calltheme.app.ui.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.screentheme.app.R
import com.screentheme.app.databinding.ActivityCallBinding
import com.screentheme.app.models.CallContactModel
import com.screentheme.app.utils.extensions.animateImageViewZoom
import com.screentheme.app.utils.helpers.CallController
import com.screentheme.app.utils.helpers.CallControllerListener
import com.screentheme.app.utils.helpers.FlashController
import com.screentheme.app.utils.helpers.ThemeManager
import com.screentheme.app.utils.helpers.VibrateController
import com.screentheme.app.utils.helpers.getCallContact
import com.screentheme.app.utils.helpers.isOreoMr1Plus
import com.screentheme.app.utils.helpers.isOreoPlus
import org.koin.android.ext.android.inject

class CallActivity : BaseActivity() {
    private val themeManager: ThemeManager by inject()
    private val vibrateHelper: VibrateController by inject()
    private val flashHelper: FlashController by inject()
    companion object {
        fun getStartIntent(context: Context): Intent {
            val openAppCallIntent = Intent(context, CallActivity::class.java)
            openAppCallIntent.action = Intent.ACTION_VIEW
            openAppCallIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            return openAppCallIntent
        }
    }

    private lateinit var binding: ActivityCallBinding

    private var callContact: CallContactModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        showWhenLockedAndTurnScreenOn()
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        hideNavigationBar()

        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addLockScreenFlags()

        CallController.addListener(callCallback)
        updateCallContactInfo(CallController.getPrimaryCall())

        var themeConfig = themeManager.getCurrentThemeConfig()

        if (themeConfig == null) {
            themeConfig = themeManager.getThemeConfig(ThemeManager.DEFAULT_THEME_ID)
        }

        binding.callEnd.setOnClickListener {
            CallController.reject()
            finish()
        }

        binding.callAccept.setOnClickListener {
            CallController.accept()
        }

        Glide.with(this)
            .load(themeConfig?.avatar)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callerAvatar)

        Glide.with(this)
            .load(themeConfig?.background)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callBackground)

        Glide.with(this)
            .load(themeConfig?.declineCallIcon)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callEnd)

        Glide.with(this)
            .load(themeConfig?.acceptCallIcon)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callAccept)

        binding.callAccept.animateImageViewZoom(800)
        binding.callEnd.animateImageViewZoom(800)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideNavigationBar()
        }
    }

    override fun onDestroy() {
        vibrateHelper.stopVibration()
        flashHelper.stopFlashLight()
        super.onDestroy()
    }

    private fun addLockScreenFlags() {
        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        if (isOreoPlus()) {
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    fun updateCallContactInfo(call: Call?) {

        getCallContact(applicationContext, call) { contact ->

            if (call != CallController.getPrimaryCall()) {
                return@getCallContact
            }
            callContact = contact

            runOnUiThread {
                updateOtherPersonsInfo()
            }
        }
    }

    private fun updateOtherPersonsInfo() {
        if (callContact == null) {
            return
        }

        binding.callerNameLabel.text = if (callContact!!.name.isNotEmpty()) callContact!!.name else getString(
            R.string.unknown_caller)
        if (callContact!!.number.isNotEmpty() && callContact!!.number != callContact!!.name) {
            binding.callerNumber.text = callContact!!.number
            if (callContact!!.numberLabel.isNotEmpty()) {
                binding.callerNumber.text = "${callContact!!.number} - ${callContact!!.numberLabel}"
            }
        } else {
            binding.callerNumber.visibility = View.GONE
        }
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private val callCallback = object : CallControllerListener {
        override fun onStateChanged() {
        }

        override fun onPrimaryCallChanged(call: Call) {
            updateCallContactInfo(call)
        }

        override fun onCallEnded(call: Call) {
            finish()
        }
    }

}