package com.calltheme.app.utils

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.calltheme.app.ui.activity.MainActivity
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
import com.screentheme.app.BuildConfig
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.utils.Session
import com.screentheme.app.utils.Tracking
import com.screentheme.app.utils.extensions.isInternetAvailable
import com.screentheme.app.utils.helpers.BillingClientProvider
import kotlin.properties.Delegates

object AdsUtils {
    private val TAG = AdsUtils::class.simpleName
    val nativeLanguageFirstOpen = MutableLiveData<ApNativeAd?>()
    val nativeLanguageDuplicateFirstOpen = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding1 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding2 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding3 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoardingFailLoad = MutableLiveData<Boolean>()
    val isCloseAdSplash = MutableLiveData<Boolean>()
    private var requestPermissionInProcess = false
    val permissionApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val permissionApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    private var requestNativeFullScreenInProcess = false
    val nativeFullScreenApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val nativeFullScreenApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val setCallThemeApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val setCallThemeApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val homeApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val homeApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    private var requestSetCallThemeInProcess = false

    private var lastTimeAdsShown: Long = 0
    private var lastTimeClosePopup: Long? = null
    private var rewardedSetCallThemeVideoAds: RewardedAd? = null
    private var rewardSetCallLoadingState: Boolean = false

    private var _interCustomize: ApInterstitialAd? = null
    private var _interTheme: ApInterstitialAd? = null
    private var _interAvatar: ApInterstitialAd? = null
    private var _interBackground: ApInterstitialAd? = null
    private var _interIcon: ApInterstitialAd? = null
    private var _interPreview: ApInterstitialAd? = null
    private var _interCategory: ApInterstitialAd? = null
    private var countClickCustomize = 0
    private var requestStateCustomize = RequestState.IDE
    private var requestStateTheme = RequestState.IDE
    private var requestStateAvatar = RequestState.IDE
    private var requestStateBackground = RequestState.IDE
    private var requestStateIcon = RequestState.IDE
    private var requestStatePreview = RequestState.IDE
    private var requestStateCategory = RequestState.IDE
    val isAdEnabled: Boolean
        get() = !AppRemoteConfig.offAllAds()
    val isInterPreviewVideo: Boolean
        get() = AppRemoteConfig.interPreviewVideo
    private var countLoading = 0
    private var adLoader: AdLoader? = null

    var stackNative by Delegates.observable(ArrayDeque<ApNativeAd>()) { _, old, new ->
        if (old.size == 0 && new.size != 0) {
            triggerRebind.postValue(true)
        }
    }
    val triggerRebind = MutableLiveData(false)

