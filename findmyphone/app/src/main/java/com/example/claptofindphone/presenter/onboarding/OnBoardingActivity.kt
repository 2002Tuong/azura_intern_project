package com.example.claptofindphone.presenter.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.distinctUntilChanged
import androidx.viewpager2.widget.ViewPager2
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.databinding.ActivityOnBoardingBinding
import com.example.claptofindphone.presenter.MainActivity
import com.example.claptofindphone.utils.AdsHelper
import com.example.claptofindphone.utils.LanguageSupporter
import com.google.android.material.tabs.TabLayoutMediator
import com.ironsource.mediationsdk.IronSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityOnBoardingBinding
    private var currentPageIndex = 0


    @Inject lateinit var preferenceSupplier: PreferenceSupplier
    @Inject lateinit var adsHelper: AdsHelper
    @Inject lateinit var languageSupporter: LanguageSupporter

    private var nativeAd1: ApNativeAd? = null
    private var nativeAd2: ApNativeAd? = null
    private var nativeAd3: ApNativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = ActivityOnBoardingBinding.inflate(layoutInflater)
        languageSupporter.loadLocale(this)
        setContentView(layoutBinding.root)
        //hideNavigationBar()
        setupViewPager()
        //layoutBinding.frAds.isVisible = adsHelper.isAdsEnabled
        layoutBinding.txtContinue.setOnClickListener { onNextPage() }
        //initAds()
        adsHelper.requestNativePermission(this)
        adsHelper.loadBannerPermission()
        initNative()
        initBanner()

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
        if(hasFocus) {
            //hideNavigationBar()
        }
    }
    private fun initBanner() {
        adsHelper.bannerAdViewOnboard.distinctUntilChanged().observe(this) {
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

    private fun initNative() {
        return
        adsHelper.onBoarding1ApNativeAd.observe(this) {
            it?.let { nativeAd ->
                nativeAd1 = nativeAd
                if (getItem() == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            this@OnBoardingActivity,
                            nativeAd1,
                            layoutBinding.frAds,
                            layoutBinding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }

        adsHelper.onBoarding2ApNativeAd.observe(this) {
            it?.let { nativeAd ->
                nativeAd2 = nativeAd
                if (getItem() == 1) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            this@OnBoardingActivity,
                            nativeAd2,
                            layoutBinding.frAds,
                            layoutBinding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }

        adsHelper.onBoarding3ApNativeAd.observe(this) {
            it?.let { nativeAd ->
                nativeAd3 = nativeAd
                if (getItem() == 2) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            this@OnBoardingActivity,
                            nativeAd3,
                            layoutBinding.frAds,
                            layoutBinding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }
        adsHelper.onBoardingApNativeAdLoadFail.observe(this) { isFail ->
            if (isFail) {
                layoutBinding.frAds.visibility = View.GONE
                adsHelper.onBoardingApNativeAdLoadFail.value = false
            }
        }
    }

    private fun setupViewPager() {
        layoutBinding.vpTutorial.adapter = OnBoardingViewPagerAdapter(this)
        layoutBinding.vpTutorial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPageIndex = position
                if (currentPageIndex >= 2) {
                    layoutBinding.txtContinue.text = getString(R.string.next)
                } else {
                    layoutBinding.txtContinue.text = getString(R.string.next)
                }
                when(position) {
                    0 -> {
                        nativeAd1?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                this@OnBoardingActivity,
                                it,
                                layoutBinding.frAds,
                                layoutBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                    1 -> {
                        nativeAd2?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                this@OnBoardingActivity,
                                it,
                                layoutBinding.frAds,
                                layoutBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                    2 -> {
                        nativeAd3?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                this@OnBoardingActivity,
                                it,
                                layoutBinding.frAds,
                                layoutBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                }

            }
        })
        TabLayoutMediator(layoutBinding.tlTutorial, layoutBinding.vpTutorial) { _, _ -> }.attach()
    }

    private fun getItem(): Int {
        return layoutBinding.vpTutorial.currentItem
    }

    private fun onNextPage() {
        currentPageIndex++
        if (currentPageIndex >= 2) {
            layoutBinding.txtContinue.text = getString(R.string.next)
        } else {
            layoutBinding.txtContinue.text = getString(R.string.next)
        }
        if (currentPageIndex < 3) {
            layoutBinding.vpTutorial.setCurrentItem(currentPageIndex, true)
        } else if (currentPageIndex == 3) {
            currentPageIndex--
            launchHome()
        } else {
            launchHome()
        }
    }

    private fun launchHome() {
        lifecycle.coroutineScope.launch {
            delay(100)
            Intent(this@OnBoardingActivity, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
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