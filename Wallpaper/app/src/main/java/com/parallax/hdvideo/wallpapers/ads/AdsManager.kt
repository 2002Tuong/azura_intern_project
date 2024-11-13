package com.parallax.hdvideo.wallpapers.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.services.log.AdEvent
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.lang.ref.WeakReference

object AdsManager {

    val KEY_NATIVE_AD: String
    val KEY_NATIVE_AD_EXIT_DIALOG: String
    private val KEY_BANNER_AD: String
    private var KEY_INTER_WALLPAPER: String
    val KEY_OPEN_AD: String
    private val KEY_REWARDED_AD: String
    val KEY_NATIVE_ONBOARD: String
    val KEY_NATIVE_LANGUAGE: String
    val KEY_INTER_SPLASH: String
    val KEY_BANNER_MAIN: String

    //check file 11 Events - HD wallpapers for details
    const val SUCCESS = "Success"
    const val FAIL = "Fail"
    const val SHOW_SUCCESS = "showSuccess"
    //show fail when load success
    const val LOAD_SUCCESS_SHOW_FAIL = "LoadSuccessShowFail"
    //show fail caused by load fail
    const val SHOW_FAIL_02 = "showFailWhenLoadFail"
    const val INTER_AD = "inter"
    const val NATIVE_AD = "native"
    const val OPEN_AD = "openAd"
    const val REWARD_AD = "rewarded"
    const val BANNER_AD = "banner"
    const val ERROR_CODE = "errorcode"
    const val AD_TYPE = "adType"
    const val STATE = "state"
    const val SHOW_STATE = "showState"

    private var interstitialAd: InterstitialAd? = null
    private var rewardAd: RewardedAd? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentAdmobTag = ""
    private var lastTimeShowedInterstitialOrRewardedAd = 0L
    private var activity: WeakReference<Activity>? = null
    private var onLoadingInterstitialAd = false
    private var onLoadingRewardAd = false
    private var loadInterAdCount = 0
    private var loadRewardedAdCount = 0
    private var hasLoadedRewardedAdFirst = false
    private var hasInitOpenAd = false
    private val exitDialogNativeAdLiveData = MutableLiveData<NativeAd?>()
    private var nativeAdDisposable: Disposable? = null
    private var allowedReload = false
    private var isGetReward = false
    var isVipUser = false

    val languageApNativeAd: MutableLiveData<ApNativeAd> = MutableLiveData()
    val languageApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()

    val onBoarding1ApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val onBoarding2ApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val onBoarding3ApNativeAd: MutableLiveData<ApNativeAd?> = MutableLiveData()
    val onBoardingApNativeAdLoadFail: MutableLiveData<Boolean> = MutableLiveData()
    private var countOnBoardNativeLoading = 0
    private var adLoaderOnBoarding: AdLoader? = null


    init {
        if (BuildConfig.DEBUG) {
            KEY_NATIVE_AD = ""
            KEY_NATIVE_AD_EXIT_DIALOG = ""
            KEY_BANNER_AD = "ca-app-pub-3940256099942544/6300978111"

            KEY_REWARDED_AD = "ca-app-pub-3940256099942544/5224354917"
            KEY_OPEN_AD = "ca-app-pub-3940256099942544/3419835294"
            KEY_NATIVE_ONBOARD = "ca-app-pub-3940256099942544/2247696110"
            KEY_NATIVE_LANGUAGE = "ca-app-pub-3940256099942544/2247696110"
            KEY_INTER_SPLASH = "ca-app-pub-3940256099942544/1033173712"
            KEY_BANNER_MAIN = "ca-app-pub-3940256099942544/6300978111"
            KEY_INTER_WALLPAPER = "ca-app-pub-3940256099942544/1033173712"
        } else {
            KEY_NATIVE_AD = WallpaperApp.instance.getString(R.string.admod_native_id)
            KEY_NATIVE_AD_EXIT_DIALOG = WallpaperApp.instance.getString(R.string.admod_detail_native_id)
            KEY_BANNER_AD = WallpaperApp.instance.getString(R.string.admod_banner_unit_id)
            KEY_REWARDED_AD = WallpaperApp.instance.getString(R.string.admod_rewarded_video_unit_id)
            KEY_OPEN_AD = WallpaperApp.instance.getString(R.string.admod_open)
            KEY_NATIVE_ONBOARD = "ca-app-pub-2057867305416948/1254965240"
            KEY_NATIVE_LANGUAGE = "ca-app-pub-2057867305416948/6235791197"
            KEY_INTER_SPLASH = "ca-app-pub-2057867305416948/3881128581"
            KEY_BANNER_MAIN = "ca-app-pub-2057867305416948/6315720234"
            KEY_INTER_WALLPAPER = "ca-app-pub-2057867305416948/3274570028"
        }
    }


