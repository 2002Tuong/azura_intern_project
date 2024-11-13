package com.example.claptofindphone.presenter.splash

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.VioAdmobCallback
import com.ads.control.ads.wrapper.ApAdError
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.data.remote.RemoteConfigProvider
import com.example.claptofindphone.utils.AdsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

import com.example.claptofindphone.AdsUnitId

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val adsHelper: AdsHelper,
    private val preferenceSupplier: PreferenceSupplier,
    private val remoteConfigProvider: RemoteConfigProvider
) : ViewModel() {

    private val _loadingState = MutableStateFlow(true)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _navigateToMainScreenState = MutableStateFlow(false)
    val navigateToMainScreenState: StateFlow<Boolean> get() = _navigateToMainScreenState

    fun loadData(activity: Activity) {
        viewModelScope.launch {
            _loadingState.update { true }
            withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                //fetch RemoteConfig
                remoteConfigProvider.fetchRemoteConfigAsync()
            }
            adsHelper.isAdsEnabled = remoteConfigProvider.isVersionAdsEnable
            Log.d("AdsLoad", "Enable: ${adsHelper.isAdsEnabled}")
            val languageCtaTop = remoteConfigProvider.isNativeAdLanguageCtaTop
            if (!preferenceSupplier.isProUser && adsHelper.isAdsEnabled) {
                adsHelper.isAdsSplashClosed.postValue(false)
                loadInterSplash(activity as AppCompatActivity)
                if (!preferenceSupplier.firstOpenComplete) {
                    adsHelper.requestNativeLanguage(activity, ctaTop =  languageCtaTop)
                    adsHelper.loadBannerLanguage()
                }else {
                    adsHelper.loadBannerFindPhone()
                    adsHelper.requestNativeHome(activity)
                }
            } else {
                delay(TIME_DELAY_IN_MILLIS)
                adsHelper.isAdsSplashClosed.postValue(true)
                _navigateToMainScreenState.update { true }
            }
        }
    }

    private fun loadInterSplash(activity: AppCompatActivity) {
        VioAdmob.getInstance().loadSplashInterstitialAds(
            activity,
            AdsUnitId.inter_splash,
            REQUEST_TIME_OUT_IN_MILLIS,
            TIME_DELAY_IN_MILLIS,
            true,
            object : VioAdmobCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    _navigateToMainScreenState.update { true }
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    adsHelper.isAdsSplashClosed.postValue(true)
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    adsHelper.isAdsSplashClosed.postValue(true)
                }
            })
    }

    fun checkShowInterSplashWhenFail(activity: AppCompatActivity) {
        VioAdmob.getInstance()
            .onCheckShowSplashWhenFail(
                activity,
                object : VioAdmobCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        _navigateToMainScreenState.update { true }
                    }
                },
                1000
            )
    }

    companion object {
        private const val TIMEOUT_IN_MILLIS = 10000L
        private const val MIN_TIMEOUT_MS = 100L
        private val REQUEST_TIME_OUT_IN_MILLIS = 30000L
        private val TIME_DELAY_IN_MILLIS = 3000L
    }
}
