package com.wifi.wificharger.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.wifi.wificharger.BuildConfig
import com.wifi.wificharger.R
import com.wifi.wificharger.data.remote.RemoteConfig
import java.util.EnumMap

object AdsUtils {
    private var interDetailCount = 1
    val nativeLanguageFirstOpen = MutableLiveData<ApNativeAd?>()
    val nativeDetail = MutableLiveData<ApNativeAd?>()
    val nativeHome = MutableLiveData<ApNativeAd?>()
    val nativeLanguageDuplicateFirstOpen = MutableLiveData<ApNativeAd?>()
    val nativeExit = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding1 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding2 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding3 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoardingFailLoad = MutableLiveData<Boolean>()
    val isCloseAdSplash = MutableLiveData<Boolean>()
    private var requestPermissionInProcess = false
    val permissionApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val permissionApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()
    private var requestState: EnumMap<InterPlacement, RequestState> = EnumMap(
        mapOf(
            InterPlacement.HOME to RequestState.IDE
        )
    )
    private var lastTimeAdsShown: Long = 0
    private var lastTimeClosePopup: Long? = null
    private var rewardedShowPasswordAds: RewardedAd? = null
    private var rewardedShowPasswordLoadingState: Boolean = false
    private var _interHome: ApInterstitialAd? = null

    val isAdEnabled: Boolean
        get() = !RemoteConfig().offAllAds()
    private var countLoading = 0
    private var adLoader: AdLoader? = null
    private val remoteConfig = RemoteConfig()

