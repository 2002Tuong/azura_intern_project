package com.slideshowmaker.slideshow.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils

object AdsHelper {
    private val TAG = AdsHelper::class.simpleName
    val apNativeSaving: MutableLiveData<ApNativeAd> = MutableLiveData()
    val apNativeSavingLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val apNativeLanguage: MutableLiveData<ApNativeAd> = MutableLiveData()
    val apNativeLanguageLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val apNativeLanguageDup: MutableLiveData<ApNativeAd> = MutableLiveData()
    val apNativeLanguageDupLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val apNativePermission: MutableLiveData<ApNativeAd> = MutableLiveData()
    val apNativePermissionLoadFail: MutableLiveData<Boolean> = MutableLiveData()

//    val nativeOnboardingLiveData: MutableLiveData<ApNativeAd> = MutableLiveData()
    val apNativeOnBoarding1 = MutableLiveData<ApNativeAd?>()
    val apNativeOnBoarding2 = MutableLiveData<ApNativeAd?>()
    val apNativeOnBoarding3 = MutableLiveData<ApNativeAd?>()
    val apNativeOnBoardingLoadFail: MutableLiveData<Boolean> = MutableLiveData()
    val isAdsSplashClosed: MutableLiveData<Boolean> = MutableLiveData()
    private var _apInterAdCreateVideo: ApInterstitialAd? = null
    private var interCreateVideoStatus = RequestStatus.IDE
    private var countOnboardLoading = 0
    @SuppressLint("StaticFieldLeak")
    private var adLoader: AdLoader? = null

    var isAdEnabled = false

    private fun interCreateVideoReady(): Boolean {
        return _apInterAdCreateVideo != null && _apInterAdCreateVideo!!.isReady
    }

    private var _apInterCreateMyVideo: ApInterstitialAd? = null
    private var interCreateMyVideoStatus = RequestStatus.IDE

    private fun interCreateMyVideoReady(): Boolean {
        return _apInterCreateMyVideo != null && _apInterCreateMyVideo!!.isReady
    }

    private var _apInterSuccess: ApInterstitialAd? = null
    private var interSuccessStatus = RequestStatus.IDE

    private fun interSuccessReady(): Boolean {
        return _apInterSuccess != null && _apInterSuccess!!.isReady
    }

    private var _apInterTrimJoin: ApInterstitialAd? = null
    private var internJoinTrimStatus = RequestStatus.IDE

    private fun interJoinTrimReady() = _apInterTrimJoin != null && _apInterTrimJoin!!.isReady

    private var _apInterSaveJoinTrim: ApInterstitialAd? = null
    private var interSaveJoinTrimStatus = RequestStatus.IDE

    private fun interSaveJoinTrimReady() = _apInterSaveJoinTrim != null && _apInterSaveJoinTrim!!.isReady

    private var _apInterExport: ApInterstitialAd? = null
    private var interExportStatus = RequestStatus.IDE

