package com.parallax.hdvideo.wallpapers.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.services.log.AdEvent
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.lang.ref.WeakReference
import java.util.*

object AppOpenAdManager : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private var openAd: AppOpenAd? = null
    private var adLoadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var isShowingAd = false
    private var curActivity: WeakReference<Activity>? = null
    private var loadingTime = 0L
    private var turnOn = true
    private var canShowAds = false

    fun start() {
        WallpaperApp.instance.unregisterActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        WallpaperApp.instance.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (AdsManager.isVipUser) return
        if (turnOn) {
            showAdIfAvailable()
        }
    }

    fun switchOnOff(turnOn: Boolean) {
        this.turnOn = turnOn
    }

    /** Shows the ad if one isn't already showing. */
    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable() && canShowAds) {
            Logger.d("will show ad")
            val fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    TrackingSupport.recordEventOnlyFirebase(AdEvent.OpenAdShowed)
                    isShowingAd = true
                }

                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    openAd = null
                    isShowingAd = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Logger.d("RewardedAd failed to show")
                }
            }
            openAd?.fullScreenContentCallback = fullScreenContentCallback
            val activity = curActivity?.get() ?: return
            openAd?.show(activity)
        } else {
            Logger.d("cannot show ad")
            fetchAd()
        }
    }

    /** Request an ad  */
    private fun fetchAd() {
        if (isAdAvailable()) return
        adLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                openAd = ad
                loadingTime = Date().time
                TrackingSupport.recordEventOnlyFirebase(AdEvent.OpenAdLoadSuccess)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                TrackingSupport.recordEventOnlyFirebase(AdEvent.OpenAdLoadFailFinal)
            }
        }
        AppOpenAd.load(
            WallpaperApp.instance, AdsManager.KEY_OPEN_AD, AdsManager.buildAdRequest(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, adLoadCallback as AppOpenAd.AppOpenAdLoadCallback
        )
    }

    /** Utility method that checks if ad exists and can be shown.  */
    private fun isAdAvailable(): Boolean {
        return openAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /** Utility method to check if ad was loaded more than n hours ago.  */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadingTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStarted(activity: Activity) {
        curActivity = WeakReference(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is MainActivity) canShowAds = false
        curActivity?.clear()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity is MainActivity) {
            canShowAds = true
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
        curActivity = WeakReference(activity)
    }
}