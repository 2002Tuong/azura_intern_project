package com.calltheme.app.ui.splash

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.calltheme.app.utils.AdsUtils
import com.calltheme.app.utils.BannerAdsHelpers
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.screentheme.app.BuildConfig
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.data.remote.config.RemoteConfig
import com.screentheme.app.models.CallThemeConfigModel
import com.screentheme.app.utils.Tracking
import com.screentheme.app.utils.extensions.isAlreadyDefaultDialer
import com.screentheme.app.utils.helpers.SharePreferenceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class SplashViewModel(
    val context: Context
) : ViewModel() {

    private val FETCH_REMOTE_CONFIG_TIMEOUT = 10000L // 10 seconds
    var nextScreenRoute = MutableLiveData<String>()
    var isShownLoadingAds = MutableLiveData<Boolean>()
    var callThemeConfig = MutableLiveData<CallThemeConfigModel>()

    private val REQUEST_TIME_OUT = 30000L
    private val TIME_DELAY = 3000L
    private val TAG = SplashViewModel::class.simpleName
    private var startTime = 0L
    private var interSplashIsReadyToShow = false
    fun startSplashFlow(activity: AppCompatActivity) {
        viewModelScope.launch {
            val remoteConfigResult = withTimeoutOrNull(FETCH_REMOTE_CONFIG_TIMEOUT) {
                RemoteConfig.fetchAndActive()
            }
            if (remoteConfigResult != null) {
                Tracking.logEvent(
                    "remote_config_loaded_success",
                    bundleOf("ad_available" to !AppRemoteConfig.offAllAds())
                )
            } else {
                Tracking.logEvent(
                    "remote_config_loaded_fail",
                    bundleOf("ad_available" to !AppRemoteConfig.offAllAds())
                )
            }
            callThemeConfig.value = AppRemoteConfig.callThemeConfigs()
            Firebase.analytics.setUserProperty("ad_available", isAdsAvailable.toString())
            if (isAdsAvailable) {
                initAds()
                loadAds(activity)
                if (!isOnboardingCompleted) {
                    AdsUtils.requestNativeLanguageFirstOpen(activity, AppRemoteConfig.nativeLanguage)
                    if (AppRemoteConfig.nativeLanguageDuplicate) {
                        AdsUtils.requestNativeLanguageDuplicateFirstOpen(activity)
                    }
                } else {
                    if (!context.isAlreadyDefaultDialer()) {
                        AdsUtils.requestNativePermission(activity)
                    } else {
                        AdsUtils.requestNativeHome(activity)
                    }
                }
                BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.HOME, activity)
                AdsUtils.loadInterCustomize(activity)
            } else {
                AdsUtils.isCloseAdSplash.postValue(true)
                onNavigate()
            }
        }
    }

    private fun loadAds(activity: AppCompatActivity) {
        if (!isOnboardingCompleted || !AppRemoteConfig.nativeSplash) {
            loadInter(activity)
        } else {
            loadNative(activity)
        }

    }

    private fun loadNative(activity: AppCompatActivity) {
        AdsUtils.requestNativeSplash(
            context = activity,
            onNextAction = {
                onNavigate()
            }
        )
    }

    private fun loadInter(activity: AppCompatActivity) {
        interSplashIsReadyToShow = false
        Log.d(TAG, "loadInterSplash: ")
        if (AppRemoteConfig.interSplash) {
            load3InterSplash(activity)
        } else {
            loadInterSplash(activity = activity)
        }
    }

    private fun onNavigate() {
        if (!isOnboardingCompleted) {
            nextScreenRoute.value = ROUTE_LANGUAGE
        } else {
            nextScreenRoute.value = ROUTE_HOME
        }
    }

    private fun load3InterSplash(activity: AppCompatActivity) {
        VioAdmob.getInstance().loadSplashInterPriority3SameTime(activity,
            BuildConfig.inter_splash_high,
            BuildConfig.inter_splash_medium,
            BuildConfig.inter_splash,
            REQUEST_TIME_OUT,
            TIME_DELAY,
            false,
            object : VioAdmobCallback() {
                override fun onAdSplashPriorityReady() {
                    super.onAdSplashPriorityReady()
                    Log.i(
                        TAG,
                        "onAdSplashHighFloorReady: "
                    )
                    showInter3Sametime(activity)
                }

                override fun onAdPriorityFailedToLoad(adError: ApAdError?) {
                    super.onAdPriorityFailedToLoad(adError)
                    Log.e(
                        TAG,
                        "onAdHighFloorFailedToLoad: $adError"
                    )
                }

                override fun onAdSplashPriorityMediumReady() {
                    super.onAdSplashPriorityMediumReady()
                    Log.i(
                        TAG,
                        "onAdSplashHighMediumReady: "
                    )
                    showInter3Sametime(activity)
                }

                override fun onAdPriorityMediumFailedToLoad(adError: ApAdError?) {
                    super.onAdPriorityMediumFailedToLoad(adError)
                    Log.i(
                        TAG,
                        "onAdMediumFailedToLoad: $adError"
                    )
                }

                override fun onAdSplashReady() {
                    super.onAdSplashReady()
                    Log.i(TAG, "onAdSplashReady: ")
                    showInter3Sametime(activity)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Log.d(
                        TAG,
                        "onAdFailedToLoad: "
                    )
                }

                override fun onNextAction() {
                    super.onNextAction()
                    onNavigate()
                }
            })
    }

    private fun showInter3Sametime(activity: AppCompatActivity) {
        viewModelScope.launch {
            val loadTime = System.currentTimeMillis() - startTime
            if (loadTime < MIN_LOAD_TIME) {
                delay(MIN_LOAD_TIME - loadTime)
            }
            interSplashIsReadyToShow = true
            VioAdmob.getInstance()
                .onShowSplashPriority3(activity, object : VioAdmobCallback() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        Log.d(TAG, "onAdClosed: ")
                        AdsUtils.isCloseAdSplash.postValue(true)
                    }

                    override fun onAdPriorityFailedToShow(adError: ApAdError?) {
                        super.onAdPriorityFailedToShow(adError)
                        AdsUtils.isCloseAdSplash.postValue(true)

                        Log.d(
                            TAG,
                            "onAdPriorityFailedToShow: "
                        )
                    }

                    override fun onAdPriorityMediumFailedToShow(adError: ApAdError?) {
                        super.onAdPriorityMediumFailedToShow(adError)
                        AdsUtils.isCloseAdSplash.postValue(true)
                        Log.d(
                            TAG,
                            "onAdPriorityMediumFailedToShow: "
                        )
                    }

                    override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)
                        AdsUtils.isCloseAdSplash.postValue(true)
                        Log.d(
                            TAG,
                            "onAdFailedToShow: "
                        )
                    }

                    override fun onNextAction() {
                        super.onNextAction()
                        onNavigate()
                        Log.d(TAG, "onNextAction: ")
                    }
                })
        }
    }

    private fun loadInterSplash(activity: AppCompatActivity) {
        startTime = System.currentTimeMillis()
        Tracking.logEvent("load_inter_splash")
        Log.d(TAG, "loadInterSplash: ")
        VioAdmob.getInstance().loadSplashInterstitialAds(
            activity,
            BuildConfig.inter_splash,
            REQUEST_TIME_OUT,
            TIME_DELAY,
            false,
            object :
                VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    onNavigate()
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Log.d("Splash", "Failed to load ${adError?.message}")
                    Tracking.logEvent("inter_splash_load_fail")
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
                    Tracking.logEvent("show_fail_${adError?.message}")
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
                        Tracking.logEvent("show_fail_${adError?.message}")
                    }
                },
                1000
            )
    }

    fun checkShowInterSplashPriority3WhenFail(activity: AppCompatActivity) {
        if (!interSplashIsReadyToShow) return
        VioAdmob.getInstance()
            .onCheckShowSplashPriority3WhenFail(activity, object : VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    AdsUtils.isCloseAdSplash.postValue(false)
                    onNavigate()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdsUtils.isCloseAdSplash.postValue(true)
                }
            }, 1000)

    }

    val isOnboardingCompleted: Boolean
        get() = runBlocking {
            SharePreferenceHelper.getBoolean(
                context,
                SharePreferenceHelper.KEY_ALREADY_WENT_THROUGH_INFO,
                false
            )
        }
    val isAdsAvailable
        get() = !(AppRemoteConfig.offAllAds() || SharePreferenceHelper.getBoolean(
            context,
            SharePreferenceHelper.KEY_IS_PURCHASED,
            false
        ))

    fun setLoadingAds(value: Boolean) {
        isShownLoadingAds.postValue(value)
    }

    private fun initAds() {
        AppOpenManager.getInstance().enableAppResume()
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
    }

    companion object {
        private const val MIN_LOAD_TIME = 3000L
        internal const val ROUTE_LANGUAGE = "language"
        internal const val ROUTE_HOME = "home"
    }
}