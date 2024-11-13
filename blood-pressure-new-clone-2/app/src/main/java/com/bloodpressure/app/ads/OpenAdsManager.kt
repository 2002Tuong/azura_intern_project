package com.bloodpressure.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bloodpressure.app.MainActivity
import com.bloodpressure.app.R
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.Logger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import kotlin.coroutines.resume

class OpenAdsManager(
    private val context: Context,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig,
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private val openAppAdsScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentActivity: Activity? = null
    private var reminderActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingOpenAd = false
    private var isShowingOpenAd = false
    private var loadTime = 0L
    private var shownOpenAdCount = 0

    private var interAd: InterstitialAd? = null
    private var isLoadingInterAd = false
    private var isShowingInterAd = false

    suspend fun loadInterSplashAd(): Boolean = when {
        !canShowInterSplashAd() -> false
        else -> loadInterstitialAdAsync()
    }

    suspend fun showInterSplashAdIfAvailable(isSplash: Boolean = false): Boolean = when {
        !canShowInterSplashAd() -> false
        else -> showInterstitialAdIfAvailableAsync(isSplash)
    }

    private fun loadOpenAd() {
        openAppAdsScope.launch {
            remoteConfig.waitRemoteConfigLoaded()
            if (canShowOpenBetaAd()) {
                loadOpenAdAsync()
            }
        }
    }

    private suspend fun loadOpenAdAsync(refresh: Boolean = false): Boolean {
        return suspendCancellableCoroutine { continuation ->
            if (isLoadingOpenAd) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            if (isAvailable() && !refresh) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }
            isLoadingOpenAd = true
            AppOpenAd.load(context,
                context.getString(R.string.appopen_resume),
                AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingOpenAd = false
                        loadTime = Date().time
                        continuation.resume(true)
                        Logger.d("onAdLoaded: $appOpenAd")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingOpenAd = false
                        continuation.resume(false)
                        Logger.e(Throwable("onAdFailedToLoad: ${loadAdError.message}"))
                    }
                })
        }
    }

    private suspend fun showOpenAdIfAvailableAsync(): Boolean =
        suspendCancellableCoroutine { continuation ->
            if (isShowingOpenAd || currentActivity == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            if (!isAvailable()) {
                loadOpenAd()
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            appOpenAd?.setOnPaidEventListener {
                TrackingManager.logAdRevenue(
                    appOpenAd?.responseInfo?.mediationAdapterClassName.orEmpty(),
                    it.valueMicros / 1_000_000.0, emptyMap()
                )
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd?.fullScreenContentCallback = null
                    appOpenAd = null
                    isShowingOpenAd = false
                    loadOpenAd()
                    continuation.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd?.fullScreenContentCallback = null
                    appOpenAd = null
                    isShowingOpenAd = false
                    continuation.resume(false)
                    loadOpenAd()
                    Logger.e(Throwable("onAdFailedToShowFullScreenContent: ${adError.message}"))
                }

                override fun onAdShowedFullScreenContent() = Unit
            }
            currentActivity?.let {
                isShowingOpenAd = true
                appOpenAd?.show(it)
            }
        }

    private fun isAvailable(): Boolean {
        return appOpenAd != null && !isAdExpired()
    }

    private fun isAdExpired(): Boolean {
        return Date().time - loadTime >= 3600000 * 4
    }

    private suspend fun loadInterstitialAdAsync(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            if (isLoadingInterAd) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            if (interAd != null) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }
            isLoadingInterAd = true
            InterstitialAd.load(context,
                context.getString(R.string.inter_splash),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        interAd = interstitialAd
                        isLoadingInterAd = false
                        continuation.resume(true)
                        Logger.d("loadInterstitialAdAsync: onAdLoaded: $interAd")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingInterAd = false
                        continuation.resume(false)
                        Logger.e(
                            Throwable("loadInterstitialAdAsync: onAdFailedToLoad: ${loadAdError.message}")
                        )
                    }
                })
        }
    }

    private var isInterstitialAdShown = false
    private suspend fun showInterstitialAdIfAvailableAsync(isSplash: Boolean): Boolean =
        suspendCancellableCoroutine { continuation ->

            if (isShowingInterAd || currentActivity == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            if (interAd == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            interAd?.setOnPaidEventListener {
                TrackingManager.logAdRevenue(
                    interAd?.responseInfo?.mediationAdapterClassName.orEmpty(),
                    it.valueMicros / 1_000_000.0, emptyMap()
                )
            }

            interAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isInterstitialAdShown = true
                    interAd?.fullScreenContentCallback = null
                    interAd = null
                    isShowingInterAd = false
                    continuation.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    TrackingManager.logEvent(
                        "interstitial_ad_failed_to_show",
                        bundleOf("ad_id" to interAd?.adUnitId)
                    )
                    interAd?.fullScreenContentCallback = null
                    isShowingInterAd = false
                    continuation.resume(false)
                    isInterstitialAdShown = false
                    Logger.e(Throwable("onAdFailedToShowFullScreenContent: ${adError.message}"))
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    TrackingManager.logEvent(
                        "interstitial_ad_show_success",
                        bundleOf("ad_id" to interAd?.adUnitId)
                    )
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    TrackingManager.logEvent(
                        "interstitial_ad_clicked",
                        bundleOf("ad_id" to interAd?.adUnitId)
                    )
                }
            }
            currentActivity?.let { activity ->
                isShowingInterAd = true
                interAd?.let { interAds ->
                    if (!isSplash) {
                        val intent = Intent(activity, LoadingAdsDialog::class.java)
                        val componentActivity = activity as? MainActivity
                        componentActivity?.let {
                            it.updateActivityResultAction { interAds.show(activity) }
                            it.getResultLauncher()?.launch(intent)
                        }
                    } else {
                        interAds.show(activity)
                    }

                }
            }
        }

    private fun canShowOpenBetaAd(): Boolean =
        !dataStore.isPurchased && !remoteConfig.offAllAds() && remoteConfig.adsConfig.shouldShowOpenBetaAd && reminderActivity == null

    private fun canShowInterSplashAd(): Boolean = !dataStore.isPurchased &&
            !remoteConfig.offAllAds() &&
            remoteConfig.adsConfig.shouldShowInterSplashAd
            && remoteConfig.adsConfig.shouldShowInterAd
            && reminderActivity == null

    override fun onStart(owner: LifecycleOwner) {
        if (shownOpenAdCount == 0) {
            shownOpenAdCount++
            loadOpenAd()
            return
        }
        if (!isInterstitialAdShown) {
            openAppAdsScope.launch {
                showInterSplashAdIfAvailable()
            }
            return
        }
        if (canShowOpenBetaAd()) {
            openAppAdsScope.launch {
                showOpenAdIfAvailableAsync()
            }
            shownOpenAdCount++
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        clear()
    }

    fun clear() {
        appOpenAd = null
        interAd = null
        shownOpenAdCount = 0
    }

    var isActivityInForeground = false
        private set

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        Logger.d("ActivityState :onActivityCreated ${p0.componentName}")
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.d("ActivityState :onActivityStarted ${activity.componentName}")
        if (activity is MainActivity)
            currentActivity = activity
        else
            reminderActivity = activity
    }

    override fun onActivityResumed(p0: Activity) {
        Logger.d("ActivityState :onActivityResumed ${p0.componentName}")
        isActivityInForeground = true
    }

    override fun onActivityPaused(p0: Activity) {
        Logger.d("ActivityState :onActivityPaused ${p0.componentName}")
    }

    override fun onActivityStopped(p0: Activity) {
        Logger.d("ActivityState :onActivityStopped ${p0.componentName}")
        isActivityInForeground = false
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        Logger.d("ActivityState :onActivitySaveInstanceState ${p0.componentName}")
    }

    override fun onActivityDestroyed(p0: Activity) {
        Logger.d("ActivityState :onActivityDestroyed ${p0.componentName}")
        if (p0 is MainActivity)
            currentActivity = null
        else
            reminderActivity = null
    }
}
