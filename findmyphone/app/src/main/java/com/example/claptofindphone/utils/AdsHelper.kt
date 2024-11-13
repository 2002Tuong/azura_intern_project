package com.example.claptofindphone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.example.claptofindphone.AdsUnitId
import com.example.claptofindphone.BuildConfig
import com.example.claptofindphone.R
import com.example.claptofindphone.ads.BannerAdsController
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions


class AdsHelper(
    private val preferenceSupplier: PreferenceSupplier,
    private val bannerAdsController: BannerAdsController,
    private val context: Context
) {
    private val TAG = AdsHelper::class.simpleName

    val languageApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val languageApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val onBoarding1ApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val onBoarding2ApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val onBoarding3ApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val onBoardingApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val permissionApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val permissionApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val homeApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val homeApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val selectApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val selectApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val resultApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val resultApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()
    private var apInterAdSelectSound: ApInterstitialAd? = null

    val bannerAdViewHowToUse: LiveData<AdView?> = bannerAdsController.bannerAdsHowToUse
    val bannerAdViewSelectSound: LiveData<AdView?> = bannerAdsController.bannerAdsSelectSound
    val bannerAdViewFindPhone: LiveData<AdView?> = bannerAdsController.bannerAdsFindPhone
    val bannerAdViewSetting: LiveData<AdView?> = bannerAdsController.bannerAdsSetting
    val bannerAdViewLangauge: LiveData<AdView?> = bannerAdsController.bannerAdsLanguage
    val bannerAdViewOnboard: LiveData<AdView?> = bannerAdsController.bannerAdsOnboard
    val bannerAdViewPermission: LiveData<AdView?> = bannerAdsController.bannerAdsPermission
    val bannerAdViewSoundActive: LiveData<AdView?> = bannerAdsController.bannerAdsSoundActive

    @SuppressLint("StaticFieldLeak")
    private var adLoaderOnBoarding: AdLoader? = null

    val isAdsSplashClosed: MutableLiveData<Boolean> = MutableLiveData()

    private var countOnBoardNativeLoading = 0
    var isAdsEnabled = false

    fun requestNativeHome(
        activity: Activity,
        reload: Boolean = false) {
        return
        if(stopLoadAds()) {
            homeApNativeAdLoadFail.postValue(true)
            return
        }

        if(homeApNativeAd.value != null && !reload) {
            return
        }

        Log.d(TAG, "loadNativeHome")
        homeApNativeAdLoadFail.postValue(false)
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            AdsUnitId.native_home,
            R.layout.layout_native_ad_home,
            object : VioAdmobCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    devShowToast("Home")
                    requestNativeHome(activity, true)
                    AppOpenManager.getInstance().disableAppResume()
                }

                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    homeApNativeAd.postValue(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    homeApNativeAdLoadFail.postValue(true)
                }
            }
        )
    }

    fun requestNativeLanguage(
        activity: Activity,
        reload: Boolean = false,
        ctaTop: Boolean = false) {
        return
        if(stopLoadAds()) {
            languageApNativeAdLoadFail.postValue(true)
            return
        }

        if(languageApNativeAd.value != null && !reload) {
            return
        }

        Log.d(TAG, "loadNativeLanguage")
        languageApNativeAdLoadFail.postValue(false)
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            AdsUnitId.native_language,
            if(ctaTop) R.layout.layout_native_ad_language_cta_top else R.layout.layout_native_ad_language,
            object : VioAdmobCallback() {
                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    languageApNativeAd.postValue(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    languageApNativeAdLoadFail.postValue(true)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    devShowToast("Language")
                    requestNativeLanguage(activity, true, ctaTop)
                    AppOpenManager.getInstance().disableAppResume()
                }
            }
        )
    }

    fun loadNativeOnBroad(
        context: Context,
        ctaTop: Boolean = false) {
        return
        if(stopLoadAds()) {
            onBoardingApNativeAdLoadFail.postValue(true)
            return
        }

        onBoardingApNativeAdLoadFail.postValue(false)
        Log.d(TAG, "loadNativeOnboard: ")
        countOnBoardNativeLoading = 0

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        adLoaderOnBoarding = AdLoader.Builder(context, AdsUnitId.native_onboarding)
            .forNativeAd { nativeAd ->
                when (countOnBoardNativeLoading) {
                    0 -> {
                        onBoarding1ApNativeAd.postValue(
                            ApNativeAd(
                                if(ctaTop) R.layout.layout_native_ad_onboarding_cta_top else R.layout.layout_native_ad_onboarding,
                                nativeAd
                            )
                        )
                    }

                    1 -> {
                        onBoarding2ApNativeAd.postValue(
                            ApNativeAd(
                                if(ctaTop) R.layout.layout_native_ad_onboarding_cta_top else R.layout.layout_native_ad_onboarding,
                                nativeAd
                            )
                        )
                    }

                    2 -> {
                        onBoarding3ApNativeAd.postValue(
                            ApNativeAd(
                                if(ctaTop) R.layout.layout_native_ad_onboarding_cta_top else R.layout.layout_native_ad_onboarding,
                                nativeAd
                            )
                        )
                    }
                }
                if (adLoaderOnBoarding?.isLoading == true) countOnBoardNativeLoading++
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    onBoardingApNativeAdLoadFail.postValue(true)
                }

                override fun onAdClicked() {
                    super.onAdClicked()

                    AppOpenManager.getInstance().disableAppResume()
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoaderOnBoarding?.loadAds(Admob.getInstance().adRequest, 3)
    }

    fun requestNativePermission(
        activity: Activity,
        reload: Boolean = false) {
        return
        if(stopLoadAds()) {
            permissionApNativeAdLoadFail.postValue(true)
            return
        }

        if(permissionApNativeAd.value != null && !reload) {
            return
        }

        Log.d(TAG, "loadNativePermission")
        permissionApNativeAdLoadFail.postValue(false)
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            AdsUnitId.native_permission,
            R.layout.layout_native_ad_permission,
            object : VioAdmobCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    devShowToast("Permission")
                    requestNativePermission(activity, true)
                }

                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    permissionApNativeAd.postValue(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    permissionApNativeAdLoadFail.postValue(true)
                }
            }
        )

    }

    fun requestNativeSelect(
        activity: Activity,
        reload: Boolean = true) {
        return
        if(stopLoadAds()) {
            selectApNativeAdLoadFail.postValue(true)
            return
        }

        if(selectApNativeAd.value != null && !reload) {
            return
        }

        Log.d(TAG, "loadNativeSelect")
        selectApNativeAdLoadFail.postValue(false)
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            AdsUnitId.native_select,
            R.layout.layout_native_ad_select,
            object : VioAdmobCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    devShowToast("SelectSound")
                    requestNativeSelect(activity, true)

                    AppOpenManager.getInstance().disableAppResume()
                }
                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    selectApNativeAd.postValue(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    selectApNativeAdLoadFail.postValue(true)
                }
            }
        )
    }

    fun requestNativeResult(
        activity: Activity,
        reload: Boolean = false) {
        return
        if(stopLoadAds()) {
            resultApNativeAdLoadFail.postValue(true)
            return
        }

        if(resultApNativeAd.value != null && !reload) {
            return
        }

        Log.d(TAG, "loadNativeResult")
        resultApNativeAdLoadFail.postValue(false)
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            AdsUnitId.native_result,
            R.layout.layout_native_ad_result,
            object: VioAdmobCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    devShowToast("Result")
                    requestNativeResult(activity, true)
                    AppOpenManager.getInstance().disableAppResume()
                }

                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    resultApNativeAd.postValue(nativeAd)
                    Log.d(TAG, "loadNativeResult success")
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    Log.d(TAG, "loadNativeResult fail")
                    resultApNativeAd.postValue(null)
                    resultApNativeAdLoadFail.postValue(true)
                }
            }
        )
    }

    fun loadInterSelectSound(context: Context, reload: Boolean = false) {

        if(stopLoadAds()) {
            return
        }

        if(isInterSelectSoundReady() && !reload) {
            return
        }

        if (BuildConfig.FLAVOR == "dev") {
            Toast.makeText(
                context,
                "inter select ads has been loaded",
                Toast.LENGTH_SHORT
            ).show()
        }

        Log.d(TAG, "loadInterSelectSoundAds")
        VioAdmob.getInstance().getInterstitialAds(
            context,
            AdsUnitId.inter_select_sound,
            object : VioAdmobCallback() {
                override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                    super.onInterstitialLoad(interstitialAd)
                    apInterAdSelectSound = interstitialAd
                }
            }
        )

    }

    fun forceShowInterSelectSound(context: Context, onNextAction: () -> Unit) {
        if (isInterSelectSoundReady() && !preferenceSupplier.isProUser && isAdsEnabled) {
            VioAdmob.getInstance().forceShowInterstitial(context, apInterAdSelectSound,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                }
            )
        } else {
            onNextAction()
        }
    }

    fun loadBannerHowToUse(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.HOW_TO_USE, reload)
    }

    fun loadBannerSelectSound(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.SELECT_SOUND, reload)
    }

    fun loadBannerFindPhone(reload: Boolean= false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.FIND_PHONE, reload)
    }

    fun loadBannerSetting(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.SETTING, reload)
    }

    fun loadBannerLanguage(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.LANGUAGE, reload)
    }

    fun loadBannerOnboard(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.ONBOARDING, reload)
    }

    fun loadBannerPermission(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.PERMISSION, reload)
    }

    fun loadBannerSoundActive(reload: Boolean = false) {
        if(stopLoadAds()) return
        bannerAdsController.loadBanner(BannerAdsController.BannerPlacement.ACTIVE_SOUND, reload)
    }

    private fun isInterSelectSoundReady() = apInterAdSelectSound != null && apInterAdSelectSound!!.isReady
    private fun stopLoadAds() = preferenceSupplier.isProUser || !isAdsEnabled || !NetworkUtils.isInternetAvailable()
    private fun devShowToast(index: String) {
        if (BuildConfig.FLAVOR == "dev") {
            Toast.makeText(
                context,
                "native ads $index has been loaded",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