    fun requestNativeHome(context: Context, reload: Boolean = false) {
        if(homeApNativeAd.value != null && !reload) {
            return
        }

        if(BillingClientProvider.getInstance(context as Activity).isPurchased || !context.isInternetAvailable()) {
            homeApNativeAdLoadFail.postValue(true)
            return
        }
        VioAdmob.getInstance().loadNativeAdResultCallback(
            context,
            BuildConfig.native_home,
            R.layout.layout_native_ad_small,
            object : VioAdmobCallback() {
                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    homeApNativeAdLoadFail.postValue(true)
                }

                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    homeApNativeAd.postValue(nativeAd)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    requestNativeHome(context, true)
                }
            }
        )
    }

    fun requestNativeHomeList(context: Context, reload: Boolean = false) {
        if (!isAdEnabled) return
        if (context.isInternetAvailable()) {
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()
            val adLoader = AdLoader.Builder(context, BuildConfig.native_home)
                .forNativeAd { nativeAd ->
                    val stack = ArrayDeque(stackNative)
                    stack.add(
                        ApNativeAd(
                            R.layout.layout_native_ad_item_list,
                            nativeAd
                        )
                    )

                    stackNative = stack
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.d(TAG, "nativeHomeListLoaded: ${error.message}")
                    }


                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeHomeList(context)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        requestNativeHomeList(context)
                    }
                })
                .withNativeAdOptions(adOptions)
                .build()
            adLoader.loadAds(Admob.getInstance().adRequest, 5 - stackNative.size)
        }
    }

    fun requestNativeLanguageFirstOpen(context: Context, requestHighFloor: Boolean = true, reload: Boolean = false) {
        if (nativeLanguageFirstOpen.value != null && !reload) {
            return
        }
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && context.isInternetAvailable()) {
            Log.d(TAG, "requestNativeLanguageFirstOpen: ")
            if (requestHighFloor) {
                VioAdmob.getInstance().loadNative3SameTime(
                    context,
                    BuildConfig.native_language_high,
                    BuildConfig.native_language_medium,
                    BuildConfig.native_language,
                    R.layout.layout_native_ad_big,
                    object : VioAdmobCallback() {
                        override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            nativeLanguageFirstOpen.postValue(nativeAd)
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            nativeLanguageFirstOpen.postValue(null)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            requestNativeLanguageFirstOpen(context)
                        }
                    }
                )
            } else {
                VioAdmob.getInstance().loadNativeAdResultCallback(
                    context,
                    BuildConfig.native_language,
                    R.layout.layout_native_ad_big,
                    object : VioAdmobCallback() {
                        override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            nativeLanguageFirstOpen.postValue(nativeAd)
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            nativeLanguageFirstOpen.postValue(null)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            requestNativeLanguageFirstOpen(context, reload = true)
                        }
                    }
                )
            }
        } else {
            nativeLanguageFirstOpen.postValue(null)
        }
    }

    fun requestNativeLanguageDuplicateFirstOpen(context: Context, reload: Boolean = false) {
        if (nativeLanguageDuplicateFirstOpen.value != null && !reload) {
            return
        }
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && context.isInternetAvailable()) {
            Log.d(TAG, "requestNativeLanguageDuplicateFirstOpen: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                context,
                BuildConfig.native_language_dup,
                R.layout.layout_native_ad_big,
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
                        requestNativeLanguageDuplicateFirstOpen(context, reload = true)
                    }
                }
            )
        } else {
            nativeLanguageDuplicateFirstOpen.postValue(null)
        }
    }

    fun requestNativeSplash(
        context: Context,
        reload: Boolean = false,
        onNextAction: () -> Unit
    ) {
        if (!isAdEnabled) {
            nativeFullScreenApNativeAdLoadFail.postValue(true)
            return
        }

        if (nativeFullScreenApNativeAd.value != null && !reload) {
            return
        }

        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && context.isInternetAvailable()) {
            Log.d(TAG, "requestNativeSplash: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                context,
                BuildConfig.native_splash,
                R.layout.layout_native_ad_fullscreen,
                object : VioAdmobCallback() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeSplash(context, reload = true, onNextAction = onNextAction)
                    }

                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        Log.d(TAG, "requestNativeSplash loaded")
                        nativeFullScreenApNativeAd.postValue(nativeAd)
                        requestNativeFullScreenInProcess = false
                        onNextAction()
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        Log.d(TAG, "requestNativeSplash: $adError")
                        nativeFullScreenApNativeAdLoadFail.postValue(true)
                        requestNativeFullScreenInProcess = false
                        onNextAction()
                    }
                }
            )
        } else {
            nativeFullScreenApNativeAdLoadFail.postValue(true)
        }
    }

    fun requestNativeSaveThemeSuccessfully(
        context: Context,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && context.isInternetAvailable()) {
            Log.d(TAG, "requestNativeSaveThemeSuccessfully: ")
            VioAdmob.getInstance().loadNativeAdResultCallback(
                context,
                BuildConfig.native_save_theme,
                R.layout.layout_native_ad_small_full,
                object : VioAdmobCallback() {
                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        onLoaded(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        onLoadFail()
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        requestNativeSaveThemeSuccessfully(context, onLoaded, onLoadFail)
                    }
                }
            )
        } else {
            onLoadFail()
        }
    }

    fun requestNativeSetCallTheme(
        activity: Activity,
        reload: Boolean = false
    ) {
        if (!isAdEnabled) {
            setCallThemeApNativeAdLoadFail.postValue(true)
            return
        }

        if (setCallThemeApNativeAd.value != null && !reload && requestSetCallThemeInProcess) {
            return
        }
        Log.d(TAG, "requestNativeSetCallTheme: $reload")
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            BuildConfig.native_set_call_theme,
            R.layout.layout_native_ad_small_full,
            object : VioAdmobCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    requestNativeSetCallTheme(activity, true)
                }

                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    Log.d(TAG, "loadNativeSetCallTheme loaded: ${nativeAd.admobNativeAd}")
                    setCallThemeApNativeAd.postValue(nativeAd)
                    requestSetCallThemeInProcess = false
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Log.d(TAG, "loadNativePermission: $adError")
                    setCallThemeApNativeAdLoadFail.postValue(true)
                    requestSetCallThemeInProcess = false
                }
            }
        )
    }

    fun requestNativePermission(
        activity: Activity,
        reload: Boolean = false
    ) {
        if (!isAdEnabled) {
            permissionApNativeAdLoadFail.postValue(true)
            return
        }

        if (permissionApNativeAd.value != null && !reload && requestPermissionInProcess) {
            return
        }

        Log.d(TAG, "loadNativePermission")
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
                    Log.d(TAG, "loadNativePermission loaded")
                    permissionApNativeAd.postValue(nativeAd)
                    requestPermissionInProcess = false
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Log.d(TAG, "loadNativePermission: $adError")
                    permissionApNativeAdLoadFail.postValue(true)
                    requestPermissionInProcess = false
                }
            }
        )

    }
    fun loadNativeOnboard(context: Context) {
        if (!isAdEnabled) return
        if (context.isInternetAvailable()) {
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
                                    R.layout.layout_native_ad_onboarding,
                                    nativeAd
                                )
                            )
                        }

                        1 -> {
                            nativeOnBoarding2.postValue(
                                ApNativeAd(
                                    R.layout.layout_native_ad_onboarding,
                                    nativeAd
                                )
                            )
                        }

                        2 -> {
                            nativeOnBoarding3.postValue(
                                ApNativeAd(
                                    R.layout.layout_native_ad_onboarding,
                                    nativeAd
                                )
                            )
                        }
                    }
                    countLoading++
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.d(TAG, "onboardLoaded: ${error.message}")
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
                        Tracking.logNativeAdImpression(
                            "show_native_onboarding_ad_success"
                        )
                    }
                })
                .withNativeAdOptions(adOptions)
                .build()
            adLoader?.loadAds(Admob.getInstance().adRequest, 3 - countLoading)
        } else {
            nativeOnBoardingFailLoad.postValue(true)
        }

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
                    Log.d(TAG, "backup ad was added to inventory")
                    backupOnboardingAds.add(ApNativeAd(
                        R.layout.layout_native_ad_onboarding,
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

    private fun interCustomizeReady(): Boolean {
        return _interCustomize != null && _interCustomize?.isReady == true && isAdEnabled
    }

    private fun interThemeReady(): Boolean {
        return _interTheme != null && _interTheme?.isReady == true && isAdEnabled
    }

    private fun interChooseAvatarReady(): Boolean {
        return _interAvatar != null && _interAvatar?.isReady == true && isAdEnabled
    }

    private fun interChooseBackgroundReady(): Boolean {
        return _interBackground != null && _interBackground?.isReady == true && isAdEnabled
    }

    private fun interChooseIconReady(): Boolean {
        return _interIcon != null && _interIcon?.isReady == true && isAdEnabled
    }

    private fun interPreviewReady(): Boolean {
        return _interPreview != null && _interPreview?.isReady == true && isAdEnabled
    }

    private fun interCategoryReady(): Boolean {
        return _interCategory != null && _interCategory?.isReady == true && isAdEnabled
    }

    fun loadInterCustomize(context: Context) {
        if (!isAdEnabled) return
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interCustomizeReady()
            && requestStateCustomize != RequestState.LOADING
        ) {
            Log.d(TAG, "loadInterCustomize: ")
            requestStateCustomize = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(
                    context,
                    BuildConfig.inter_customize,
                    object : VioAdmobCallback() {
                        override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                            super.onInterstitialLoad(interstitialAd)
                            _interCustomize = interstitialAd
                            requestStateCustomize = RequestState.COMPLETE
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            requestStateCustomize = RequestState.COMPLETE
                        }
                    })
        }
    }

    fun forceShowInterCustomize(context: Context, onNextAction: () -> Unit) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interCustomizeReady() && isAdEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interCustomize,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )

            (context as? MainActivity)?.let { mainActivity ->
                mainActivity.interSelectCategoryEnable = false
                mainActivity.restartCategoryTimer()
            }
        } else {
            onNextAction()
        }
    }

    fun loadInterTheme(context: Context) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interThemeReady()
            && requestStateTheme != RequestState.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterTheme: ")
            requestStateTheme = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(context, BuildConfig.inter_theme, object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interTheme = interstitialAd
                        requestStateTheme = RequestState.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStateTheme = RequestState.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterTheme(context: Context, onNextAction: () -> Unit) {
        Log.d(TAG, "forceShow: $_interTheme")
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interThemeReady() && isAdEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interTheme,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )

            (context as? MainActivity)?.let { mainActivity ->
                mainActivity.interSelectCategoryEnable = false
                mainActivity.restartCategoryTimer()
            }
        } else {
            onNextAction()
        }
    }

    fun loadInterChooseAvatar(context: Context) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interChooseAvatarReady()
            && requestStateAvatar != RequestState.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterChooseAvatar: ")
            requestStateAvatar = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(context, BuildConfig.inter_choose_avatar, object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interAvatar = interstitialAd
                        requestStateAvatar = RequestState.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStateAvatar = RequestState.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterChooseAvatar(context: Context, onNextAction: () -> Unit) {
        Log.d(TAG, "forceShow: $_interAvatar")
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interChooseAvatarReady() && isAdEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interAvatar,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )

            (context as? MainActivity)?.let { mainActivity ->
                mainActivity.interSelectCategoryEnable = false
                mainActivity.restartCategoryTimer()
            }
        } else {
            onNextAction()
        }
    }

    fun loadInterChooseBackground(context: Context) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interChooseBackgroundReady()
            && requestStateBackground != RequestState.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterChooseAvatar: ")
            requestStateBackground = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(context, BuildConfig.inter_choose_background, object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interBackground = interstitialAd
                        requestStateBackground = RequestState.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStateBackground = RequestState.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterChooseBackground(context: Context, onNextAction: () -> Unit) {
        Log.d(TAG, "forceShow: $_interBackground")
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interChooseBackgroundReady() && isAdEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interBackground,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )

            (context as? MainActivity)?.let { mainActivity ->
                mainActivity.interSelectCategoryEnable = false
                mainActivity.restartCategoryTimer()
            }
        } else {
            onNextAction()
        }
    }

    fun loadInterChooseIcon(context: Context) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interChooseIconReady()
            && requestStateIcon != RequestState.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterChooseAvatar: ")
            requestStateIcon = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(context, BuildConfig.inter_choose_icon, object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interIcon = interstitialAd
                        requestStateIcon = RequestState.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStateIcon = RequestState.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterChooseIcon(context: Context, onNextAction: () -> Unit) {
        Log.d(TAG, "forceShow: $_interIcon")
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interChooseIconReady() && isAdEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interIcon,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )

            (context as? MainActivity)?.let { mainActivity ->
                mainActivity.interSelectCategoryEnable = false
                mainActivity.restartCategoryTimer()
            }
        } else {
            onNextAction()
        }
    }

    fun loadInterPreview(context: Context) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interPreviewReady()
            && requestStatePreview != RequestState.LOADING
            && isAdEnabled
        ) {
            val unitId = if (isInterPreviewVideo) BuildConfig.inter_preview_video else BuildConfig.inter_preview
            Log.d(TAG, "loadInterPreview: $isInterPreviewVideo")
            requestStatePreview = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(context, unitId, object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interPreview = interstitialAd
                        requestStatePreview = RequestState.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStatePreview = RequestState.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterPreview(context: Context, onNextAction: () -> Unit) {
        Log.d(TAG, "forceShow: $_interPreview")
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interPreviewReady() && isAdEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interPreview,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )
        } else {
            onNextAction()
        }
    }

    fun loadInterCategory(context: Context) {
        if (!isAdEnabled) return
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !interCategoryReady()
            && requestStateCategory != RequestState.LOADING
        ) {
            Log.d(TAG, "loadInterCategory: ")
            requestStateCategory = RequestState.LOADING
            VioAdmob.getInstance()
                .getInterstitialAds(
                    context,
                    BuildConfig.inter_category,
                    object : VioAdmobCallback() {
                        override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                            super.onInterstitialLoad(interstitialAd)
                            _interCategory = interstitialAd
                            requestStateCategory = RequestState.COMPLETE
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            requestStateCategory = RequestState.COMPLETE
                        }
                    })
        }
    }

    fun forceShowInterCategory(context: Context, onNextAction: () -> Unit) {
        Log.d("InterCategory", "call")
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased && interCategoryReady() && isAdEnabled) {
            Log.d("InterCategory", "call on success")
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interCategory,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                true
            )
        } else {
            Log.d("InterCategory", "call on fail")
            onNextAction()
        }
    }


    fun showRewardedVideo(
        activity: FragmentActivity,
        onComplete: ((Boolean) -> Unit)? = null,
        onRewardedAd: ((Boolean) -> Unit)? = null
    ) {
        if (rewardedSetCallThemeVideoAds == null && !rewardSetCallLoadingState) {
            loadSetCallRewardedAd(activity)
            onComplete?.invoke(false)
            onRewardedAd?.invoke(true)
            return
        }

        if (rewardedSetCallThemeVideoAds == null || rewardSetCallLoadingState || !canShowAd(activity) || isRateLimitReached()) {
            onRewardedAd?.invoke(true)
            onComplete?.invoke(false)
            return
        }

        var onRewarded = false
        rewardedSetCallThemeVideoAds?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedSetCallThemeVideoAds?.fullScreenContentCallback = null
                    lastTimeClosePopup = System.currentTimeMillis()
                    onRewardedAd?.invoke(onRewarded)
                    onComplete?.invoke(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedSetCallThemeVideoAds?.fullScreenContentCallback = null
                    onComplete?.invoke(false)
                    onRewardedAd?.invoke(true)
                    Log.e("qvk", Throwable("onRewardAdFailedToShowFullScreenContent: ${adError.message}").toString())
                }

                override fun onAdShowedFullScreenContent() {
                    lastTimeAdsShown = System.currentTimeMillis()
                    rewardedSetCallThemeVideoAds = null
                    loadSetCallRewardedAd(activity)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadSetCallRewardedAd(activity)
                    AppOpenManager.getInstance().disableAppResume()
                }
            }

        rewardedSetCallThemeVideoAds?.let {

            it.show(activity) {
                AppOpenManager.getInstance().disableAppResume()
                onRewarded = true
//                onRewardedAd?.invoke(true)
            }
        } ?: run {
            onComplete?.invoke(false)
        }
    }

    fun loadSetCallRewardedAd(context: Context, reload: Boolean = false) {
        if ((!canShowAd(context) || rewardSetCallLoadingState || rewardedSetCallThemeVideoAds != null) && !reload) {
            return
        }
        Tracking.logEvent("load_reward_ads")

        rewardSetCallLoadingState = true
        RewardedAd.load(
            context,
            BuildConfig.rewarded_set_call_theme,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedSetCallThemeVideoAds = ad
                    rewardSetCallLoadingState = false
                    Log.d(this.javaClass.simpleName, "onAdLoaded: $ad")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedSetCallThemeVideoAds = null
                    rewardSetCallLoadingState = false
                    Log.e(this.javaClass.simpleName, Throwable("onAdFailedToLoad: ${loadAdError.message}").toString())
                }
            }
        )
    }

    private fun canShowAd(context: Context): Boolean {
        return !BillingClientProvider.getInstance(context as Activity).isPurchased && isAdEnabled
    }

    private fun isRateLimitReached(): Boolean =
        System.currentTimeMillis() - lastTimeAdsShown < AppRemoteConfig.offsetTimeShowInterAds * 1000
}

enum class RequestState {
    IDE, LOADING, COMPLETE
}