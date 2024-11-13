package com.example.claptofindphone.presenter.language

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.example.claptofindphone.data.remote.RemoteConfigProvider
import com.example.claptofindphone.databinding.ActivityLanguageBinding
import com.example.claptofindphone.presenter.MainActivity
import com.example.claptofindphone.presenter.onboarding.OnBoardingActivity
import com.example.claptofindphone.utils.AdsHelper
import com.example.claptofindphone.utils.LanguageSupporter
import com.ironsource.mediationsdk.IronSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : AppCompatActivity() {

    private lateinit var layoutBinding: ActivityLanguageBinding
    private lateinit var languageListAdapter: LanguageAdapter
    private var isFromSetting = false

    @Inject
    lateinit var languageSupporter: LanguageSupporter
    @Inject
    lateinit var adsHelper: AdsHelper
    @Inject
    lateinit var remoteConfigProvider: RemoteConfigProvider
    private val viewModel by viewModels<LanguageScreenViewModel>()
    companion object {
        private const val FROM_SETTING_KEY = "from_setting"
        fun start(activity: Activity, fromSetting: Boolean = false) {
            Intent(activity, LanguageActivity::class.java).apply {
                putExtra(FROM_SETTING_KEY, fromSetting)
                activity.startActivity(this)
                if(!fromSetting) {
                    activity.finish()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageSupporter.loadLocale(this)
        layoutBinding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        //layoutBinding.frAds.isVisible = adsHelper.isAdsEnabled
        isFromSetting = intent.getBooleanExtra(FROM_SETTING_KEY, false)
        if (isFromSetting) {
            layoutBinding.frAds.visibility = View.GONE
            layoutBinding.imgBack.visibility = View.VISIBLE
            layoutBinding.bottomLayout.visibility = View.GONE
        } else {
            layoutBinding.imgBack.visibility = View.GONE
            val isNativeOnboardingCtaTop = remoteConfigProvider.isNativeAdOnboardCtaTop
            adsHelper.loadNativeOnBroad(this, ctaTop =  isNativeOnboardingCtaTop)
            adsHelper.loadBannerOnboard()
            initAds()
            initBanner()
            //hideNavigationBar()
        }
        setupAdapter()
        layoutBinding.imgChoose.setOnClickListener {
            languageSupporter.changeLang(languageListAdapter.itemSelected().languageCode, this)
            if (isFromSetting) {
                restartApp()
            } else {
                launchOnBoarding()
            }
        }
        layoutBinding.imgBack.setOnClickListener {
            onBackPressed()
        }

        viewModel.selectedLanguageState.observe(this) {
            layoutBinding.imgChoose.isVisible = !it.isNullOrEmpty()
        }
    }

    public override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    public override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus && !isFromSetting) {
            //hideNavigationBar()
        }
    }

    private fun restartApp() {
        val startAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("play-splash", false)
        }
        startActivity(startAppIntent)
    }

    private fun initBanner() {
        adsHelper.bannerAdViewLangauge.distinctUntilChanged().observe(this) {
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                layoutBinding.bottomLayout.addView(it)
            }
        }
    }
    private fun initAds() {
        return
        adsHelper.languageApNativeAdLoadFail.observe(this) {
            if (it) {
                layoutBinding.frAds.visibility = View.GONE
                adsHelper.languageApNativeAdLoadFail.value = false
            }
        }
        adsHelper.languageApNativeAd.observe(this) {
            Log.d("AdsLanguage", "${it}")
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    this,
                    it,
                    layoutBinding.frAds,
                    layoutBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }

    private fun setupAdapter() {
        languageListAdapter = LanguageAdapter(this)
        languageListAdapter.setOnLanguageChange {
            viewModel.selectLanguage(it)
        }
        languageListAdapter.setData(
            if (isFromSetting)
                languageSupporter.getSupportedLanguage()
            else
                languageSupporter.getSupportedLanguage().take(5)
        )
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutBinding.rvcLanguage.addItemDecoration(SpacingDecorator(8))
        layoutBinding.rvcLanguage.layoutManager = layoutManager
        layoutBinding.rvcLanguage.adapter = languageListAdapter
    }

    private fun launchOnBoarding() {
        Intent(this, OnBoardingActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    fun hideNavigationBar() {
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
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