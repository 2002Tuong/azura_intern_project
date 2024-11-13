package com.calltheme.app.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.screentheme.app.BuildConfig
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.utils.extensions.isInternetAvailable
import com.screentheme.app.utils.helpers.BillingClientProvider

object BannerAdsHelpers {
    private val TAG = BannerAdsHelpers::class.simpleName
    var bannerHomeFailToLoad = MutableLiveData<Boolean>()
        private set
    var bannerThemeFailToLoad = MutableLiveData<Boolean>()
        private set
    var bannerCustomizeFailToLoad = MutableLiveData<Boolean>()
        private set

    private val _homeAdView = MutableLiveData<AdView?>()
    val homeBannerAds: LiveData<AdView?> get() = _homeAdView

    private val _themeBannerAd = MutableLiveData<AdView?>()
    val themeBannerAd: LiveData<AdView?> get() = _themeBannerAd

    private val _customizeArtBannerAd = MutableLiveData<AdView?>()
    val customizeBannerAd: LiveData<AdView?> get() = _customizeArtBannerAd
    fun requestLoadBannerAds(placement: BannerAdPlacement, context: Context) {
        if (!BillingClientProvider.getInstance(context as Activity).isPurchased
            && context.isInternetAvailable()
            && !AppRemoteConfig.offAllAds()
        ) {
            val oldAdView = (when (placement) {
                BannerAdPlacement.HOME -> _homeAdView
                BannerAdPlacement.THEME -> _themeBannerAd
                BannerAdPlacement.CUSTOMIZE -> _customizeArtBannerAd
            }).value
            if (oldAdView != null) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(context, "loadBannerAds $placement: isReload = true", Toast.LENGTH_SHORT).show()
                }
                oldAdView.loadAd(AdRequest.Builder().build())
                return
            }

            if (BuildConfig.DEBUG) {
                Toast.makeText(context, "loadBannerAds $placement: isReload = false", Toast.LENGTH_SHORT).show()
            }


            (when (placement) {
                BannerAdPlacement.HOME -> bannerHomeFailToLoad
                BannerAdPlacement.THEME -> bannerThemeFailToLoad
                BannerAdPlacement.CUSTOMIZE -> bannerCustomizeFailToLoad
            }).postValue(false)
            val id = when (placement) {
                BannerAdPlacement.HOME -> BuildConfig.banner_home
                BannerAdPlacement.THEME -> BuildConfig.banner_theme
                BannerAdPlacement.CUSTOMIZE -> BuildConfig.banner_customize
            }
            Admob.getInstance()
                .requestLoadBanner(
                    context,
                    id,
                    object : AdCallback() {
                        override fun onBannerLoaded(adView: AdView?) {
                            super.onBannerLoaded(adView)
                            (when (placement) {
                                BannerAdPlacement.HOME -> _homeAdView
                                BannerAdPlacement.THEME -> _themeBannerAd
                                BannerAdPlacement.CUSTOMIZE -> _customizeArtBannerAd
                            }).postValue(adView)
                        }

                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            super.onAdFailedToLoad(i)
                            (when (placement) {
                                BannerAdPlacement.HOME -> bannerHomeFailToLoad
                                BannerAdPlacement.THEME -> bannerThemeFailToLoad
                                BannerAdPlacement.CUSTOMIZE -> bannerCustomizeFailToLoad
                            }).postValue(true)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            requestLoadBannerAds(placement, context)
                        }
                    },
                    false,
                    Admob.BANNER_INLINE_LARGE_STYLE
                )
        } else {
            (when (placement) {
                BannerAdPlacement.HOME -> bannerHomeFailToLoad
                BannerAdPlacement.THEME -> bannerThemeFailToLoad
                BannerAdPlacement.CUSTOMIZE -> bannerCustomizeFailToLoad
            }).postValue(true)
            onDestroy(placement)
            return
        }
    }

    fun onDestroy() {
        BannerAdPlacement.values().forEach {
            onDestroy(it)
        }
    }

    private fun onDestroy(placement: BannerAdPlacement) {
        when(placement) {
            BannerAdPlacement.HOME -> {
                _homeAdView.value?.destroy()
                _homeAdView.postValue(null)
            }
            BannerAdPlacement.THEME -> {
                _themeBannerAd.value?.destroy()
                _themeBannerAd.postValue(null)
            }
            BannerAdPlacement.CUSTOMIZE -> {
                _customizeArtBannerAd.value?.destroy()
                _customizeArtBannerAd.postValue(null)
            }
        }
    }

    enum class BannerAdPlacement {
        HOME, THEME, CUSTOMIZE
    }

}