package com.example.videoart.batterychargeranimation.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.funtion.AdCallback
import com.example.videoart.batterychargeranimation.BuildConfig
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.extension.isInternetAvailable
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlin.properties.Delegates

object AdsUtils {
    private val TAG = AdsUtils::class.simpleName
    var adsEnable = false
    val isCloseAdSplash = MutableLiveData<Boolean>()
    private var _interSelect: ApInterstitialAd? = null
    private var _interBatteryInfo: ApInterstitialAd? = null
    val isCloseInterSelect = MutableLiveData<Boolean>(false)
    private var countClick = 0

    private var requestStateSelect = RequestStatus.IDE
    private var requestStateBatteryInfo = RequestStatus.IDE

    val nativeLanguage = MutableLiveData<ApNativeAd?>()
    val nativeLanguageLoadFail = MutableLiveData<Boolean>()

    val nativeOnboard1 = MutableLiveData<ApNativeAd?>()
    val nativeOnboard2 = MutableLiveData<ApNativeAd?>()
    val nativeOnboard3 = MutableLiveData<ApNativeAd?>()
    val nativeOnboardLoadFail = MutableLiveData<Boolean>()
    private var countOnboardLoading = 0
    private var adLoader: AdLoader? = null


    val nativePermission1 = MutableLiveData<ApNativeAd?>()
    val nativePermission2 = MutableLiveData<ApNativeAd?>()
    val nativePermission3 = MutableLiveData<ApNativeAd?>()
    val nativePermissionLoadFail = MutableLiveData<Boolean>()
    private var countPermissionLoading = 0
    private var adLoaderPermission: AdLoader? = null

    val nativePreview = MutableLiveData<ApNativeAd?>()
    val nativePreviewLoadFail = MutableLiveData<Boolean>()
    val nativePreviewLoading = MutableLiveData<Boolean>()

    val nativeHome = MutableLiveData<ApNativeAd?>()
    val nativeHomeLoadFail = MutableLiveData<Boolean>()

    val nativeResult = MutableLiveData<ApNativeAd?>()
    val nativeResultLoadFail = MutableLiveData<Boolean>()

    val bannerLiveData = MutableLiveData<AdView?>()
    val bannerFailToLoad = MutableLiveData<Boolean>()
    val bannerLoading = MutableLiveData<Boolean>()


    val nativeLanguageDup = MutableLiveData<ApNativeAd?>()
    val nativeLanguageDupLoadFail = MutableLiveData<Boolean>()

    val nativeBatteryInfo = MutableLiveData<ApNativeAd?>()
    val nativeBatteryInfoLoadFail = MutableLiveData<Boolean>()

    val nativeExit = MutableLiveData<ApNativeAd?>()
    val nativeExitLoadFail = MutableLiveData<Boolean>()

    val nativeCharging = MutableLiveData<ApNativeAd?>()
    val nativeChargingLoadFail = MutableLiveData<Boolean>()

    private fun interSelectReady(): Boolean = _interSelect != null && _interSelect!!.isReady && adsEnable
    private fun interBatteryInfoReady(): Boolean = _interBatteryInfo != null && _interBatteryInfo!!.isReady && adsEnable