    private fun interExportReady() = _apInterExport != null && _apInterExport!!.isReady

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
                    headline?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                    body?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                    adMedia?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                }
                onLoaded(nativeAd)
            }

            override fun onAdFailedToLoad(adError: ApAdError?) {
                super.onAdFailedToLoad(adError)
                onLoadFail()
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

    fun requestNativeHome(
        activity: Activity,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && isAdEnabled) {
            Log.d(TAG, "requestNativeHome: ")
            loadNative(
                activity = activity,
                adId = BuildConfig.native_home,
                layout = R.layout.layout_native_ad_home,
                onLoaded = onLoaded,
                onLoadFail = onLoadFail
            )
        } else {
            onLoadFail()
        }
    }

    fun requestNativeChooseImage(
        activity: Activity,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && isAdEnabled) {
            Log.d(TAG, "requestNativeChooseImage: ")
            loadNative(
                activity = activity,
                adId = BuildConfig.native_choose_image,
                layout = R.layout.layout_native_ad_choose_media,
                onLoaded = onLoaded,
                onLoadFail = onLoadFail
            )
        } else {
            onLoadFail()
        }
    }

    fun requestNativeArrangeImage(
        activity: Activity,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && isAdEnabled) {
            Log.d(TAG, "requestNativeArrangeImage: ")
            loadNative(
                activity = activity,
                adId = BuildConfig.native_arrange_image,
                layout = R.layout.layout_native_ad_choose_media,
                onLoaded = onLoaded,
                onLoadFail = onLoadFail
            )
        } else {
            onLoadFail()
        }
    }

    fun requestNativeSaving(
        activity: Activity
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && apNativeSaving.value == null && isAdEnabled) {
            Log.d(TAG, "requestNativeSaving: ")
            apNativeSavingLoadFail.postValue(false)
            loadNative(
                activity = activity,
                adId = BuildConfig.native_saving,
                layout = R.layout.layout_native_ad_saving,
                onLoaded = {
                    apNativeSaving.postValue(it)
                }, onLoadFail = {
                    apNativeSavingLoadFail.postValue(true)
                }
            )
        } else {
            apNativeSavingLoadFail.postValue(true)
        }
    }

    fun requestNativeSuccess(
        activity: Activity,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && isAdEnabled) {
            Log.d(TAG, "requestNativeSuccess: ")
            loadNative(
                activity = activity,
                adId = BuildConfig.native_success,
                layout = R.layout.layout_native_ad_success,
                onLoaded = onLoaded,
                onLoadFail = onLoadFail
            )
        } else {
            onLoadFail()
        }
    }

    fun requestNativeMyVideo(
        activity: Activity,
        onLoaded: (ApNativeAd) -> Unit,
        onLoadFail: () -> Unit
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && isAdEnabled) {
            Log.d(TAG, "requestNativeMyVideo: ")
            loadNative(
                activity = activity,
                adId = BuildConfig.native_my_video,
                layout = R.layout.layout_native_ad_success,
                onLoaded = onLoaded,
                onLoadFail = onLoadFail
            )
        } else {
            onLoadFail()
        }
    }

    fun requestNativePermission(
        activity: Activity
    ) {
        Log.d("AdsPermission", "call from ${activity::class.simpleName}")
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && apNativeLanguage.value == null && isAdEnabled) {
            val layoutId = R.layout.layout_native_ad_permission
            apNativePermissionLoadFail.postValue(false)
            loadNative(
                activity = activity,
                adId = BuildConfig.native_permission,
                layout = layoutId,
                onLoaded = { nativeAd ->
                    apNativePermission.postValue(nativeAd)

                }, onLoadFail = {
                    apNativePermissionLoadFail.postValue(true)
                }
            )
        } else {
            apNativePermissionLoadFail.postValue(true)
        }
    }

    fun requestNativeLanguage(
        activity: Activity
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && apNativeLanguage.value == null && isAdEnabled) {
            Log.d(TAG, "requestNativeLanguage: ")
            val ctaTop = RemoteConfigRepository.nativeAdsConfig?.isCtaTopLanguage ?: false
            val layoutId = if (ctaTop) {
                R.layout.layout_native_ad_language_top_cta
            } else {
                R.layout.layout_native_ad_language
            }
            apNativeLanguageLoadFail.postValue(false)
            loadNative(
                activity = activity,
                adId = BuildConfig.native_language,
                layout = layoutId,
                onLoaded = { nativeAd ->
                    apNativeLanguage.postValue(nativeAd)

                }, onLoadFail = {
                    apNativeLanguageLoadFail.postValue(true)
                }
            )
        } else {
            apNativeLanguageLoadFail.postValue(true)
        }
    }

    fun requestNativeLanguageDup(
        activity: Activity
    ) {
        if (!SharedPreferUtils.proUser && Utils.isInternetAvailable() && apNativeLanguageDup.value == null && isAdEnabled) {
            Log.d(TAG, "requestNativeLanguage: ")
            val ctaTop = RemoteConfigRepository.nativeAdsConfig?.isCtaTopLanguage ?: false
            val layoutId = if (ctaTop) {
                R.layout.layout_native_ad_language_top_cta
            } else {
                R.layout.layout_native_ad_language
            }
            apNativeLanguageDupLoadFail.postValue(false)
            loadNative(
                activity = activity,
                adId = BuildConfig.native_language_dup,
                layout = layoutId,
                onLoaded = { nativeAd ->
                    apNativeLanguageDup.postValue(nativeAd)

                }, onLoadFail = {
                    apNativeLanguageDupLoadFail.postValue(true)
                }
            )
        } else {
            apNativeLanguageDupLoadFail.postValue(true)
        }
    }


