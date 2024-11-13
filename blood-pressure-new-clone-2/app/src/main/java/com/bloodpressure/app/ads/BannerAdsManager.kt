package com.bloodpressure.app.ads

import android.content.Context
import com.bloodpressure.app.R
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BannerAdsManager(private val context: Context) {

    private val _homeAdView = MutableStateFlow<AdView?>(null)
    private var needReload = false
    val homeAdView: StateFlow<AdView?>
        get() {
            needReload = true
            return _homeAdView
        }

    fun loadBannerAd() {
        val adView = AdView(context).apply {
            setAdSize(this@BannerAdsManager.getAdSize())
            adUnitId = context.resources.getString(R.string.banner)
            loadAd(AdRequest.Builder().build())
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.d("banner loaded")
                    requestLayout()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Logger.d("banner loaded fail")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    reloadBannerAd()
                }
            }
            setOnPaidEventListener {
                TrackingManager.logAdRevenue(
                    this.responseInfo?.mediationAdapterClassName.orEmpty(),
                    it.valueMicros / 1_000_000.0, emptyMap()
                )
            }
        }
        _homeAdView.update { adView }
    }

    fun reloadBannerAd() {
        Logger.d("reload banner")
        if (_homeAdView.value?.isLoading == true || !needReload) {
            return
        }
        Logger.d("truly reload banner")
        _homeAdView.value?.loadAd(AdRequest.Builder().build())
    }

    private fun getAdSize(): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidth = displayMetrics.widthPixels / displayMetrics.density
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            adWidth.toInt()
        )
    }

    fun onDestroy() {
        _homeAdView.update { null }
    }
}
