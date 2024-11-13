package com.artgen.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.artgen.app.R
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.log.Logger
import com.artgen.app.tracking.TrackingManager
import com.artgen.app.ui.MainActivity
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
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingOpenAd = false
    private var isShowingOpenAd = false
    private var loadTime = 0L
    private var shownOpenAdCount = 0

    private var interAd: InterstitialAd? = null
    private var isLoadingInterAd = false
    private var isShowingInterAd = false
    private var shouldShowAppOpenAds = false

    fun setShouldShowAppOpenAds(value: Boolean) {
        shouldShowAppOpenAds = value
    }

    suspend fun loadInterSplashAd(): Boolean = when {
        !canShowInterSplashAd() -> false
        else -> loadInterstitialAdAsync()
    }

    suspend fun showInterSplashAdIfAvailable(): Boolean = when {
        !canShowInterSplashAd() -> false
        else -> showInterstitialAdIfAvailableAsync()
    }

    private fun loadOpenAd() {
        if (canShowOpenBetaAd()) {
            openAppAdsScope.launch {
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
                context.getString(R.string.app_open_ad),
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
            TrackingManager.logLoadInterstitialAd()
            InterstitialAd.load(context,
                context.getString(R.string.inter_splash),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        interAd = interstitialAd
                        isLoadingInterAd = false
                        continuation.resume(true)
                        Logger.d("loadInterstitialAdAsync: onAdLoaded: $interAd")
                        TrackingManager.logInterstitialAdLoaded()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingInterAd = false
                        continuation.resume(false)
                        TrackingManager.logInterstitialAdFailed(loadAdError)
                        Logger.e(
                            Throwable("loadInterstitialAdAsync: onAdFailedToLoad: ${loadAdError.message}")
                        )
                    }
                })
        }
    }

    private suspend fun showInterstitialAdIfAvailableAsync(): Boolean =
        suspendCancellableCoroutine { continuation ->
            if (isShowingInterAd || currentActivity == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            if (interAd == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            interAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interAd?.fullScreenContentCallback = null
                    interAd = null
                    isShowingInterAd = false
                    continuation.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interAd?.fullScreenContentCallback = null
                    interAd = null
                    isShowingInterAd = false
                    continuation.resume(false)
                    Logger.e(Throwable("onAdFailedToShowFullScreenContent: ${adError.message}"))
                    TrackingManager.logShowInterstitialAdFailed(adError)
                }

                override fun onAdShowedFullScreenContent() {
                    TrackingManager.logShowInterstitialAdSuccess()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    setShouldShowAppOpenAds(false)
                }
            }
            currentActivity?.let {
                isShowingInterAd = true
                interAd?.show(it)
            }
        }

    private fun canShowOpenBetaAd(): Boolean =
        !dataStore.isPurchased && !remoteConfig.offAllAds() && currentActivity is MainActivity

    private fun canShowInterSplashAd(): Boolean =
        !dataStore.isPurchased && !remoteConfig.offAllAds() && !remoteConfig.offInterSplashAds()

    override fun onStart(owner: LifecycleOwner) {
        loadOpenAd()
        if (canShowOpenBetaAd() && shouldShowAppOpenAds) {
            openAppAdsScope.launch {
                showOpenAdIfAvailableAsync()
            }
            shownOpenAdCount++
        }
        if (!shouldShowAppOpenAds) {
            shouldShowAppOpenAds = true
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

    override fun onActivityCreated(p0: Activity, p1: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        if (activity is MainActivity)
            currentActivity = activity
    }

    override fun onActivityResumed(p0: Activity) = Unit

    override fun onActivityPaused(p0: Activity) = Unit

    override fun onActivityStopped(p0: Activity) = Unit

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit

    override fun onActivityDestroyed(p0: Activity) {
        if (p0 is MainActivity) {
            currentActivity = null
        }
    }
}
