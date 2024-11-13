package com.wifi.wificharger.ui.splash

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.wifi.wificharger.utils.Logger
import com.wifi.wificharger.BuildConfig
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.remote.RemoteConfig
import com.wifi.wificharger.ui.base.BaseViewModel
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.TrackingManager
import com.wifi.wificharger.utils.arePermissionsGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashViewModel(
    dataStore: AppDataStore
) : BaseViewModel(dataStore) {

    var nextScreenRoute = MutableLiveData<String>()
    var isOnboardingCompleted = false
        private set
    private var remoteConfig = RemoteConfig()
    private val REQUEST_TIME_OUT = 30000L
    private var startTime = 0L
    private var interSplashIsReadyToShow = false

    init {
        viewModelScope.launch {
            dataStore.isOnboardingShownFlow.collectLatest {
                isOnboardingCompleted = it
            }
        }
//        TrackingManager.logSplashScreenLaunch()
    }

    fun loadData(activity: AppCompatActivity) {
        viewModelScope.launch {
            if (isAdsAvailable()) {
                initAds(activity)
                loadInter(activity)

                if (!isOnboardingCompleted) {
                    AdsUtils.requestNativeLanguageFirstOpen(activity)
                    AdsUtils.requestNativeLanguageDuplicateFirstOpen(activity)
                } else {
                    if (!activity.arePermissionsGranted(permissions = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
                        AdsUtils.requestNativePermission(activity)
                    }
                }
            } else {
                AdsUtils.isCloseAdSplash.postValue(true)
                onNavigate()
            }
        }
    }

    private fun loadInter(activity: AppCompatActivity) {
        interSplashIsReadyToShow = false
        Logger.d( "loadInterSplash: ")
        loadInterSplash(activity = activity)
    }

    private fun loadInterSplash(activity: AppCompatActivity) {
        startTime = System.currentTimeMillis()
        TrackingManager.logEvent("load_inter_splash")
        Logger.d("loadInterSplash: ")
        VioAdmob.getInstance().loadSplashInterstitialAds(
            activity,
            BuildConfig.inter_splash,
            REQUEST_TIME_OUT,
            MIN_LOAD_TIME,
            false,
            object :
                VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    onNavigate()
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Logger.d("Failed to load ${adError?.message}")
                    TrackingManager.logEvent("inter_splash_load_fail")
                    AdsUtils.isCloseAdSplash.postValue(true)
                }

                override fun onAdSplashReady() {
                    super.onAdSplashReady()
                    showInterSplash(activity)
                }
            })
    }

    private fun showInterSplash(activity: AppCompatActivity) {
        viewModelScope.launch {
            val loadTime = System.currentTimeMillis() - startTime
            if (loadTime < MIN_LOAD_TIME) {
                delay(MIN_LOAD_TIME - loadTime)
            }
            interSplashIsReadyToShow = true
            VioAdmob.getInstance().onShowSplash(activity, object : VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    onNavigate()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdsUtils.isCloseAdSplash.postValue(true)
                }

                override fun onAdFailedToShow(adError: ApAdError?) {
                    super.onAdFailedToShow(adError)
                    TrackingManager.logEvent("show_fail_${adError?.message}")
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
                        TrackingManager.logEvent("show_fail_${adError?.message}")
                    }
                },
                1000
            )
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
        if (BuildConfig.FLAVOR != "dev")
            initAppsflyer(activity)
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
        val afRevenueBuilder = AppsFlyerAdRevenue.Builder(activity.application)
        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build())
    }


    private fun isAdsAvailable(): Boolean = !dataStore.isPurchased && !remoteConfig.offAllAds()

    companion object {
        internal const val ROUTE_LANGUAGE = "language"
        internal const val ROUTE_HOME = "home"
        private const val MIN_LOAD_TIME = 3000L
    }
}