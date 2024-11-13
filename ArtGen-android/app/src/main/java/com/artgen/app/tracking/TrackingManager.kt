package com.artgen.app.tracking

import android.os.Bundle
import androidx.core.os.bundleOf
import com.artgen.app.log.Logger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object TrackingManager {

    fun logLanguageSelectionLaunchEvent() {
        logEvent("language_selection_launch")
    }

    fun logOnboardingLaunchEvent() {
        logEvent("onboarding_launch")
    }

    fun logSplashScreenLaunch() {
        logEvent("splash_screen_launch")
    }

    fun logStartLoadLanguageNativeAdEvent() {
        logEvent("start_load_language_native_ad")
    }
    fun logStartLoadLanguageSettingNativeAdEvent() {
        logEvent("start_load_language_setting_native_ad")
    }

    fun logLoadLanguageNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_language_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logLanguageNativeAdStartShowEvent() {
        logEvent("native_language_ad_start_show")
    }

    fun logShowStylePickerNativeAdSuccess() {
        logEvent("show_style_picker_native_ad_success")
    }

    fun logStartLoadOnboardingNativeAdEvent() {
        logEvent("start_load_onboarding_native_ad")
    }

    fun logLoadNativeAdFailed(eventName: String, reasonCode: Int) {
        logEvent(eventName, bundleOf("reason" to reasonCode))
    }

    fun logNativeAdImpression(eventName: String) {
        logEvent(eventName)
    }
    fun logLoadOnboardingNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_onboarding_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logOnboardingNativeAdStartShowEvent() {
        logEvent("native_onboarding_ad_start_show")
    }

    fun logSelectLanguageButtonClickEvent() {
        logEvent("select_language_button_click")
    }

    fun logStartLoadStylePickerNativeAdEvent() {
        logEvent("start_load_style_picker_native_ad")
    }

    fun logLoadStylePickerNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_style_picker_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logStartLoadImagePickerNativeAdEvent() {
        logEvent("start_load_image_picker_native_ad")
    }

    fun logLoadImagePickerNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_image_picker_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logStartLoadAllStyleNativeAdEvent() {
        logEvent("start_load_all_style_native_ad")
    }

    fun logLoadAllStyleNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_all_style_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logStartLoadAllDoneNativeAdEvent() {
        logEvent("start_load_all_done_native_ad")
    }

    fun logLoadAllDoneNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_all_done_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logStartLoadResultNativeAdEvent() {
        logEvent("start_load_result_native_ad")
    }

    fun logLoadResultNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_result_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logEvent(eventName: String, params: Bundle? = null) {
        Firebase.analytics.logEvent(eventName, params)
        Logger.d("event: $eventName -- params: ${params.toString()}")
    }

    fun logLoadInterstitialAd() {
        logEvent("start_load_interstitial_ad")
    }

    fun logInterstitialAdLoaded() {
        logEvent("load_interstitial_ad_success")
    }

    fun logInterstitialAdFailed(error: LoadAdError) {
        logEvent("load_interstitial_ad_failed", bundleOf("reason" to error.code))
    }

    fun logShowInterstitialAdSuccess() {
        logEvent("show_interstitial_ad_success")
    }

    fun logShowInterstitialAdFailed(adError: AdError) {
        logEvent("show_interstitial_ad_failed", bundleOf("reason" to adError.code))
    }
}
