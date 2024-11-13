package com.bloodpressure.app.ads

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.utils.Logger
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AdsManager(
    private val activity: Activity,
) : KoinComponent, DefaultLifecycleObserver {


    private val interAdsManager: InterAdsManager by inject()
    private val nativeAdsManager: NativeAdsManager by inject()
    private val adsInitializer: AdsInitializer by inject()
    private val dataStore: AppDataStore by inject()
    private val remoteConfig: RemoteConfig by inject()
    private val bannerAdsManager = BannerAdsManager(activity)

    val homeBannerAd: StateFlow<AdView?>
        get() = bannerAdsManager.homeAdView

    val trackerNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.trackerNativeAd

    val onboardingNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.onboardingNativeAd

    val onboardingNativeAd1: StateFlow<NativeAd?>
        get() = nativeAdsManager.onboardingNativeAd1

    val onboardingNativeAd2: StateFlow<NativeAd?>
        get() = nativeAdsManager.onboardingNativeAd2

    val onboardingNativeAd3: StateFlow<NativeAd?>
        get() = nativeAdsManager.onboardingNativeAd3

    val heartRateNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.featuresNativeAd[NativeAdPlacement.NATIVE_HEART_RATE] ?: MutableStateFlow(null)

    val bloodPressureNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_PRESSURE] ?: MutableStateFlow(null)

    val bmiNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.featuresNativeAd[NativeAdPlacement.NATIVE_BMI] ?: MutableStateFlow(null)

    val bloodSugarNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_SUGAR] ?: MutableStateFlow(null)

    val historyNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.historyNativeAd

    val infoNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.infoNativeAd

    val languageNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.languageNativeAd

    val languageSettingNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.languageSettingNativeAd

    val addRecordNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.addRecordNativeAd

    val exitAppNativeAd: StateFlow<NativeAd?>
        get() = nativeAdsManager.exitAppNativeAd

    fun loadInterAds() {
        interAdsManager.loadAds()
    }

    fun loadBannerAds() {
        if (remoteConfig.adsConfig.shouldShowHomeBannerAd) {
            bannerAdsManager.loadBannerAd()
        }
    }

    fun reloadBannerAds() {
        if (remoteConfig.adsConfig.shouldShowHomeBannerAd) {
            bannerAdsManager.reloadBannerAd()
        }
    }

    fun loadHomeNativeAd() {
        nativeAdsManager.loadHomeNativeAd()
    }

    fun loadOnboardingNativeAd() {
        if (!dataStore.isOnboardingShown) {
            nativeAdsManager.loadNativeOnboard()
            Logger.d("AdsManager: loadOnboardingNativeAd")
        }
    }

    fun loadInfoNativeAd() {
        nativeAdsManager.loadInfoNativeAd()
    }

    private fun loadLanguageNativeAd() {
        if (!dataStore.isLanguageSelected) {
            nativeAdsManager.loadLanguageNativeAd()
            Logger.d("AdsManager: loadLanguageNativeAd")
        }
    }

    fun loadHistoryNativeAd() {
        nativeAdsManager.loadHistoryNativeAd()
    }

    fun loadLanguageSettingNativeAd() {
        nativeAdsManager.loadLanguageSettingNativeAd()
    }

    fun loadAddRecordNativeAd() {
        nativeAdsManager.loadAddRecordNativeAd()
    }

    fun loadExitAppNativeAdIfNeeded() {
        if (exitAppNativeAd.value == null) {
            nativeAdsManager.loadExitAppNativeAd()
        }
    }

    suspend fun loadFeatureNative() {
        withTimeout(20000) {
            nativeAdsManager.loadFeatureNativeAd()
        }
    }

    fun loadAddRecordFeatureInter(type: TrackerType) {
        interAdsManager.loadAddFeatureAds(type)
    }

    fun showClickAddRecordAd(onComplete: () -> Unit) {
        if (remoteConfig.adsConfig.shouldShowClickAddRecordInterAd) {
            interAdsManager.showInterstitial(
                placement = InterAdsManager.InterAdPlacement.CLICK_ADD_RECORD,
                activity = activity,
                onComplete = { onComplete.invoke() }
            )
        } else {
            onComplete.invoke()
        }
    }

    fun showClickHistoryItemAd(onComplete: () -> Unit) {
        if (remoteConfig.adsConfig.shouldShowClickHistoryItemInterAd) {
            interAdsManager.showInterstitial(
                placement = InterAdsManager.InterAdPlacement.CLICK_HISTORY_ITEM,
                activity = activity,
                onComplete = { onComplete.invoke() }
            )
        } else {
            onComplete.invoke()
        }
    }

    fun showClickInfoItemAd() {
        if (remoteConfig.adsConfig.shouldShowClickInfoItemInterAd) {
            interAdsManager.showInterstitial(
                placement = InterAdsManager.InterAdPlacement.CLICK_INFO_ITEM,
                activity = activity,
                onComplete = {}
            )
        }
    }

    fun showExitAppAd(onComplete: (Boolean) -> Unit) {
        if (remoteConfig.adsConfig.shouldShowExitInterAd) {
            interAdsManager.showInterstitial(
                placement = InterAdsManager.InterAdPlacement.EXIT_APP,
                activity = activity,
                onComplete = onComplete
            )
        } else {
            onComplete.invoke(false)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycleScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isProUser ->
                    nativeAdsManager.setProUser(isProUser)
                    if (isProUser) {
                        onDestroy()
                    } else {
                        adsInitializer.waitUntilAdsInitialized()
                        loadLanguageNativeAd()
                    }
                }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onDestroy()
    }

    private fun onDestroy() {
        nativeAdsManager.onDestroy()
        interAdsManager.onDestroy()
        bannerAdsManager.onDestroy()
    }

    fun showHeartRateInterAds(onComplete: (Boolean) -> Unit) {
        interAdsManager.showInterstitial(
            InterAdsManager.InterAdPlacement.ADD_HEART_RATE,
            activity,
            onComplete
        )
    }

    fun showAddFeatureInterAds(type: TrackerType, onComplete: (Boolean) -> Unit) {
        val interAdsPlace = when (type) {
            TrackerType.HEART_RATE -> InterAdsManager.InterAdPlacement.ADD_HEART_RATE
            TrackerType.WEIGHT_BMI -> InterAdsManager.InterAdPlacement.ADD_BMI
            TrackerType.BLOOD_SUGAR -> InterAdsManager.InterAdPlacement.ADD_SUGAR
            TrackerType.WATER_REMINDER -> InterAdsManager.InterAdPlacement.ADD_WATER
            else -> throw IllegalArgumentException("Unsupported info item $type")
        }
        interAdsManager.showInterstitial(
            interAdsPlace,
            activity,
            onComplete
        )
    }

}
