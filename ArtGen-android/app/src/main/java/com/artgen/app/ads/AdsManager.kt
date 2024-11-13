package com.artgen.app.ads

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.log.Logger
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AdsManager(
    private val activity: Activity,
) : KoinComponent, DefaultLifecycleObserver {
    private val interAdsManager: InterAdsManager by inject()
    private val nativeAdsManager: NativeAdsManager by inject()
    private val adsInitializer: AdsInitializer by inject()
    private val dataStore: AppDataStore by inject()
    private val openAdsManager: OpenAdsManager by inject()
    private val bannerAdsManager = BannerAdsManager(activity, dataStore, openAdsManager)
    private val rewardAdsManager: RewardAdsManager by inject()

    val cropPhotoBannerAd: StateFlow<AdView?>
        get() = bannerAdsManager.cropPhotoAdView

    val genArtBannerAd: StateFlow<AdView?>
        get() = bannerAdsManager.genArtBannerAd

    val resultBannerAd: StateFlow<AdView?>
        get() = bannerAdsManager.resultArtBannerAd

    val onboardingNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.onboardingNativeAd
    val onboardingNativeAd2: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.onboardingNativeAd2
    val onboardingNativeAd3: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.onboardingNativeAd3

    val languageNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.languageNativeAd

    val languageSettingNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.languageSettingNativeAd

    val stylePickerNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.stylePickerNativeAd

    val imagePickerNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.imagePickerNativeAd

    val allStyleNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.allStyleNativeAd

    val allDoneNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.allDoneNativeAd

    val resultNativeAd: StateFlow<NativeAdWrapper?>
        get() = nativeAdsManager.resultNativeAd

    private fun loadAds() {
        loadLanguageNativeAd()
    }

    fun loadRewardedVideoAds() {
        rewardAdsManager.loadAds()
    }

    fun loadSaveArtRewardedAds(reload: Boolean = false) {
        rewardAdsManager.loadSaveArtRewardAds(reload)
    }

    fun loadOnboardingNativeAd() {
        if (!dataStore.isOnboardingShown) {
            nativeAdsManager.loadOnboardingNativeAd()
            nativeAdsManager.loadOnboardingNativeAd(1)
            nativeAdsManager.loadOnboardingNativeAd(2)
            Logger.d("AdsManager: loadOnboardingNativeAd")
        }
    }

    fun loadRemainingOnboardingNativeAds() {
        if (!dataStore.isOnboardingShown) {
            Logger.d("AdsManager: loadOnboardingNativeAd")
        }
    }

    fun loadLanguageNativeAd() {
        if (!dataStore.isLanguageSelected) {
            nativeAdsManager.loadLanguageNativeAd()
            Logger.d("AdsManager: loadLanguageNativeAd")
        }
    }

    fun loadStylePickerNativeAd(reload: Boolean = false) {
        nativeAdsManager.loadStylePickerNativeAd(reload)
        Logger.d("AdsManager: loadStylePickerNativeAd")
    }

    fun loadImagePickerNativeAd(reload: Boolean = false) {
        nativeAdsManager.loadImagePickerNativeAd(reload)
        Logger.d("AdsManager: loadImagePickerNativeAd")
    }

    fun loadAllStyleNativeAd(reload: Boolean = false) {
        nativeAdsManager.loadAllStyleNativeAd(reload)
        Logger.d("AdsManager: loadAllStyleNativeAd")
    }

    fun loadAllDoneNativeAd(reload: Boolean = false) {
        nativeAdsManager.loadAllDoneNativeAd(reload)
        Logger.d("AdsManager: loadAllDoneNativeAd")
    }

    fun loadLanguageSettingNativeAd() {
        nativeAdsManager.loadLanguageSettingNativeAd()
    }

    fun loadResultNativeAd(reload: Boolean = false) {
        nativeAdsManager.loadResultNativeAd(reload)
        Logger.d("AdsManager: loadResultNativeAd")
    }

    fun loadBannerAd(placement: BannerAdsManager.BannerAdPlacement) {
        bannerAdsManager.loadAd(placement)
    }

    fun showGenArtRewardedVideoAd(onComplete: (Boolean) -> Unit, onRewardedAd: (Boolean) -> Unit) {
        rewardAdsManager.showRewardedVideo(
            placement = RewardAdsManager.RewardedVideoAdPlacement.GEN_ART,
            activity = activity,
            onComplete = onComplete,
            onRewardedAd = onRewardedAd
        )
    }

    fun showSaveArtRewardVideoAd(onComplete: (Boolean) -> Unit, onRewardedAd: (Boolean) -> Unit) {
        rewardAdsManager.showRewardedVideo(
            placement = RewardAdsManager.RewardedVideoAdPlacement.SAVE_ART,
            activity = activity,
            onComplete = onComplete,
            onRewardedAd = onRewardedAd
        )
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
                        loadAds()
                    }
                }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        if(dataStore.isLanguageSelected) {
            onDestroy()
        }
    }

    private fun onDestroy() {
        nativeAdsManager.onDestroy()
        bannerAdsManager.onDestroy()
        rewardAdsManager.onDestroy()
    }

    fun destroyStylePickerNativeAd() {
        nativeAdsManager.destroyStylePickerNativeAd()
    }
}
