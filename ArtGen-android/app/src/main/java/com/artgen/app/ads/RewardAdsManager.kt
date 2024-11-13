package com.artgen.app.ads

import android.app.Activity
import android.content.Context
import com.artgen.app.R
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.log.Logger
import com.artgen.app.tracking.TrackingManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.EnumMap

class RewardAdsManager(
    private val context: Context,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig,
    private val openAdsManager: OpenAdsManager
) {
    private val rewardedVideoAds: EnumMap<RewardedVideoAdPlacement, RewardedAd?> = EnumMap(
        mapOf(
            RewardedVideoAdPlacement.GEN_ART to null,
            RewardedVideoAdPlacement.SAVE_ART to null
        )
    )
    private var lastTimeClosePopup: EnumMap<RewardedVideoAdPlacement, Long?> = EnumMap(
        mapOf(
            RewardedVideoAdPlacement.GEN_ART to null,
            RewardedVideoAdPlacement.SAVE_ART to null
        )
    )
    private var loadingState: EnumMap<RewardedVideoAdPlacement, Boolean> = EnumMap(
        mapOf(
            RewardedVideoAdPlacement.GEN_ART to false,
            RewardedVideoAdPlacement.SAVE_ART to false
        )
    )

    private var lastTimeAdsShown: Long = 0

    fun loadAds() {
        loadAd(RewardedVideoAdPlacement.GEN_ART)
    }

    fun loadSaveArtRewardAds(reload: Boolean) {
        loadAd(RewardedVideoAdPlacement.SAVE_ART,reload)
    }

    fun loadAd(type: RewardedVideoAdPlacement, reload: Boolean = false) {
        if ((!canShowAd() || loadingState[type] == true || rewardedVideoAds[type] != null) && !reload) {
            return
        }
        TrackingManager.logEvent("load_reward_ads")
        val adUnitResId = when (type) {
            RewardedVideoAdPlacement.GEN_ART -> R.string.reward_create
            RewardedVideoAdPlacement.SAVE_ART -> R.string.reward_save
        }
        loadingState[type] = true
        RewardedAd.load(
            context,
            context.getString(adUnitResId),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedVideoAds[type] = ad
                    loadingState[type] = false
                    Logger.d("onAdLoaded $type: $ad")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedVideoAds[type] = null
                    loadingState[type] = false
                    Logger.e(Throwable("onAdFailedToLoad $type: ${loadAdError.message}"))
                }
            }
        )
    }

    fun showRewardedVideo(
        placement: RewardedVideoAdPlacement,
        activity: Activity,
        onComplete: ((Boolean) -> Unit)? = null,
        onRewardedAd: ((Boolean) -> Unit)? = null
    ) {
        if (rewardedVideoAds[placement] == null && loadingState[placement] != true) {
            loadAd(placement)
            onComplete?.invoke(false)
            return
        }

        if (rewardedVideoAds[placement] == null ||
            loadingState[placement] == true ||
            !canShowAd() ||
            isRateLimitReached()
        ) {
            onComplete?.invoke(false)
            return
        }

        rewardedVideoAds[placement]?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedVideoAds[placement]?.fullScreenContentCallback = null
                    lastTimeClosePopup[placement] = System.currentTimeMillis()
                    onComplete?.invoke(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedVideoAds[placement]?.fullScreenContentCallback = null
                    onComplete?.invoke(false)
                    Logger.e(Throwable("onAdFailedToShowFullScreenContent $placement: ${adError.message}"))
                }

                override fun onAdShowedFullScreenContent() {
                    lastTimeAdsShown = System.currentTimeMillis()
                    rewardedVideoAds[placement] = null
                    loadAd(placement)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    openAdsManager.setShouldShowAppOpenAds(false)
                }
            }

        rewardedVideoAds[placement]?.let {

            rewardedVideoAds[placement]?.show(activity) {
                onRewardedAd?.invoke(true)
            }
        } ?: run {
            onComplete?.invoke(false)
        }
    }

    private fun canShowAd(): Boolean {
        return !dataStore.isPurchased && !remoteConfig.offAllAds()
    }

    private fun isRateLimitReached(): Boolean =
        System.currentTimeMillis() - lastTimeAdsShown < remoteConfig.offsetTimeShowInterAds() * 1000

    fun onDestroy() {
        rewardedVideoAds.clear()
    }

    enum class RewardedVideoAdPlacement {
        GEN_ART,
        SAVE_ART,
    }
}