    fun loadInterSelect (context: Context, reload: Boolean = false){
        if(!adsEnable || !context.isInternetAvailable()) return
        if(interSelectReady() && !reload) {
            return
        }
        isCloseInterSelect.postValue(false)
        if(requestStateSelect != RequestStatus.LOADING ) {
            Log.d("Ads", "Load inter select")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_select,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interSelect = interstitialAd
                        requestStateSelect = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStateSelect = RequestStatus.COMPLETE
                        isCloseInterSelect.postValue(true)
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        isCloseInterSelect.postValue(true)
                    }
                }
            )
        }
    }

    fun loadInterBatteryInfo(context: Context, reload: Boolean = false){
        if(!adsEnable || !context.isInternetAvailable()) return
        if(interBatteryInfoReady() && !reload) {
            return
        }

        if(requestStateBatteryInfo != RequestStatus.LOADING ) {
            Log.d("Ads", "Load inter battery info")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_battery_info,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _interBatteryInfo = interstitialAd
                        requestStateBatteryInfo = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        requestStateBatteryInfo = RequestStatus.COMPLETE

                    }
                }
            )
        }
    }


    fun forceShowInterSelect(context: Context, onNextAction: () -> Unit) {
        countClick ++
        if(interSelectReady() && adsEnable && countClick % 2 != 0) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interSelect,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        isCloseInterSelect.postValue(true)
                        Log.d("Preview", "onAdClosed")
                    }
                }
            )
        }else {
            onNextAction()
            isCloseInterSelect.postValue(true)
        }
    }

    fun forceShowInterBatteryInfo(context: Context, onNextAction: () -> Unit) {
        if(interBatteryInfoReady() && adsEnable) {
            VioAdmob.getInstance().forceShowInterstitial(
                context,
                _interBatteryInfo,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction.invoke()
                    }
                }
            )
        } else {
            onNextAction.invoke()
        }
    }

    private fun onNativeCallback(
        activity: Activity,
        adId: String,
        layout: Int,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit,
    ): VioAdmobCallback {
        return object : VioAdmobCallback() {
            override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                super.onNativeAdLoaded(nativeAd)
                val adView = LayoutInflater.from(activity).inflate(layout, null) as? NativeAdView
                if (nativeAd.admobNativeAd.responseInfo?.mediationAdapterClassName?.startsWith("com.google.ads.mediation.facebook") == true) {
                    val headline = adView?.findViewById<TextView>(R.id.ad_headline)
                    val body = adView?.findViewById<TextView>(R.id.ad_body)
                    val adMedia = adView?.findViewById<TextView>(R.id.ad_media)
                    val adStar = adView?.findViewById<RatingBar>(R.id.ad_stars)
                    adView?.starRatingView
                    headline?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                    body?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                    adMedia?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }

                    adStar?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                }
                onLoaded(nativeAd)
            }

            override fun onAdFailedToLoad(adError: ApAdError?) {
                super.onAdFailedToLoad(adError)
                onLoadFail()
            }

            override fun onAdImpression() {
                super.onAdImpression()

            }

            override fun onAdClicked() {
                super.onAdClicked()
                loadNative(
                    activity, adId, layout, onLoaded, onLoadFail
                ).also {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(activity, "reload Native Ads", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadNative(
        activity: Activity,
        adId: String,
        layout: Int,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            adId,
            layout,
            onNativeCallback(activity, adId, layout, onLoaded, onLoadFail)
        )
    }


    var stackNative by Delegates.observable(ArrayDeque<ApNativeAd>()) { _, old, new ->
        if (old.size == 0 && new.size != 0) {
            triggerRebind.postValue(true)
        }
    }
    val triggerRebind = MutableLiveData(false)

    fun requestNativeHomeList(context: Context, reload: Boolean = false) {
        if (!adsEnable) return
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
                            R.layout.layout_native_big,
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



    fun requestNativeLanguage(
        activity: Activity
    ) {
        if(activity.isInternetAvailable() && nativeLanguage.value == null && adsEnable) {
            nativeLanguageLoadFail.postValue(false)
            Log.d("AdsLanguage", "native language load")
            loadNative(
                activity,
                BuildConfig.native_language,
                R.layout.layout_native_big_lang,
                {
                    nativeLanguage.postValue(it)
                    Log.d("AdsLanguage", "load success")

                },
                {
                    Log.d("AdsLanguage", "nativeLoad fail")
                    nativeLanguageLoadFail.postValue(true)
                }
            )
        }else {
            nativeLanguageLoadFail.postValue(true)
        }
    }

    fun requestNativeLanguageDup(
        activity: Activity
    ) {
        if(activity.isInternetAvailable() && nativeLanguageDup.value == null && adsEnable) {
            nativeLanguageDupLoadFail.postValue(false)
            Log.d("AdsUtil", "native language load")
            loadNative(
                activity,
                BuildConfig.native_language_dup,
                R.layout.layout_native_big_lang,
                {
                    nativeLanguageDup.postValue(it)

                },
                {
                    Log.d("AdsUtil", "nativeLoad fail")
                    nativeLanguageDupLoadFail.postValue(true)
                }
            )
        }else {
            nativeLanguageDupLoadFail.postValue(true)
        }
    }

    fun requestNativePermission(
        activity: Activity,
        reload: Boolean = false
    ) {

        if(!activity.isInternetAvailable() || !adsEnable) {
            nativePermissionLoadFail.postValue(true)
            return
        }
        countPermissionLoading = 0
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        nativePermissionLoadFail.postValue(false)

        adLoaderPermission = AdLoader.Builder(activity, BuildConfig.native_permission)
            .forNativeAd {nativeAd ->
                when(countPermissionLoading) {
                    0 -> nativePermission1.postValue(ApNativeAd(R.layout.layout_native_big, nativeAd))
                    1 -> nativePermission2.postValue(ApNativeAd(R.layout.layout_native_big, nativeAd))
                    2 -> nativePermission3.postValue(ApNativeAd(R.layout.layout_native_big, nativeAd))
                }
                if(adLoaderPermission?.isLoading == true) countPermissionLoading ++
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    nativeOnboardLoadFail.postValue(true)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.d("PermissionFragment", "Ads Clicked")
                    requestNativePermission(activity)
                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        adLoaderPermission?.loadAds(Admob.getInstance().adRequest, 3)
    }

    fun requestNativePreview(
        activity: Activity,
        reload: Boolean = false
    ) {
        if(!activity.isInternetAvailable() || !adsEnable) {
            nativePreviewLoadFail.postValue(true)
            return
        }

        if(nativePreview.value != null && !reload) {
            return
        }

        if(nativePreviewLoading.value == true) {
            return
        }
        val oldNative = nativePreview.value

        Log.d("AdsPreview", "preview load ")
        nativePreviewLoadFail.postValue(false)
        nativePreviewLoading.postValue(true)
        loadNative(
            activity,
            BuildConfig.native_preview,
            R.layout.layout_native_small,
            {
                nativePreview.postValue(it)
                nativePreviewLoading.postValue(false)
                Log.d("AdsPreview", "preview load success")
            },
            {
                Log.d("AdsPreview", "preview load fail")
                nativePreviewLoading.postValue(false)
                if(reload) {
                    nativePreview.postValue(oldNative)
                }else {
                    nativePreviewLoadFail.postValue(true)
                }
            }
        )

    }

    fun requestNativeHome(
        activity: Activity,
        reload: Boolean = true
    ) {
        if(!activity.isInternetAvailable() || !adsEnable) {
            nativeHomeLoadFail.postValue(true)
            return
        }

        if(nativeHome.value != null && !reload) {
            return
        }

        nativeHomeLoadFail.postValue(false)
        loadNative(
            activity,
            BuildConfig.native_home,
            R.layout.layout_native_big,
            {
                nativeHome.postValue(it)
            },
            {
                nativeHomeLoadFail.postValue(true)
            }
        )
    }

    fun requestNativeBatteryInfo(
        activity: Activity,
        reload: Boolean = false
    ) {
        if(!activity.isInternetAvailable() || !adsEnable) {
            nativeBatteryInfoLoadFail.postValue(true)
            return
        }

        if(nativeBatteryInfo.value != null && !reload) {
            return
        }
        nativeBatteryInfoLoadFail.postValue(false)
        loadNative(
            activity,
            BuildConfig.native_battery_info,
            R.layout.layout_native_big,
            {
                nativeBatteryInfo.postValue(it)
            },
            {
                nativeBatteryInfoLoadFail.postValue(true)
            }
        )
    }

    fun requestNativeExit(
        activity: Activity,
        reload: Boolean = false
    ) {
        if(!activity.isInternetAvailable() || !adsEnable) {
            nativeExitLoadFail.postValue(true)
            return
        }

        if(nativeExit.value != null && !reload) {
            return
        }
        nativeExitLoadFail.postValue(false)
        loadNative(
            activity,
            BuildConfig.native_exit,
            R.layout.layout_native_big,
            {
                nativeExit.postValue(it)
            },
            {
                nativeExitLoadFail.postValue(true)
            }
        )
    }

    fun requestNativeCharging(
        activity: Activity,
        reload: Boolean = false
    ) {

        if(!activity.isInternetAvailable() || !adsEnable) {
            nativeChargingLoadFail.postValue(true)
            return
        }

        if(nativeCharging.value != null && !reload) {
            return
        }
        Log.d("AdsCharging", "Load")
        nativeChargingLoadFail.postValue(false)
        loadNative(
            activity,
            BuildConfig.native_charging,
            R.layout.layout_native_big,
            {
                nativeCharging.postValue(it)
                Log.d("AdsCharging", "LoadSuccess")
            },
            {
                nativeChargingLoadFail.postValue(true)
                Log.d("AdsCharging", "LoadFail")
            }
        )
    }


    fun loadNativeOnboard(
        activity: Activity
    ) {

        if(!adsEnable || !activity.isInternetAvailable()) {
            nativeOnboardLoadFail.postValue(true)
            return
        }
        Log.d("AdsOnboard", "onboarding call")
        countOnboardLoading = 0
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        adLoader = AdLoader.Builder(activity, BuildConfig.native_onboarding)
            .forNativeAd {nativeAd ->
                Log.d("AdsOnboard", "$countOnboardLoading load success")
                when(countOnboardLoading) {
                    0 -> nativeOnboard1.postValue(ApNativeAd(R.layout.layout_native_big, nativeAd))
                    1 -> nativeOnboard2.postValue(ApNativeAd(R.layout.layout_native_big, nativeAd))
                    2 -> nativeOnboard3.postValue(ApNativeAd(R.layout.layout_native_big, nativeAd))
                }
                if(adLoader?.isLoading == true) countOnboardLoading ++
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    nativeOnboardLoadFail.postValue(true)
                    Log.d("AdsOnboard", "$countOnboardLoading load fail")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader?.loadAds(Admob.getInstance().adRequest, 3)
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
                        R.layout.layout_native_big,
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

                })
                .withNativeAdOptions(adOptions)
                .build()
        backupOnboardingAdLoader?.loadAds(AdRequest.Builder().build(), numberOfAd)
    }

    fun requestLoadBanner(context: Activity, reload: Boolean = false) {
        if(!context.isInternetAvailable() || !adsEnable) {
            Log.d("AdsUtil", "fail because internet")
            bannerFailToLoad.postValue(true)
            return
        }
        if(bannerLiveData.value != null && !reload) {
            return
        }

        if(bannerLoading.value == true) {
            return
        }
        bannerLoading.postValue(true)
        bannerFailToLoad.postValue(false)
        Log.d("AdsBanner", "banner load call")
        Admob.getInstance().requestLoadBanner(
            context,
            BuildConfig.banner_main,
            object : AdCallback() {
                override fun onBannerLoaded(adView: AdView?) {
                    super.onBannerLoaded(adView)
                    Log.d("AdsBanner", "banner load success")
                    bannerLiveData.postValue(adView)
                    bannerLoading.postValue(false)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    //bannerLoading.postValue(true)
                }

                override fun onAdFailedToLoad(i: LoadAdError?) {
                    super.onAdFailedToLoad(i)
                    Log.d("AdsBanner", "fail because load failed")
                    i?.let {
                        Log.d("AdsBanner", it.message)
                    }
                    bannerFailToLoad.postValue(true)
                    bannerLoading.postValue(false)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    requestLoadBanner(context)
                }
            },
            false,
            Admob.BANNER_INLINE_LARGE_STYLE
        )
    }
}

enum class RequestStatus {
    IDE, LOADING, COMPLETE
}