package com.artgen.app.ads

import android.content.Context
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.artgen.app.BuildConfig
import com.artgen.app.R
import com.artgen.app.data.local.AppDataStore
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class BannerAdsManager(
    private val context: Context,
    private val appDataStore: AppDataStore,
    private val openAdsManager: OpenAdsManager
) {

    private val _cropPhotoAdView = MutableStateFlow<AdView?>(null)
    val cropPhotoAdView: StateFlow<AdView?> get() = _cropPhotoAdView

    private val _genArtBannerAd = MutableStateFlow<AdView?>(null)
    val genArtBannerAd: StateFlow<AdView?> get() = _genArtBannerAd

    private val _resultArtBannerAd = MutableStateFlow<AdView?>(null)
    val resultArtBannerAd: StateFlow<AdView?> get() = _resultArtBannerAd

    fun loadAd(placement: BannerAdPlacement) {
        if(!canLoadAds()) {
            onDestroy(placement)
            return
        }
        Timber.d("load banner ad for ${placement}")
        val oldAdView = (when (placement) {
            BannerAdPlacement.CROP_PHOTO -> _cropPhotoAdView
            BannerAdPlacement.GEN_ART -> _genArtBannerAd
            BannerAdPlacement.RESULT_SCR -> _resultArtBannerAd
        }).value
        if (oldAdView != null) {
            oldAdView.loadAd(AdRequest.Builder().build())
            return
        }
        val adView = AdView(context).apply {
            setAdSize(this@BannerAdsManager.getAdSize())
            adUnitId = context.resources.getString(R.string.banner)
            loadAd(AdRequest.Builder().build())
            id = ViewCompat.generateViewId()
        }
        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
                openAdsManager.setShouldShowAppOpenAds(false)
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Timber.d("onAdImpression ${placement.name}")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                if (BuildConfig.FLAVOR == "dev") {
                    Toast.makeText(
                        context,
                        "Banner ads ${placement.name} has been loaded",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Timber.d("onAdLoaded ${placement.name}")
            }
        }
        (when (placement) {
            BannerAdPlacement.CROP_PHOTO -> _cropPhotoAdView
            BannerAdPlacement.GEN_ART -> _genArtBannerAd
            BannerAdPlacement.RESULT_SCR -> _resultArtBannerAd
        }).update { adView }
    }

    private fun getAdSize(): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidth = displayMetrics.widthPixels / displayMetrics.density
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context, adWidth.toInt()
        )
    }

    private fun canLoadAds(): Boolean {
        return !appDataStore.isPurchased
    }

    fun onDestroy() {
        BannerAdPlacement.values().forEach {
            onDestroy(it)
        }
    }

    private fun onDestroy(placement: BannerAdPlacement) {
        when(placement) {
            BannerAdPlacement.CROP_PHOTO -> {
                _cropPhotoAdView.value?.destroy()
                _cropPhotoAdView.tryEmit(null)
            }
            BannerAdPlacement.GEN_ART -> {
                _genArtBannerAd.value?.destroy()
                _genArtBannerAd.tryEmit(null)
            }
            BannerAdPlacement.RESULT_SCR -> {
                _resultArtBannerAd.value?.destroy()
                _resultArtBannerAd.tryEmit(null)
            }
        }
    }
    enum class BannerAdPlacement {
        CROP_PHOTO, GEN_ART, RESULT_SCR
    }

}
