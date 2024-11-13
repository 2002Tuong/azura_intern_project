package com.slideshowmaker.slideshow.ui.moreapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivitySettingScreenBinding
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.extentions.sendEmail

class MoreAppActivity : AppCompatActivity() {

    private val layoutBinding: ActivitySettingScreenBinding
        get() = _binding!!
    private var _binding: ActivitySettingScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.loadLocale(this)
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        setupUI()
        hideNavigationBar()
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    private fun setupUI() {
        layoutBinding.layoutMoreApp.setOnClickListener {
            
        }
        layoutBinding.layoutFeedback.setOnClickListener {
            sendEmail(
                getString(R.string.feedback_email),
                getString(R.string.feedback_email_subject, BuildConfig.VERSION_NAME)
            )
        }

        layoutBinding.layoutPolicy.setOnClickListener {
            openUrl(getString(R.string.terms_and_policy_url))
        }

        layoutBinding.layoutShare.setOnClickListener {
            val sendingIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.dialog_sharing_message, SHARE_URL))
                type = "text/plain"
            }

            val sharingIntent = Intent.createChooser(sendingIntent, null)
            startActivity(sharingIntent)
        }

        layoutBinding.layoutNoAds.isVisible = !SharedPreferUtils.proUser
        layoutBinding.layoutNoAds.setOnClickListener {
            startActivity(PremiumActivity.newIntent(this, "settings"))
        }
    }

    private fun openUrl(url: String) {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(browserIntent)
        } catch (exception: Exception) {
        }
    }

    companion object {
        private const val SHARE_URL =
            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
    }

    private fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }
}