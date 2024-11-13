package com.bloodpressure.app.tracking

import android.os.Bundle
import androidx.core.os.bundleOf
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.appsflyer.adrevenue.adnetworks.generic.MediationNetwork
import com.bloodpressure.app.utils.Logger
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.util.Currency
import java.util.Locale

object TrackingManager {

    fun logLanguageSelectionLaunchEvent() {
        logEvent("language_selection_launch")
    }

    fun logOnboardingLaunchEvent() {
        logEvent("onboarding_launch")
    }

    fun logTrackerScreenLaunchEvent() {
        logEvent("tracker_screen_launch")
    }

    fun logInfoScreenLaunchEvent() {
        logEvent("info_screen_launch")
    }

    fun logSettingScreenLaunchEvent() {
        logEvent("setting_screen_launch")
    }

    fun logAddRecordScreenLaunchEvent() {
        logEvent("add_record_screen_launch")
    }

    fun logInfoDetailScreenLaunchEvent() {
        logEvent("info_detail_screen_launch")
    }

    fun logHistoryScreenLaunch() {
        logEvent("history_screen_launch")
    }

    fun logSplashScreenLaunch() {
        logEvent("splash_screen_launch")
    }

    fun logStartLoadLanguageNativeAdEvent() {
        logEvent("start_load_language_native_ad")
    }

    fun logLoadLanguageNativeAdSuccessEvent(loadTime: Long) {
        logEvent("load_language_native_ad_success", bundleOf("load_time" to loadTime))
    }

    fun logLanguageNativeAdStartShowEvent() {
        logEvent("native_language_ad_start_show")
    }

    fun logStartLoadOnboardingNativeAdEvent() {
        logEvent("start_load_onboarding_native_ad")
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

    fun logAddHeartRateRecordScreenLaunchEvent() {
        logEvent("add_heart_rate_record_screen_launch")
    }

    fun logHeartRateHistoryScreenLaunch() {
        logEvent("heart_rate_history_screen_launch")
    }

    fun logEvent(eventName: String, params: Bundle? = null) {
        Firebase.analytics.logEvent(eventName, params)
        Logger.d("event: $eventName -- params: ${params.toString()}")
    }

    fun logOnConversionDataSuccess() {
        logEvent("appsflyer_on_conversion_data_success")
    }

    fun logOnConversionDataFail() {
        logEvent("appsflyer_on_conversion_data_fail")
    }

    fun logOnAppOpenAttribution() {
        logEvent("appsflyer_on_app_open_attribution")
    }

    fun logOnAttributionFailure() {
        logEvent("appsflyer_on_conversion_attribution_failure")
    }

    fun logAdRevenue(
        adMediationNetwork: String,
        revenue: Double,
        data: Map<String, String>
    ) {
        val monetization = when (adMediationNetwork) {
            "com.jirbo.adcolony.AdColonyAdapter", "com.google.ads.mediation.adcolony.AdColonyMediationAdapter" -> "adcolony"
            "com.google.ads.mediation.applovin.ApplovinAdapter", "com.google.ads.mediation.applovin.AppLovinMediationAdapter" -> "applovin"
            "com.google.ads.mediation.fyber.FyberMediationAdapter" -> "fyber"
            "com.google.ads.mediation.inmobi.InMobiAdapter" -> "inmobi"
            "com.google.ads.mediation.ironsource.IronSourceAdapter", "com.google.ads.mediation.ironsource.IronSourceRewardedAdapter" -> "ironsource"
            "com.vungle.mediation.VungleInterstitialAdapter", "com.vungle.mediation.VungleAdapter" -> "vungle"
            "com.google.ads.mediation.facebook.FacebookAdapter", "com.google.ads.mediation.facebook.FacebookMediationAdapter" -> "facebook"
            "com.mbridge.msdk", "com.google.ads.mediation.mintegral.MintegralMediationAdapter" -> "mintegral"
            "com.pangle.ads", "com.google.ads.mediation.pangle.PangleMediationAdapter" -> "pangle"
            "com.google.ads.mediation.unity.UnityAdapter", "com.google.ads.mediation.unity.UnityMediationAdapter" -> "unity"
            else -> "admob"
        }
        AppsFlyerAdRevenue.logAdRevenue(
            monetization,
            MediationNetwork.googleadmob,
            Currency.getInstance(
                Locale.US
            ),
            revenue,
            data
        )
    }
}
