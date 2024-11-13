package com.example.videoart.batterychargeranimation.ui.splash

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.example.videoart.batterychargeranimation.BuildConfig
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.data.remote.RemoteConfig
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import com.example.videoart.batterychargeranimation.utils.TrackingManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SplashViewModel(
    val context: Context
) : ViewModel() {
    private val FETCH_REMOTE_CONFIG_TIMEOUT = 10000L // 10 seconds
    var nextScreenRoute = MutableLiveData<String>()
    var isShownLoadingAds = MutableLiveData<Boolean>()
    private val REQUEST_TIME_OUT = 30000L
    private val TIME_DELAY = 3000L
    private val TAG = SplashViewModel::class.simpleName
    private var startTime = 0L
    private var interSplashIsReadyToShow = false

    fun startSplash(activity: AppCompatActivity) {
        viewModelScope.launch {
            withTimeoutOrNull(10000L) {
                RemoteConfig.waitRemoteConfigLoaded()
            }
            AdsUtils.adsEnable = RemoteConfig.isAdsEnable
            if(isAdsAvailable) {
                initAds(activity)
                interSplashIsReadyToShow = false
                loadInterSplash(activity)
                if(!isOnboardingCompleted) {
                    AdsUtils.requestNativeLanguage(activity)
                } else {
                    AdsUtils.requestLoadBanner(activity)
                }
            } else {
                AdsUtils.isCloseAdSplash.postValue(true)
                onNavigate()
            }
        }
    }

    private fun onNavigate() {
        if (!isOnboardingCompleted) {
            nextScreenRoute.value = ROUTE_LANGUAGE
        } else {
            nextScreenRoute.value = ROUTE_HOME
        }
    }

    private fun initAds(activity: AppCompatActivity) {
        AppOpenManager.getInstance().enableAppResume()
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)

        if(BuildConfig.FLAVOR == "PROD") {
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

    private fun loadInterSplash(activity: AppCompatActivity) {
        VioAdmob.getInstance().loadSplashInterstitialAds(
            activity,
            BuildConfig.inter_splash,
            REQUEST_TIME_OUT,
            TIME_DELAY,
            object : VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    onNavigate()
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    AdsUtils.isCloseAdSplash.postValue(true)
                }

                override fun onAdSplashReady() {
                    super.onAdSplashReady()
                    showInterSplash(activity)
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdsUtils.isCloseAdSplash.postValue(true)
                }
            }
        )
    }

    private fun showInterSplash(activity: AppCompatActivity) {
        viewModelScope.launch {
            interSplashIsReadyToShow = true
            VioAdmob.getInstance().onShowSplash(activity, object  : VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    onNavigate()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdsUtils.isCloseAdSplash.postValue(true)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    AdsUtils.isCloseAdSplash.postValue(true)
                }
            })
        }
    }

    fun checkShowInterSplashWhenFail(activity: AppCompatActivity) {
        if (!interSplashIsReadyToShow) return
        VioAdmob.getInstance()
            .onCheckShowSplashWhenFail(
                activity,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        AdsUtils.isCloseAdSplash.postValue(false)
                        onNavigate()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        AdsUtils.isCloseAdSplash.postValue(true)
                    }

                    override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)

                    }
                },
                1000
            )
    }

    val isAdsAvailable: Boolean
        get() = RemoteConfig.isAdsEnable

    private val isOnboardingCompleted = PreferenceUtils.isFirstOpenComplete

    companion object {
        private const val MIN_LOAD_TIME = 3000L
        internal const val ROUTE_LANGUAGE = "language"
        internal const val ROUTE_HOME = "home"
    }
}