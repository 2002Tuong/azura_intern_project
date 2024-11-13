package com.artgen.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.artgen.app.R
import com.artgen.app.ads.OpenAdsManager
import com.artgen.app.log.Logger
import com.artgen.app.tracking.TrackingManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdsUtils(private val openAdsManager: OpenAdsManager) {
    private val TAG = AdsUtils::class.simpleName
    val nativeLanguageFirstOpen = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding1 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding2 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoarding3 = MutableLiveData<ApNativeAd?>()
    val nativeOnBoardingFailLoad = MutableLiveData<Boolean>()
    val isCloseAdSplash = MutableLiveData<Boolean>()
    private var countLoading = 0

    @SuppressLint("StaticFieldLeak")
    private var adLoader: AdLoader? = null

    private var _interCustomize: ApInterstitialAd? = null
    private var _interTheme: ApInterstitialAd? = null
    private var countClickCustomize = 0
    private var requestStateCustomize = RequestState.IDE
    private var requestStateTheme = RequestState.IDE
    var isAdEnabled: Boolean = false

    fun loadNativeOnboard(
        context: Context
    ) {
        if (!isAdEnabled) return
        if (context.isInternetAvailable()) {
            Log.d(TAG, "loadNativeOnboard: ")
            countLoading = 0
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()
            adLoader = AdLoader.Builder(context, context.getString(R.string.native_onboarding))
                .forNativeAd { nativeAd ->
                    when (countLoading) {
                        0 -> {
                            nativeOnBoarding1.postValue(
                                ApNativeAd(
                                    R.layout.layout_native_onboarding,
                                    nativeAd
                                )
                            )
                        }

                        1 -> {
                            nativeOnBoarding2.postValue(
                                ApNativeAd(
                                    R.layout.layout_native_onboarding,
                                    nativeAd
                                )
                            )
                        }

                        2 -> {
                            nativeOnBoarding3.postValue(
                                ApNativeAd(
                                    R.layout.layout_native_onboarding,
                                    nativeAd
                                )
                            )
                        }
                    }
                    if (adLoader?.isLoading == true) countLoading++
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        nativeOnBoardingFailLoad.postValue(true)
                    }


                    override fun onAdClicked() {
                        super.onAdClicked()
                        openAdsManager.setShouldShowAppOpenAds(false)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        TrackingManager.logNativeAdImpression(
                            "show_native_onboarding_ad_success"
                        )
                    }
                })
                .withNativeAdOptions(adOptions)
                .build()
            adLoader?.loadAds(Admob.getInstance().adRequest, 3)
        } else {
            nativeOnBoardingFailLoad.postValue(true)
        }
    }

}

enum class RequestState {
    IDE, LOADING, COMPLETE
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}