package com.bloodpressure.app.ads

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.bloodpressure.app.MainActivity
import com.bloodpressure.app.R
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.Logger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.EnumMap

class InterAdsManager(
    private val context: Context,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig
) {
    private val interstitialAds: EnumMap<InterAdPlacement, InterstitialAd?> = EnumMap(
        mapOf(
            InterAdPlacement.CLICK_INFO_ITEM to null,
            InterAdPlacement.CLICK_HISTORY_ITEM to null,
            InterAdPlacement.CLICK_ADD_RECORD to null,
            InterAdPlacement.EXIT_APP to null,
            InterAdPlacement.ADD_HEART_RATE to null,
            InterAdPlacement.ADD_BMI to null,
            InterAdPlacement.ADD_SUGAR to null,
            InterAdPlacement.ADD_WATER to null,
        )
    )
    private var lastTimeClosePopup: EnumMap<InterAdPlacement, Long?> = EnumMap(
        mapOf(
            InterAdPlacement.CLICK_INFO_ITEM to null,
            InterAdPlacement.CLICK_HISTORY_ITEM to null,
            InterAdPlacement.CLICK_ADD_RECORD to null,
            InterAdPlacement.EXIT_APP to null,
            InterAdPlacement.ADD_HEART_RATE to null,
            InterAdPlacement.ADD_BMI to null,
            InterAdPlacement.ADD_SUGAR to null,
            InterAdPlacement.ADD_WATER to null,
        )
    )
    private var loadingState: EnumMap<InterAdPlacement, Boolean> = EnumMap(
        mapOf(
            InterAdPlacement.CLICK_INFO_ITEM to false,
            InterAdPlacement.CLICK_HISTORY_ITEM to false,
            InterAdPlacement.CLICK_ADD_RECORD to false,
            InterAdPlacement.EXIT_APP to false,
            InterAdPlacement.ADD_HEART_RATE to false,
            InterAdPlacement.ADD_BMI to false,
            InterAdPlacement.ADD_SUGAR to false,
            InterAdPlacement.ADD_WATER to false,
        )
    )

    private var lastTimeAdsShown: Long = 0

    fun loadAds() {
        if (remoteConfig.adsConfig.shouldShowClickInfoItemInterAd) {
            loadAd(InterAdPlacement.CLICK_INFO_ITEM)
        }
        if (remoteConfig.adsConfig.shouldShowClickHistoryItemInterAd) {
            loadAd(InterAdPlacement.CLICK_HISTORY_ITEM)
        }
        if (remoteConfig.adsConfig.shouldShowClickAddRecordInterAd) {
            loadAd(InterAdPlacement.CLICK_ADD_RECORD)
        }
        if (remoteConfig.adsConfig.shouldShowExitInterAd) {
            loadAd(InterAdPlacement.EXIT_APP)
        }
    }

    fun loadAddFeatureAds(type: TrackerType) {
        val interAdType = when (type) {
            TrackerType.HEART_RATE -> {
                if (!remoteConfig.adsConfig.shouldShowInterHeartRate) return
                InterAdPlacement.ADD_HEART_RATE
            }
            TrackerType.BLOOD_SUGAR -> {
                if (!remoteConfig.adsConfig.shouldShowInterSugar) return
                InterAdPlacement.ADD_SUGAR
            }
            TrackerType.WEIGHT_BMI -> {
                if (!remoteConfig.adsConfig.shouldShowInterBmi) return
                InterAdPlacement.ADD_BMI
            }
            TrackerType.WATER_REMINDER -> {
                if (!remoteConfig.adsConfig.shouldShowInterWater) return
                InterAdPlacement.ADD_WATER
            }
            else -> throw IllegalArgumentException("Unsupported info item $type")
        }
        loadAd(interAdType)
    }

    private fun loadAd(type: InterAdPlacement) {
        if (!canShowAd() || loadingState[type] == true || interstitialAds[type] != null) {
            return
        }
        val adUnitResId = when (type) {
            InterAdPlacement.CLICK_HISTORY_ITEM -> R.string.inter_history
            InterAdPlacement.CLICK_INFO_ITEM -> R.string.inter_info
            InterAdPlacement.CLICK_ADD_RECORD -> R.string.inter_add
            InterAdPlacement.EXIT_APP -> R.string.inter_exit_app
            InterAdPlacement.ADD_HEART_RATE -> R.string.inter_heart_rate
            InterAdPlacement.ADD_BMI -> R.string.inter_bmi
            InterAdPlacement.ADD_SUGAR -> R.string.inter_sugar
            InterAdPlacement.ADD_WATER -> R.string.inter_water
        }
        loadingState[type] = true
        InterstitialAd.load(
            context,
            context.getString(adUnitResId),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAds[type] = interstitialAd
                    loadingState[type] = false
                    Logger.d("onAdLoaded $type: $interstitialAd")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAds[type] = null
                    loadingState[type] = false
                    Logger.e(Throwable("onAdFailedToLoad $type: ${loadAdError.message}"))
                }
            }
        )
    }

    fun showInterstitial(
        placement: InterAdPlacement,
        activity: Activity,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (interstitialAds[placement] == null && loadingState[placement] != true) {
            loadAd(placement)
            onComplete?.invoke(false)
            return
        }

        if (interstitialAds[placement] == null ||
            loadingState[placement] == true ||
            !canShowAd() ||
            isRateLimitReached()
        ) {
            onComplete?.invoke(false)
            return
        }

        val ads = interstitialAds[placement]
        interstitialAds[placement]?.setOnPaidEventListener {
            TrackingManager.logAdRevenue(
                ads?.responseInfo?.mediationAdapterClassName.orEmpty(),
                it.valueMicros / 1000000.0,
                emptyMap()
            )
        }
        interstitialAds[placement]?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAds[placement]?.fullScreenContentCallback = null
                    lastTimeClosePopup[placement] = System.currentTimeMillis()
                    onComplete?.invoke(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    TrackingManager.logEvent(
                        "interstitial_ad_failed_to_show",
                        bundleOf("ad_id" to ads?.adUnitId)
                    )
                    interstitialAds[placement]?.fullScreenContentCallback = null
                    onComplete?.invoke(false)
                    Logger.e(Throwable("onAdFailedToShowFullScreenContent $placement: ${adError.message}"))
                }

                override fun onAdShowedFullScreenContent() {
                    TrackingManager.logEvent(
                        "interstitial_ad_show_success",
                        bundleOf("ad_id" to ads?.adUnitId)
                    )
                    lastTimeAdsShown = System.currentTimeMillis()
                    interstitialAds[placement] = null
                    loadAd(placement)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    TrackingManager.logEvent(
                        "interstitial_ad_clicked",
                        bundleOf("ad_id" to ads?.adUnitId)
                    )
                }
            }

        interstitialAds[placement]?.let { interAds ->
            val intent = Intent(activity, LoadingAdsDialog::class.java)
            val componentActivity = activity as? MainActivity
            componentActivity?.let {
                it.updateActivityResultAction { interAds.show(activity) }
                it.getResultLauncher()?.launch(intent)
            }
        }
    }

    private fun canShowAd(): Boolean {
        return !dataStore.isPurchased && !remoteConfig.offAllAds() && remoteConfig.adsConfig.shouldShowInterAd
    }

    private fun isRateLimitReached(): Boolean =
        System.currentTimeMillis() - lastTimeAdsShown < remoteConfig.offsetTimeShowInterAds() * 1000

    fun onDestroy() {
        interstitialAds.clear()
    }

    enum class InterAdPlacement {
        CLICK_HISTORY_ITEM, CLICK_INFO_ITEM, CLICK_ADD_RECORD, EXIT_APP,
        ADD_HEART_RATE, ADD_BMI, ADD_SUGAR, ADD_WATER
    }
}
