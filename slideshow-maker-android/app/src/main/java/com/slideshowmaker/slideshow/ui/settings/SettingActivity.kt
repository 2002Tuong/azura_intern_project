package com.slideshowmaker.slideshow.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ads.control.admob.AppOpenManager
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivitySettingScreenBinding
import com.slideshowmaker.slideshow.ui.language.LanguageActivity
import com.slideshowmaker.slideshow.ui.moreapps.MoreAppActivity
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity
import com.slideshowmaker.slideshow.utils.ConsentHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.extentions.sendEmail

class SettingActivity : AppCompatActivity() {

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
        layoutBinding.ibBack.setOnClickListener { finish() }
        layoutBinding.layoutMoreApp.setOnClickListener {
            startActivity(Intent(this, MoreAppActivity::class.java))
        }
        layoutBinding.layoutFeedback.setOnClickListener {
            AppOpenManager.getInstance().disableAdResumeByClickAction()
            sendEmail(
                getString(R.string.feedback_email),
                getString(R.string.feedback_email_subject, BuildConfig.VERSION_NAME)
            )
        }

        layoutBinding.layoutPolicy.setOnClickListener {
            AppOpenManager.getInstance().disableAdResumeByClickAction()
            openUrl(getString(R.string.terms_and_policy_url))
        }

        layoutBinding.layoutShare.setOnClickListener {
            AppOpenManager.getInstance().disableAdResumeByClickAction()
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
        layoutBinding.layoutLanguage.setOnClickListener {
            launchLanguage()
        }

        Log.d("Consent", "${RemoteConfigRepository.cmpRequire}")
        layoutBinding.layoutPrivacyPolicy.isVisible = true
        layoutBinding.layoutPrivacyPolicy.setOnClickListener {
            ConsentHelper.updateConsent(this, {}, {this.finish()})
        }
    }

    private fun launchLanguage() {
        LanguageActivity.start(this, true)
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
        const val SHARE_URL =
            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
    }

    fun hideNavigationBar() {
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