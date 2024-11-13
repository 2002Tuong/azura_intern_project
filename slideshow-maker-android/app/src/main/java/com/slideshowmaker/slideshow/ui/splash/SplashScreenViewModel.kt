package com.slideshowmaker.slideshow.ui.splash

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.SubscriptionRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.ui.base.BaseViewModel
import com.slideshowmaker.slideshow.ui.dialog.LoadingAdsPopup
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.TrackingManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SplashScreenViewModel(
    override val subscriptionRepos: SubscriptionRepository
) : BaseViewModel(subscriptionRepos) {

    private val _loadingState = MutableStateFlow(true)
    private var appOpenAdObj: AppOpenAd? = null
    private var onLoadingOpenAd = false
    var onShowingOpenAds = false
    val loadingState: StateFlow<Boolean> get() = _loadingState

    private val _canNavigateToMainScreen = MutableStateFlow(false)
    val navigateToMainScreen: StateFlow<Boolean> get() = _canNavigateToMainScreen
    fun loadData(activity: Activity) {
        Log.d("AppFlyer", "call time")
        viewModelScope.launch {
            _loadingState.update { true }
            checkSubscriptionStatus()
            withTimeoutOrNull(DEFAULT_TIMEOUT_MILLIS) {
                RemoteConfigRepository.fetch()
            }
            AdsHelper.isAdEnabled = RemoteConfigRepository.isAdsEnable
            Log.d("AppFlyer", "${AdsHelper.isAdEnabled}")
            if (!SharedPreferUtils.proUser && AdsHelper.isAdEnabled) {
                initAds(activity as AppCompatActivity)
                loadAdSplash(activity as AppCompatActivity)
                showOpenAdIfAvailableAsync(activity)
                if (!SharedPreferUtils.isFirstOpenComplete) {
                    AdsHelper.requestNativeLanguage(activity)
                    AdsHelper.requestNativeLanguageDup(activity)
                }
                AdsHelper.isAdsSplashClosed.postValue(false)
            } else {
                delay(DEFAULT_TIME_DELAY)
                AdsHelper.isAdsSplashClosed.postValue(true)
                _canNavigateToMainScreen.update { true }
            }
        }
    }

    private fun initAds(activity: AppCompatActivity) {
        AppOpenManager.getInstance().enableAppResume()
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
        if(BuildConfig.FLAVOR == "PROD" ) {
            Log.d("AppFlyer", "call init")
            initAppsflyer(activity)

        }
    }

    private fun initAppsflyer(activity: AppCompatActivity) {
        AppsFlyerLib.getInstance()
            .init(BuildConfig.appsflyer_key, object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    TrackingManager.logEvent("appsflyer_on_conversion_data_success")
                }

                override fun onConversionDataFail(p0: String?) {
                    TrackingManager.logEvent("appsflyer_on_conversion_data_fail")
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    TrackingManager.logEvent("appsflyer_on_app_open_attribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    TrackingManager.logEvent("appsflyer_on_conversion_attribution_failure")
                }

            }, activity)
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG);
        AppsFlyerLib.getInstance().start(activity)


        runCatching {
            val afRevenueBuilder = AppsFlyerAdRevenue.Builder(activity.application)
            AppsFlyerAdRevenue.initialize(afRevenueBuilder.build())
        }

    }

    private suspend fun loadAdSplash(activity: AppCompatActivity) {
        if (RemoteConfigRepository.appOpenAdsConfig?.enable == true) {
            loadOpenInterAd(activity)
        } else {
            loadInterSplash(activity)
        }
    }

    private suspend fun loadOpenInterAd(activity: AppCompatActivity) {
        val startTime = System.currentTimeMillis()
        withTimeoutOrNull(DEFAULT_REQUEST_TIME_OUT) {
            loadOpenAds(activity)
        } ?: kotlin.run {
            _canNavigateToMainScreen.update { true }
        }
        val durationTime = System.currentTimeMillis() - startTime
        if (durationTime < DEFAULT_TIME_DELAY) {
            delay(DEFAULT_TIME_DELAY - durationTime)
        }
    }

    private fun loadOpenAds(activity: AppCompatActivity) {
        if (onLoadingOpenAd) {
            return
        }
        onLoadingOpenAd = true
        AppOpenAd.load(
            activity,
            BuildConfig.appopen_splash,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Logger.d("onAdLoaded: $ad")
                    appOpenAdObj = ad
                    onLoadingOpenAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    onLoadingOpenAd = false
                    _canNavigateToMainScreen.update { true }
                    Logger.e("onAdFailedToLoad: ${loadAdError.message}")
                }
            })
    }

    fun showOpenAdIfAvailableAsync(activity: AppCompatActivity) {
        val loadingAdsPopup = LoadingAdsPopup.newInstance()
        appOpenAdObj?.let {
            loadingAdsPopup.show(activity.supportFragmentManager, null)
        }
        appOpenAdObj?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAdObj?.fullScreenContentCallback = null
                appOpenAdObj = null
                onShowingOpenAds = false
                AdsHelper.isAdsSplashClosed.postValue(true)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAdObj?.fullScreenContentCallback = null
                appOpenAdObj = null
                onShowingOpenAds = false
                _canNavigateToMainScreen.update { true }
            }

            override fun onAdShowedFullScreenContent() {
                AppOpenManager.getInstance().disableAppResumeWithActivity(activity::class.java)
            }
        }
        appOpenAdObj?.let {
            onShowingOpenAds = true
            _canNavigateToMainScreen.update { true }
            it.show(activity)
        }
    }


    private fun loadInterSplash(activity: AppCompatActivity) {
        VioAdmob.getInstance().loadSplashInterstitialAds(
            activity,
            BuildConfig.inter_splash,
            DEFAULT_REQUEST_TIME_OUT,
            DEFAULT_TIME_DELAY,
            true,
            object :
                VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    _canNavigateToMainScreen.update { true }
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdsHelper.isAdsSplashClosed.postValue(true)
                }
            })
    }

    fun checkShowInterSplashWhenFail(activity: AppCompatActivity) {
        VioAdmob.getInstance()
            .onCheckShowSplashWhenFail(
                activity,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        _canNavigateToMainScreen.update { true }
                    }
                },
                1000
            )
    }

    companion object {
        private const val DEFAULT_TIMEOUT_MILLIS = 10000L
        private const val DEFAULT_MIN_TIMEOUT_MS = 100L
        private val DEFAULT_REQUEST_TIME_OUT = 30000L
        private val DEFAULT_TIME_DELAY = 3000L

    }
}