    fun setActivity(activity : Activity) {
        this.activity = WeakReference(activity)
    }

    fun buildAdRequest(): AdRequest {
        val extrasBundle = Bundle()
        if (!RemoteConfig.commonData.isActiveServer) {
            extrasBundle.putString("max_ad_content_rating", "PG")
        }
        return AdRequest.Builder()
            .addNetworkExtrasBundle(
                AdMobAdapter::class.java,
                extrasBundle
            )
            //.addTestDevice("2A3CD06E42E19CD8E68FE4C38E08302E")
            //.addTestDevice("F9B599244354AF6F1E0269B3332C8A07")
            .build()
    }

    //region Native ad

    fun loadNativeAd(adKey: String) : Single<NativeAd>? {
        if (isVipUser) return null
        return Single.create<NativeAd> { emitter ->
            val AdLoaderBuilder = AdLoader.Builder(WallpaperApp.instance, adKey)
            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val nativeAdOptions = com.google.android.gms.ads.nativead.NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

            AdLoaderBuilder.forNativeAd { unifiedNativeAd ->
                if (!emitter.isDisposed) {
                    emitter.onSuccess(unifiedNativeAd)
                }
            }.withNativeAdOptions(nativeAdOptions)

            val adLoader = AdLoaderBuilder.withAdListener(object : AdListener() {

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.LoadNativeAdFailFinal)
                    Logger.d("loadNativeAd load native failed: $p0")
                    if (!emitter.isDisposed) {
                        emitter.onError(Exception())
                    }
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.LoadNativeAdSuccess)
                    Logger.d("loadNativeAd onAdLoaded")
                }
            }).build()
            adLoader.loadAd(buildAdRequest())
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }
    //endregion

    //region Banner ad

    fun loadBanner(view: FrameLayout, retryCount: Int = 0) : AdView? {
        val  context = view.context ?: return null
        try {
            view.removeAllViews()
            val adView = AdView(context)
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    view.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (retryCount > 0) {
                        loadBanner(view, retryCount - 1)
                    } else {
                        view.visibility = View.GONE
                    }
                }
            }
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM
            view.addView(adView, params)
            adView.adUnitId = KEY_BANNER_AD
            //adView.adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(App.instance, (AppConfig.widthScreen / AppConfig.displayMetrics.density).toInt())
            adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(WallpaperApp.instance, (AppConfiguration.widthScreenValue / AppConfiguration.displayMetrics.density).toInt()))
            adView.loadAd(buildAdRequest())
            return adView
        }catch (e: IOException) {}
        return null
    }
    //endregion

    //region Interstitial ad

    @Synchronized
    fun loadInterstitialAd(reload: Boolean = false) {
        if (!RemoteConfig.commonData.supportInter) return
        if ((interstitialAd == null && !onLoadingInterstitialAd) || reload) {
            onLoadingInterstitialAd = true
            loadInterAdCount++
            InterstitialAd.load(WallpaperApp.instance, KEY_INTER_WALLPAPER, buildAdRequest(), object :
                InterstitialAdLoadCallback() {

                override fun onAdLoaded(p0: InterstitialAd) {
                    interstitialAd = p0
                    onLoadingInterstitialAd = false
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.LoadInterstitialAdSuccess)
                    if (!hasLoadedRewardedAdFirst) {
                        hasLoadedRewardedAdFirst = true
                        loadRewardedAd()
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    onLoadingInterstitialAd = false
                    if (loadInterAdCount < 3 && !hasLoadedRewardedAdFirst) {
                        reloadInterstitialAd()
                    } else {
                        TrackingSupport.recordEventOnlyFirebase(AdEvent.LoadInterstitialAdFailFinal)
                        if (!hasLoadedRewardedAdFirst) {
                            hasLoadedRewardedAdFirst = true
                            loadRewardedAd()
                        } else {
                            reloadAd()
                        }
                    }
                }
            })
        }
    }

    private fun reloadInterstitialAd() {
        onLoadingInterstitialAd = false
        interstitialAd = null
        loadInterstitialAd()
    }

    @Synchronized
    fun showInterstitialAd(isForced: Boolean = false, tag: String) : Boolean {
        if (isVipUser) return false
        val curActivity = activity?.get() ?: return false
        if (!RemoteConfig.commonData.supportInter) return false
        val canShowAd = if (canShowInter || isForced) {
            if (interstitialAd == null) {
                TrackingSupport.recordEventOnlyFirebase(AdEvent.InterFailToShow)
                false
            } else {
                interstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        TrackingSupport.recordEventOnlyFirebase(AdEvent.InterLoadSuccessShowFail)
                        AppOpenAdManager.switchOnOff(true)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        lastTimeShowedInterstitialOrRewardedAd = System.currentTimeMillis()
                        AppOpenAdManager.switchOnOff(true)
                        //RxBus.push(AdWrapper(state = 1, tag = currentAdmobTag), 200)
                    }

                    override fun onAdShowedFullScreenContent() {
                        TrackingSupport.recordEventOnlyFirebase(AdEvent.InterstitialAdShowed)
                        AppOpenAdManager.switchOnOff(false)
                    }

                }
                interstitialAd!!.setOnPaidEventListener {
                    Logger.d("setOnPaidEventListener")
                }
                currentAdmobTag = tag
                interstitialAd!!.show(curActivity)
                allowedReload = true
                interstitialAd = null
                true
            }
        } else false
        reloadAd(delay = 200, isForced = true)
        return canShowAd
    }

    val canShowInter: Boolean get() = _canShowInter && interstitialAd != null

    private val _canShowInter: Boolean get() {
        if (isVipUser) return false
        var loadTimeInMillis = if (RemoteConfig.commonData.waitingShowInter == 0L) 30L else RemoteConfig.commonData.waitingShowInter // default 1 minute
        loadTimeInMillis *= 1000
        if (loadTimeInMillis < 0) return false
        val delta = System.currentTimeMillis() - lastTimeShowedInterstitialOrRewardedAd
        return delta > loadTimeInMillis || delta <= 0
    }

    //endregion

    //region Rewarded ad

    @Synchronized
    fun loadRewardedAd() {
        if (!RemoteConfig.commonData.supportReward) return
        if (onLoadingRewardAd || canShowRewardAd) return
        onLoadingRewardAd = true
        loadRewardedAdCount++
        RewardedAd.load(
            WallpaperApp.instance,
            KEY_REWARDED_AD,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    rewardAd = p0
                    onLoadingRewardAd = false
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.LoadRewardedAdSuccess)
                    if (!hasInitOpenAd) {
                        hasInitOpenAd = true
                        loadNativeAdOnExitDialog()
                        AppOpenAdManager.start()
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    onLoadingRewardAd = false
                    rewardAd = null
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.LoadRewardedAdFailFinal)
                    if (loadRewardedAdCount > 2) {
                        reloadAd()
                    } else {
                        if (!hasInitOpenAd) {
                            hasInitOpenAd = true
                            loadNativeAdOnExitDialog()
                            AppOpenAdManager.start()
                        }
                        loadRewardedAd()
                    }
                }
            })
    }

    val canShowRewardAd get() = rewardAd != null

    fun showRewardedAd(tag: String) : Boolean {
        if (isVipUser) return false
        if (!RemoteConfig.commonData.supportReward) return false
        val curActivity = activity?.get() ?: return false
        val canShowAd = if (canShowRewardAd) {
            isGetReward = false
            rewardAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    Logger.d("RewardedAd was dismissed.")
                    AppOpenAdManager.switchOnOff(true)
                    if (isGetReward) lastTimeShowedInterstitialOrRewardedAd = System.currentTimeMillis()
                    RxBus.push(AdWrapper(type = 1, state = 0, tag = currentAdmobTag), 200)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Logger.d("RewardedAd failed to show")
                    AppOpenAdManager.switchOnOff(false)
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.RewardedLoadSuccessShowFail)
                }

                override fun onAdShowedFullScreenContent() {
                    AppOpenAdManager.switchOnOff(false)
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.RewardedAdShowed)
                    Logger.d("RewardedAd showed fullscreen content.")
                    rewardAd = null
                }
            }
            currentAdmobTag = tag
            rewardAd!!.show(curActivity) {
                isGetReward = true
                RxBus.push(AdWrapper(type = 1, state = 1, tag = currentAdmobTag))
                TrackingSupport.recordEvent(AdEvent.UserEarnedReward)
            }
            rewardAd = null
            allowedReload = true
            true
        } else {
            TrackingSupport.recordEventOnlyFirebase(AdEvent.RewardedFailToShow)
            false
        }
        reloadAd(delay = 200, isForced = true)
        return canShowAd
    }
    //endregion

    @Synchronized
    fun reloadAd(delay: Long = 10_000, isForced: Boolean = false) {
        if (hasInitOpenAd &&
            (interstitialAd == null || rewardAd == null)
            && (allowedReload || isForced)) {
            if (!isForced) allowedReload = false
            handler.removeCallbacks(adsRunnable)
            handler.postDelayed(adsRunnable, delay)
        }
    }

    private val adsRunnable = Runnable {
        if (NetworkUtils.isNetworkConnected()) {
            loadRewardedAd()
            loadInterstitialAd()
        }
    }

    fun loadNativeAdOnExitDialog(adKey: String = KEY_NATIVE_AD_EXIT_DIALOG) : LiveData<NativeAd?> {
        if (!isVipUser && RemoteConfig.commonData.supportNative && exitDialogNativeAdLiveData.value == null
            && nativeAdDisposable == null) {
            nativeAdDisposable = loadNativeAd(adKey = adKey)?.subscribe({
                exitDialogNativeAdLiveData.value = it
            }, {
                nativeAdDisposable = null
            })
        }
        return exitDialogNativeAdLiveData
    }

    fun requestNativeLanguage(
        activity: Activity,
        reload: Boolean = false,
) {
        if(stopLoadAds()) {
            languageApNativeAdLoadFail.postValue(true)
            return
        }

        if(languageApNativeAd.value != null && !reload) {
            return
        }
        Log.d("AdsNative", "Load language")
        languageApNativeAdLoadFail.postValue(false)
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            KEY_NATIVE_LANGUAGE,
            R.layout.layout_native_ad_language,
            object : VioAdmobCallback() {
                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    languageApNativeAd.postValue(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    languageApNativeAdLoadFail.postValue(true)
                    Log.d("AdsNative", "Load fail")
                }

                override fun onAdClicked() {
                    super.onAdClicked()

                    requestNativeLanguage(activity, true)
                    AppOpenManager.getInstance().disableAppResume()
                }
            }
        )
    }

    fun loadNativeOnBroad(
        context: Context) {
        if(stopLoadAds()) {
            onBoardingApNativeAdLoadFail.postValue(true)
            return
        }

        onBoardingApNativeAdLoadFail.postValue(false)
        countOnBoardNativeLoading = 0

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        adLoaderOnBoarding = AdLoader.Builder(context, KEY_NATIVE_ONBOARD)
            .forNativeAd { nativeAd ->
                when (countOnBoardNativeLoading) {
                    0 -> {
                        onBoarding1ApNativeAd.postValue(
                            ApNativeAd(
                                R.layout.layout_native_ad_language,
                                nativeAd
                            )
                        )
                    }

                    1 -> {
                        onBoarding2ApNativeAd.postValue(
                            ApNativeAd(
                                R.layout.layout_native_ad_language,
                                nativeAd
                            )
                        )
                    }

                    2 -> {
                        onBoarding3ApNativeAd.postValue(
                            ApNativeAd(
                                R.layout.layout_native_ad_language,
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

    fun loadInterSplash(activity: AppCompatActivity, onNextAction: () -> Unit = {}) {

        if(stopLoadAds()) {
            onNextAction.invoke()
            return
        }
        VioAdmob.getInstance().loadSplashInterstitialAds(
            activity,
            KEY_INTER_SPLASH,
            10000L,
            3000L,
            true,
            object : VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    onNextAction()
                    Log.d("Splash", "onNextAction call")
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    onNextAction()
                    Log.d("Splash", "onAdFailedToLoad call")
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    onNextAction()
                    Log.d("Splash", "onAdClosed call")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d("Splash", "onAdLoaded call")
                }

                override fun onAdFailedToShow(adError: ApAdError?) {
                    super.onAdFailedToShow(adError)
                    Log.d("Splash", "onAdFailedToShow call")
                    onNextAction()
                }
            })
    }

    fun checkShowInterSplashWhenFail(activity: AppCompatActivity, onNextAction: () -> Unit = {}) {
        VioAdmob.getInstance()
            .onCheckShowSplashWhenFail(
                activity,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        onNextAction()
                    }
                },
                1000
            )
    }

    fun loadBanner(activity: Activity, onLoadFail: () -> Unit = {}) {
        if(stopLoadAds()) {
            onLoadFail()
            return
        }
        VioAdmob.getInstance().loadBanner(activity, KEY_BANNER_MAIN)
    }

    fun stopLoadAds(): Boolean = isVipUser || !NetworkUtils.isNetworkConnected()

    fun destroyNativeAdOnExitDialog() {
        exitDialogNativeAdLiveData.value?.destroy()
        exitDialogNativeAdLiveData.value = null
        nativeAdDisposable?.dispose()
        nativeAdDisposable = null
    }

    fun destroyAll() {
        isGetReward = false
        destroyNativeAdOnExitDialog()
        interstitialAd = null
        handler.removeCallbacksAndMessages(null)
        currentAdmobTag = ""
        rewardAd = null
        activity?.clear()
        activity = null
        onLoadingRewardAd = false
        onLoadingInterstitialAd = false
        lastTimeShowedInterstitialOrRewardedAd = 0
        hasInitOpenAd = false
        hasLoadedRewardedAdFirst = false
        loadInterAdCount = 0
        loadRewardedAdCount = 0
        allowedReload = false
    }
}