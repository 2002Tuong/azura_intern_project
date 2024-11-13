package com.slideshowmaker.slideshow.ui.language

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivityLanguageScreen2Binding
import com.slideshowmaker.slideshow.databinding.ActivityLanguageScreenBinding
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.onboarding.OnBoardingActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.hideNavigationBar
import kotlinx.coroutines.launch
import timber.log.Timber

class LanguageActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityLanguageScreen2Binding
    private lateinit var languageAdapter: LanguageAdapter
    lateinit var navController: NavController
    var isFromSetting = false

    companion object {
        const val FROM_SETTING_KEY = "from_setting"
        fun start(activity: Activity, fromSetting: Boolean = false) {
            Intent(activity, LanguageActivity::class.java).apply {
                putExtra(FROM_SETTING_KEY, fromSetting)
                activity.startActivity(this)
                activity.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.loadLocale(this)
        isFromSetting = intent.getBooleanExtra(FROM_SETTING_KEY, false)
        layoutBinding = ActivityLanguageScreen2Binding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
//        layoutBinding.frAds.isVisible = AdsHelper.isAdEnabled
//        initBanner()
//        AdsHelper.loadNativeOnboard(this@LanguageActivity)

        Log.d("isFromsetting", "languageActivity: ${isFromSetting}")
        /*if (isFromSetting) {
            layoutBinding.frAds.visibility = View.GONE
            layoutBinding.imgBack.visibility = View.VISIBLE
        } else {
            layoutBinding.imgBack.visibility = View.GONE
            initAds()
        }
        setupAdapter()
        layoutBinding.imgChoose.setOnClickListener {
            LanguageHelper.changeLang(languageAdapter.itemSelected().code, this)
            if (isFromSetting) {
                restartApp()
            } else {
                launchOnBoarding()
            }
        }
        Timber.d("qvk23 language: " + SharedPreferUtils.langCode)
        if (SharedPreferUtils.langCode.isEmpty()) {
            layoutBinding.imgChoose.visibility = View.INVISIBLE
        }
        layoutBinding.imgBack.setOnClickListener {
            onBackPressed()
        }*/
        lifecycleScope.launch {
            this@LanguageActivity.hideNavigationBar()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
    }

//    private fun initBanner() {
//        if (!SharedPreferUtils.proUser) {
//            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner)
//        } else {
//            layoutBinding.frAds.visibility = View.GONE
//        }
//    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    fun restartApp() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("play-splash", false)
        }
        startActivity(intent)
    }

//    private fun initAds() {
//        AdsHelper.apNativeLanguageLoadFail.observe(this) {
//            if (it) {
//                layoutBinding.frAds.visibility = View.GONE
//                AdsHelper.apNativeLanguageLoadFail.value = false
//            }
//        }
//        AdsHelper.apNativeLanguage.observe(this) {
//            if (it != null) {
//                VioAdmob.getInstance().populateNativeAdView(
//                    this,
//                    it,
//                    layoutBinding.frAds,
//                    layoutBinding.includeNative.shimmerContainerBanner
//                )
//                AdsHelper.apNativeLanguage.value = null
//
//            }
//        }
//    }

//    private fun setupAdapter() {
//        languageAdapter = LanguageAdapter(this)
//        languageAdapter.setData(
//            if (isFromSetting) {
//                if (SharedPreferUtils.langCode.isEmpty()) {
//                    SharedPreferUtils.setLanguageCode("en")
//                }
//                LanguageHelper.getSupportedLanguage()
//            }
//            else
//                LanguageHelper.getSupportedLanguage().take(5)
//        )
//        languageAdapter.onLanguageSelectedCallback = {
//            layoutBinding.imgChoose.visibility = View.VISIBLE
//        }
//        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        layoutBinding.rvcLanguage.layoutManager = linearLayoutManager
//        layoutBinding.rvcLanguage.adapter = languageAdapter
//    }

    fun launchOnBoarding() {
        Intent(this, OnBoardingActivity::class.java).apply {
            startActivity(this)
            finish()
        }
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