    fun requestNativeLanguageFirstOpen(activity: Activity, reload: Boolean = false) {
        if (nativeLanguageFirstOpen.value != null && !reload) {
            return
        }
        if (canLoadAds(activity)) {
            Logger.d("requestNativeLanguageFirstOpen: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                BuildConfig.native_language,
                R.layout.layout_native_ad_permission,
                object : VioAdmobCallback() {
                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        nativeLanguageFirstOpen.postValue(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        Logger.d("nativeFirstOpen: ${adError?.message}, ${BuildConfig.native_language}")
                        nativeLanguageFirstOpen.postValue(null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeLanguageFirstOpen(activity, reload = true)
                    }
                }
            )
        } else {
            nativeLanguageFirstOpen.postValue(null)
        }
    }

    fun requestNativeLanguageDuplicateFirstOpen(activity: Activity, reload: Boolean = false) {
        if (nativeLanguageDuplicateFirstOpen.value != null && !reload) {
            return
        }
        if (canLoadAds(activity)) {
            Logger.d("requestNativeLanguageDuplicateFirstOpen: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                BuildConfig.native_language_dup,
                R.layout.layout_native_ad_permission,
                object : VioAdmobCallback() {
                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        nativeLanguageDuplicateFirstOpen.postValue(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        nativeLanguageDuplicateFirstOpen.postValue(null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeLanguageDuplicateFirstOpen(activity, true)
                    }
                }
            )
        } else {
            nativeLanguageDuplicateFirstOpen.postValue(null)
        }
    }

    fun requestNativeExit(activity: Activity, reload: Boolean = false) {
        if (nativeExit.value != null && !reload) {
            return
        }
        if (canLoadAds(activity)) {
            Logger.d("requestNativeExit: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                BuildConfig.native_exit,
                R.layout.layout_native_ad_permission,
                object : VioAdmobCallback() {
                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        nativeExit.postValue(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        nativeExit.postValue(null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeExit(activity, true)
                    }
                }
            )
        } else {
            nativeExit.postValue(null)
        }
    }

    fun requestNativePermission(
        activity: Activity,
        reload: Boolean = false
    ) {
        if (!canLoadAds(activity)) {
            permissionApNativeAdLoadFail.postValue(true)
            return
        }

        if (permissionApNativeAd.value != null && !reload && requestPermissionInProcess) {
            return
        }

        Logger.d("loadNativePermission")
        if (BuildConfig.DEBUG) {
            Toast.makeText(activity, "loadNativePermission: isReload = $reload", Toast.LENGTH_SHORT).show()
        }
        permissionApNativeAdLoadFail.postValue(false)
        requestPermissionInProcess = true
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            BuildConfig.native_permission,
            R.layout.layout_native_ad_permission,
            object : VioAdmobCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    requestNativePermission(activity, true)
                }

                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    Logger.d("loadNativePermission loaded")
                    permissionApNativeAd.postValue(nativeAd)
                    requestPermissionInProcess = false
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Logger.d("loadNativePermission: $adError")
                    permissionApNativeAdLoadFail.postValue(true)
                    requestPermissionInProcess = false
                }
            }
        )

    }

    fun requestNativeDetail(activity: Activity, reload: Boolean = false) {
        if (nativeDetail.value != null && !reload) {
            return
        }
        if (canLoadAds(activity)) {
            Logger.d("requestNativeDetail: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                BuildConfig.native_detail,
                R.layout.layout_native_non_media,
                object : VioAdmobCallback() {
                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        nativeDetail.postValue(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        nativeDetail.postValue(null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeDetail(activity, reload = true)
                    }
                }
            )
        } else {
            nativeDetail.postValue(null)
        }
    }

    fun requestNativeHome(activity: Activity, reload: Boolean = false) {
        if (nativeHome.value != null && !reload) {
            return
        }
        if (canLoadAds(activity)) {
            Logger.d("requestNativeHome: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                BuildConfig.native_home,
                R.layout.layout_native_non_media_white_background,
                object : VioAdmobCallback() {
                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        nativeHome.postValue(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        nativeHome.postValue(null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeHome(activity, reload = true)
                    }
                }
            )
        } else {
            nativeHome.postValue(null)
        }
    }

    fun loadNativeOnboard(context: Context) {
        Logger.d("loadNativeOnboard")
        if (!canLoadAds(context)) {
            nativeOnBoardingFailLoad.postValue(true)
            return
        }
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        adLoader = AdLoader.Builder(context, BuildConfig.native_onboarding)
            .forNativeAd { nativeAd ->
                when (countLoading) {
                    0 -> {
                        nativeOnBoarding1.postValue(
                            ApNativeAd(
                                R.layout.layout_native_ad_permission,
                                nativeAd
                            )
                        )
                    }

                    1 -> {
                        nativeOnBoarding2.postValue(
                            ApNativeAd(
                                R.layout.layout_native_ad_permission,
                                nativeAd
                            )
                        )
                    }

                    2 -> {
                        nativeOnBoarding3.postValue(
                            ApNativeAd(
                                R.layout.layout_native_ad_permission,
                                nativeAd
                            )
                        )
                    }
                }
                countLoading++
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Logger.d("onAdFailedToLoad: ${error.message}")
                    if (adLoader?.isLoading == false) {
                        nativeOnBoardingFailLoad.postValue(true)
                        loadNativeOnboard(context)
                    }
                }


                override fun onAdClicked() {
                    super.onAdClicked()
                    Session.isNativeAdClick = true
                }

                override fun onAdImpression() {
                    super.onAdImpression()
//                        TrackingManager.logNativeAdImpression(
//                            "show_native_onboarding_ad_success"
//                        )
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.d("onAdLoaded: ")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader?.loadAds(Admob.getInstance().adRequest, 3 - countLoading)
    }

    var backupOnboardingAdLoader: AdLoader? = null
    var backupOnboardingRetryCount = 0
    val backupOnboardingAds: MutableList<ApNativeAd?> = mutableListOf()
    fun loadBackupOnboardingAd(context: Context, numberOfAd: Int = 2) {
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
            .build()
        backupOnboardingAdLoader =
            AdLoader.Builder(context, BuildConfig.native_onboarding)
                .forNativeAd { nativeAd ->
                    Logger.d("backup ad was added to inventory")
                    backupOnboardingAds.add(ApNativeAd(
                        R.layout.layout_native_onboarding,
                        nativeAd
                    ))
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        if (backupOnboardingAdLoader?.isLoading == false && backupOnboardingRetryCount <= 1) {
                            backupOnboardingRetryCount++
                            loadBackupOnboardingAd(context, 2 - backupOnboardingAds.size)
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Session.isNativeAdClick = true
                    }
                })
                .withNativeAdOptions(adOptions)
                .build()
        backupOnboardingAdLoader?.loadAds(AdRequest.Builder().build(), numberOfAd)
    }

    private fun interHomeReady(): Boolean {
        return _interHome != null && _interHome?.isReady == true && isAdEnabled
    }
    fun loadInterHome(context: Context) {
        if (canLoadAds(context)
            && !interHomeReady()
            && requestState[InterPlacement.HOME] != RequestState.LOADING
        ) {
            Logger.d("loadInterTheme: ")
            requestState[InterPlacement.HOME] = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(context, BuildConfig.inter_home, object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interHome = interstitialAd
                        requestState[InterPlacement.HOME] = RequestState.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestState[InterPlacement.HOME] = RequestState.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterDetail(context: Context, onNavigate:() -> Unit, onNextAction: () -> Unit) {
        if (interHomeReady() && isAdEnabled && interDetailCount++ % 2 != 0) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interHome,
                object : VioAdmobCallback() {

                    override fun onNextAction() {
                        super.onNextAction()
                        onNavigate()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        onNextAction()
                    }

                    override fun onAdFailedToShow(adError: ApAdError?) {
                        super.onAdFailedToShow(adError)
                        onNextAction()
                    }
                },
                true
            )
        } else {
            onNavigate()
            onNextAction()
        }
    }

    fun showRewardedVideo(
        activity: FragmentActivity,
        onComplete: ((Boolean) -> Unit)? = null,
        onRewardedAd: ((Boolean) -> Unit)? = null
    ) {
        if (rewardedShowPasswordAds == null && !rewardedShowPasswordLoadingState) {
            loadShowPasswordRewardedAd(activity)
            onComplete?.invoke(false)
            onRewardedAd?.invoke(true)
            return
        }

        if (rewardedShowPasswordAds == null || rewardedShowPasswordLoadingState || !canShowAd(activity) || isRateLimitReached()) {
            onRewardedAd?.invoke(true)
            onComplete?.invoke(false)
            return
        }

        var onRewarded = false
        rewardedShowPasswordAds?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedShowPasswordAds?.fullScreenContentCallback = null
                    lastTimeClosePopup = System.currentTimeMillis()
                    onRewardedAd?.invoke(onRewarded)
                    onComplete?.invoke(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedShowPasswordAds?.fullScreenContentCallback = null
                    onComplete?.invoke(false)
                    onRewardedAd?.invoke(true)
                    Logger.e(Throwable("onRewardAdFailedToShowFullScreenContent: ${adError.message}"))
                }

                override fun onAdShowedFullScreenContent() {
                    lastTimeAdsShown = System.currentTimeMillis()
                    rewardedShowPasswordAds = null
                    loadShowPasswordRewardedAd(activity)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadShowPasswordRewardedAd(activity)
                }
            }

        rewardedShowPasswordAds?.let {

            it.show(activity) {
                AppOpenManager.getInstance().disableAppResume()
                onRewarded = true
            }
        } ?: run {
            onComplete?.invoke(false)
        }
    }

    fun loadShowPasswordRewardedAd(context: Context, reload: Boolean = false) {
        if ((!canShowAd(context) || rewardedShowPasswordLoadingState || rewardedShowPasswordAds != null) && !reload) {
            return
        }
        TrackingManager.logEvent("load_reward_ads")

        rewardedShowPasswordLoadingState = true
        RewardedAd.load(
            context,
            BuildConfig.rewarded_show_password,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedShowPasswordAds = ad
                    rewardedShowPasswordLoadingState = false
                    Logger.d(this.javaClass.simpleName, "onAdLoaded: $ad")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedShowPasswordAds = null
                    rewardedShowPasswordLoadingState = false
                    Logger.e(Throwable("onAdFailedToLoad: ${loadAdError.message}"))
                }
            }
        )
    }

    private fun canShowAd(context: Context): Boolean {
        return isAdEnabled
    }

    private fun isRateLimitReached(): Boolean =
        System.currentTimeMillis() - lastTimeAdsShown < remoteConfig.offsetTimeShowInterAds() * 1000

    fun canLoadAds(context: Context): Boolean = context.isInternetAvailable() && isAdEnabled
}

enum class RequestState {
    IDE, LOADING, COMPLETE
}

enum class InterPlacement {
    HOME
}