//    fun requestNativeOnBoarding(
//        activity: Activity
//    ) {
//        if (!SharedPrefUtils.isProUser && Utils.isInternetAvailable() && nativeOnboardingLiveData.value == null && isAdEnabled) {
//            Log.d(TAG, "requestNativeOnBoarding: ")
//            nativeSavingLoadFail.postValue(false)
//            VioAdmob.getInstance().loadNativeAdResultCallback(
//                activity,
//                BuildConfig.native_onboarding,
//                R.layout.layout_native_onboarding,
//                object : VioAdmobCallback() {
//                    override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
//                        super.onNativeAdLoaded(nativeAd)
//                        nativeOnboardingLiveData.postValue(nativeAd)
//                    }
//
//                    override fun onAdFailedToLoad(adError: ApAdError?) {
//                        super.onAdFailedToLoad(adError)
//                        nativeOnBoardingLoadFail.postValue(true)
//                    }
//                })
//        } else {
//            nativeOnBoardingLoadFail.postValue(true)
//        }
//    }

    fun loadNativeOnboard(
        activity: Activity
    ) {
        if (!isAdEnabled) return
        val ctaTop = RemoteConfigRepository.nativeAdsConfig?.isCtaTopOnboard ?: false
        val layoutId = if (ctaTop) {
            R.layout.layout_native_ad_onboarding_top_cta
        } else {
            R.layout.layout_native_ad_onboarding
        }
        if (Utils.isInternetAvailable()) {
            countOnboardLoading = 0
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()
            adLoader = AdLoader.Builder(activity, BuildConfig.native_onboarding)
                .forNativeAd { nativeAd ->
                    when (countOnboardLoading) {
                        0 -> {
                            Logger.d("$TAG: loadNativeOnboard: $countOnboardLoading")
                            apNativeOnBoarding1.postValue(
                                ApNativeAd(
                                    layoutId,
                                    nativeAd
                                )
                            )
                        }

                        1 -> {
                            Logger.d("$TAG: loadNativeOnboard: $countOnboardLoading")
                            apNativeOnBoarding2.postValue(
                                ApNativeAd(
                                    layoutId,
                                    nativeAd
                                )
                            )
                        }

                        2 -> {
                            Logger.d("$TAG: loadNativeOnboard: $countOnboardLoading")
                            apNativeOnBoarding3.postValue(
                                ApNativeAd(
                                    layoutId,
                                    nativeAd
                                )
                            )
                        }
                    }
                    if (adLoader?.isLoading == true) countOnboardLoading++
                }
                .withAdListener(object : AdListener() {
                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.d("Onboard", "Ad Impression: ${countOnboardLoading}")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        apNativeOnBoardingLoadFail.postValue(true)
                    }
                })
                .withNativeAdOptions(adOptions)
                .build()
            adLoader?.loadAds(Admob.getInstance().adRequest, 3)
        } else {
            apNativeOnBoardingLoadFail.postValue(true)
        }
    }

    fun loadInterCreate(context: Context) {
        if (!SharedPreferUtils.proUser
            && Utils.isInternetAvailable()
            && !interCreateVideoReady()
            && interCreateVideoStatus != RequestStatus.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterCreate: ")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_create_video,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _apInterAdCreateVideo = interstitialAd
                        interCreateVideoStatus = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        interCreateVideoStatus = RequestStatus.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterCreate(context: Context, onNextAction: () -> Unit) {
        if (interCreateVideoReady() && isAdEnabled && !SharedPreferUtils.proUser) {
            VioAdmob.getInstance()
                .forceShowInterstitial(context, _apInterAdCreateVideo, object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                })
        } else {
            onNextAction()
        }
    }


    fun loadInterMyVideo(context: Context) {
        if (!SharedPreferUtils.proUser
            && Utils.isInternetAvailable()
            && !interCreateMyVideoReady()
            && interCreateMyVideoStatus != RequestStatus.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterMyVideo: ")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_my_video,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _apInterCreateMyVideo = interstitialAd
                        interCreateMyVideoStatus = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        interCreateMyVideoStatus = RequestStatus.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterMyVideo(context: Context, onNextAction: () -> Unit) {
        if (interCreateMyVideoReady() && isAdEnabled && !SharedPreferUtils.proUser) {
            VioAdmob.getInstance()
                .forceShowInterstitial(context, _apInterCreateMyVideo, object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        _apInterCreateMyVideo = null
                        loadInterMyVideo(context)
                    }
                })
        } else {
            onNextAction()
        }
    }

    fun loadInterSuccess(context: Context) {
        return
        if (!SharedPreferUtils.proUser
            && Utils.isInternetAvailable()
            && !interSuccessReady()
            && interSuccessStatus != RequestStatus.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterSuccess: ")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_save,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _apInterSuccess = interstitialAd
                        interSuccessStatus = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        interSuccessStatus = RequestStatus.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterSuccess(context: Context, onNextAction: () -> Unit) {
        onNextAction()
        return
        if (interSuccessReady() && isAdEnabled) {
            Admob.getInstance().setOpenActivityAfterShowInterAds(false)
            showToast("Show interSave", context)
            VioAdmob.getInstance()
                .forceShowInterstitial(context, _apInterSuccess, object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        _apInterSuccess = null
                        loadInterSuccess(context)
                        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
                    }
                })
        } else {
            onNextAction()
        }
    }

    fun loadInterJoinTrim(context: Context) {
        return
        if (!SharedPreferUtils.proUser
            && Utils.isInternetAvailable()
            && !interJoinTrimReady()
            && internJoinTrimStatus != RequestStatus.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterJoinTrim: ")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_join_trim,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _apInterTrimJoin = interstitialAd
                        internJoinTrimStatus = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        internJoinTrimStatus = RequestStatus.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterJoinTrim(context: Context, onNextAction: () -> Unit) {
        onNextAction()
        return
        if (interJoinTrimReady() && isAdEnabled && !SharedPreferUtils.proUser) {
            showToast("show InterJoinTrim", context)
            VioAdmob.getInstance()
                .forceShowInterstitial(context, _apInterTrimJoin, object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        _apInterTrimJoin = null
                        loadInterJoinTrim(context)
                    }
                })
        } else {
            onNextAction()
        }
    }

    fun loadInterSaveJoinTrim(context: Context) {
        return
        if (!SharedPreferUtils.proUser
            && Utils.isInternetAvailable()
            && !interSaveJoinTrimReady()
            && interSaveJoinTrimStatus != RequestStatus.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterSaveJoinTrim: ")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_save_join_trim,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _apInterSaveJoinTrim = interstitialAd
                        interSaveJoinTrimStatus = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        interSaveJoinTrimStatus = RequestStatus.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterSaveJoinTrim(context: Context, onNextAction: () -> Unit) {
        onNextAction()
        return
        if (interSaveJoinTrimReady() && isAdEnabled) {
            //Admob.getInstance().setOpenActivityAfterShowInterAds(false)
            showToast("Show interSaveJoinTrim", context)
            VioAdmob.getInstance()
                .forceShowInterstitial(context, _apInterSaveJoinTrim, object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        _apInterSaveJoinTrim = null
                        loadInterSaveJoinTrim(context)
                        //Admob.getInstance().setOpenActivityAfterShowInterAds(true)
                    }
                })
        } else {
            onNextAction()
        }
    }


    fun loadInterExport(context: Context) {
        if (!SharedPreferUtils.proUser
            && Utils.isInternetAvailable()
            && !interExportReady()
            && interExportStatus != RequestStatus.LOADING
            && isAdEnabled
        ) {
            Log.d(TAG, "loadInterSuccess: ")
            VioAdmob.getInstance().getInterstitialAds(
                context,
                BuildConfig.inter_export_video,
                object : VioAdmobCallback() {
                    override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        _apInterExport = interstitialAd
                        interExportStatus = RequestStatus.COMPLETE
                    }

                    override fun onAdFailedToLoad(adError: ApAdError?) {
                        super.onAdFailedToLoad(adError)
                        interExportStatus = RequestStatus.COMPLETE
                    }
                })
        }
    }

    fun forceShowInterExport(context: Context, onNextAction: () -> Unit) {
        if (interExportReady() && isAdEnabled) {
            Admob.getInstance().setOpenActivityAfterShowInterAds(false)
            showToast("Show interExport", context)
            VioAdmob.getInstance()
                .forceShowInterstitial(context, _apInterExport, object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        _apInterExport = null
                        loadInterExport(context)
                        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
                    }
                })
        } else {
            onNextAction()
        }
    }

    private fun showToast(message: String, context: Context) {
        if(BuildConfig.is_debug) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

enum class RequestStatus {
    IDE, LOADING, COMPLETE
}