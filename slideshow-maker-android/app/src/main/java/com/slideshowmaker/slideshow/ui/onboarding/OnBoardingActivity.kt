package com.slideshowmaker.slideshow.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.google.android.material.tabs.TabLayoutMediator
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivityOnBoardingScreenBinding
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.permission.PermissionActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.hideNavigationBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnBoardingActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityOnBoardingScreenBinding
    private var curPageIndex = 0
    private var native1: ApNativeAd? = null
    private var native2: ApNativeAd? = null
    private var native3: ApNativeAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.loadLocale(this)
        layoutBinding = ActivityOnBoardingScreenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            this@OnBoardingActivity.hideNavigationBar()
        }
        AdsHelper.requestNativePermission(this)
        setContentView(layoutBinding.root)
        setupViewPager()
        layoutBinding.frAds.isVisible = AdsHelper.isAdEnabled
        layoutBinding.txtContinue.setOnClickListener { onNextPage() }
        initAds()
//        initBanner()
        hideNavigationBar()
    }

    private fun initAds() {
        AdsHelper.apNativeOnBoardingLoadFail.observe(this) {
            if (it) {
                layoutBinding.frAds.visibility = View.GONE
                AdsHelper.apNativeOnBoardingLoadFail.value = false
            }
        }

        AdsHelper.apNativeOnBoarding1.observe(this) {
            it?.let { nativeAd ->
                native1 = nativeAd
                if (getItem() == 0) {
                    Log.d("Onboard", "${native1}")
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            this@OnBoardingActivity,
                            native1,
                            layoutBinding.frAds,
                            layoutBinding.includeNative.shimmerContainerBanner
                        )
                        //AdsHelper.apNativeOnBoarding1.value = null
                    }
                }

            }
        }

        AdsHelper.apNativeOnBoarding2.observe(this) {
            it?.let { nativeAd ->
                native2 = nativeAd
                if (getItem() == 1) {
                    Log.d("Onboard", "${native2}")
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            this@OnBoardingActivity,
                            native2,
                            layoutBinding.frAds,
                            layoutBinding.includeNative.shimmerContainerBanner
                        )
                        //AdsHelper.apNativeOnBoarding2.value = null
                    }

                }
            }
        }

        AdsHelper.apNativeOnBoarding3.observe(this) {
            it?.let { nativeAd ->
                native3 = nativeAd
                if (getItem() == 2) {
                    Log.d("Onboard", "${native3}")
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            this@OnBoardingActivity,
                            native3,
                            layoutBinding.frAds,
                            layoutBinding.includeNative.shimmerContainerBanner
                        )
                        //AdsHelper.apNativeOnBoarding3.value = null
                    }
                }

            }
        }
    }

    private fun initBanner() {
        if (!SharedPreferUtils.proUser) {
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner)
        } else {
            layoutBinding.frAds.visibility = View.GONE
        }
    }

    private fun setupViewPager() {
        layoutBinding.vpTutorial.adapter = OnBoardingViewPagerAdapter(this)
        layoutBinding.vpTutorial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                curPageIndex = position
                if (curPageIndex >= 2) {
                    layoutBinding.txtContinue.text = getString(R.string.get_started)
                } else {
                    layoutBinding.txtContinue.text = getString(R.string.next)
                }

                when(position) {
                    0 -> {
                        native1?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                this@OnBoardingActivity,
                                it,
                                layoutBinding.frAds,
                                layoutBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                    1 -> {
                        native2?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                this@OnBoardingActivity,
                                it,
                                layoutBinding.frAds,
                                layoutBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                    2 -> {
                        native3?.let {
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

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    private fun onNextPage() {
        curPageIndex++
        if (curPageIndex >= 2) {
            layoutBinding.txtContinue.text = getString(R.string.get_started)
        } else {
            layoutBinding.txtContinue.text = getString(R.string.next)
        }
        if (curPageIndex < 3) {
            layoutBinding.vpTutorial.setCurrentItem(curPageIndex, true)
        } else if (curPageIndex == 3) {
            curPageIndex--
            launchHome()
        } else {
            launchHome()
        }
    }

    private fun launchHome() {
        lifecycle.coroutineScope.launch {
            delay(100)
            Intent(this@OnBoardingActivity, PermissionActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }
    }

    private fun getItem(): Int {
        Log.d("Onboard", "Current: ${layoutBinding.vpTutorial.currentItem}")
        return layoutBinding.vpTutorial.currentItem
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