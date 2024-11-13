package com.wifi.wificharger.utils

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
import com.wifi.wificharger.BuildConfig
import com.wifi.wificharger.data.remote.RemoteConfig

object BannerAdsUtils {
    var bannerHomeFailToLoad = MutableLiveData<Boolean>()
        private set
    private val _homeAdView = MutableLiveData<AdView?>()
    val homeBannerAds: LiveData<AdView?> get() = _homeAdView

    fun requestLoadBannerAds(placement: BannerAdPlacement, activity: Activity) {
        if (AdsUtils.canLoadAds(activity)) {
            val oldAdView = (when (placement) {
                BannerAdPlacement.HOME -> _homeAdView
            }).value
            if (oldAdView != null) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(
                        activity,
                        "loadBannerAds $placement: isReload = true",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                oldAdView.loadAd(AdRequest.Builder().build())
                return
            }

            if (BuildConfig.DEBUG) {
                Toast.makeText(
                    activity,
                    "loadBannerAds $placement: isReload = false",
                    Toast.LENGTH_SHORT
                ).show()
            }


            (when (placement) {
                BannerAdPlacement.HOME -> bannerHomeFailToLoad
            }).postValue(false)
            val id = when (placement) {
                BannerAdPlacement.HOME -> BuildConfig.banner_home
            }
            Admob.getInstance()
                .requestLoadBanner(
                    activity,
                    id,
                    object : AdCallback() {
                        override fun onBannerLoaded(adView: AdView?) {
                            super.onBannerLoaded(adView)
                            Logger.d("onBannerLoaded: $placement")
                            (when (placement) {
                                BannerAdPlacement.HOME -> _homeAdView
                            }).postValue(adView)
                        }

                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            super.onAdFailedToLoad(i)
                            Logger.d("banner onAdFailedToLoad: ${i?.message}")
                            (when (placement) {
                                BannerAdPlacement.HOME -> bannerHomeFailToLoad
                            }).postValue(true)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            requestLoadBannerAds(placement, activity)
                        }
                    },
                    false,
                    Admob.BANNER_INLINE_LARGE_STYLE
                )
        } else {
            (when (placement) {
                BannerAdPlacement.HOME -> bannerHomeFailToLoad
            }).postValue(true)
            onDestroy(placement)
            return
        }
    }

    fun onDestroy() {
        BannerAdPlacement.entries.forEach {
            onDestroy(it)
        }
    }

    private fun onDestroy(placement: BannerAdPlacement) {
        when (placement) {
            BannerAdPlacement.HOME -> {
                _homeAdView.value?.destroy()
                _homeAdView.postValue(null)
            }
        }
    }

    enum class BannerAdPlacement {
        HOME
    }
}
