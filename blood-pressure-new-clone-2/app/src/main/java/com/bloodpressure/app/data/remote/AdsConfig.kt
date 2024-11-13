package com.bloodpressure.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote Config ads
 *
 * @property shouldShowInterSplashAd
 * @property shouldShowOpenBetaAd
 * @property shouldShowClickHistoryItemInterAd
 * @property shouldShowClickInfoItemInterAd
 * @property shouldShowClickAddRecordInterAd
 * @property shouldShowExitInterAd
 * @property shouldShowHomeNativeAd
 * @property shouldShowOnboardingNativeAd
 * @property shouldShowLanguageSettingNativeAd
 * @property shouldShowExitAppNativeAd
 * @property shouldShowHomeBannerAd
 * @property shouldShowInfoDetailBannerAd
 * @property shouldShowNativeAd
 * @property shouldShowInterAd
 * @property ctaTopLanguage
 * @property ctaTopOnboarding
 * @property shouldShowAddRecordNativeAd                true/false (show/hide **native_edit** các màn edit hoặc nhập record)
 * @property shouldShowHearRateNativeAd                 true/false (show/hide **native_heart_rate**)
 * @property shouldShowBmiNativeAd                      true/false (show/hide **native_bmi**)
 * @property shouldShowHistoryNativeAd                  true/false (show/hide **native_history** các màn history các tính năng)
 * @property shouldShowInfoNativeAd                     true/false (show/hide **native_info**)
 * @property shouldShowLanguageNativeAd                 true/false (show/hide **native_language_setting**)
 * @property shouldShowPressureNativeAd                 true/false (show/hide **native_blood_pressure**)
 * @property shouldShowBloodSugarNativeAd               true/false (show/hide **native_blood_sugar**)
 * @property shouldShowInterSugar                       true/false (show/hide **inter_sugar**)
 * @property shouldShowInterWater                       true/false (show/hide **inter_water**)
 * @property shouldShowInterHeartRate                   true/false (show/hide **inter_heart_rate**)
 * @property shouldShowInterBmi                         true/false (show/hide **inter_bmi**)
 */
@Serializable
data class AdsConfig(
    @SerialName("show_inter_splash")
    val shouldShowInterSplashAd: Boolean,
    @SerialName("show_open_beta_ad")
    val shouldShowOpenBetaAd: Boolean,
    @SerialName("show_click_history_item_inter_ad")
    val shouldShowClickHistoryItemInterAd: Boolean,
    @SerialName("show_click_info_item_inter_ad")
    val shouldShowClickInfoItemInterAd: Boolean,
    @SerialName("show_click_add_record_inter_ad")
    val shouldShowClickAddRecordInterAd: Boolean,
    @SerialName("show_exit_inter_ad")
    val shouldShowExitInterAd: Boolean,
    @SerialName("show_home_native_ad")
    val shouldShowHomeNativeAd: Boolean,
    @SerialName("show_onboarding_native_ad")
    val shouldShowOnboardingNativeAd: Boolean,
    @SerialName("show_history_native_ad")
    val shouldShowHistoryNativeAd: Boolean,
    @SerialName("show_info_native_ad")
    val shouldShowInfoNativeAd: Boolean,
    @SerialName("show_language_native_ad")
    val shouldShowLanguageNativeAd: Boolean,
    @SerialName("show_language_setting_native_ad")
    val shouldShowLanguageSettingNativeAd: Boolean,
    @SerialName("show_add_record_native_ad")
    val shouldShowAddRecordNativeAd: Boolean,
    @SerialName("show_exit_app_native_ad")
    val shouldShowExitAppNativeAd: Boolean,
    @SerialName("show_home_banner_ad")
    val shouldShowHomeBannerAd: Boolean,
    @SerialName("show_info_detail_banner_ad")
    val shouldShowInfoDetailBannerAd: Boolean,
    @SerialName("show_heart_rate_native_ad")
    val shouldShowHearRateNativeAd: Boolean,
    @SerialName("show_bmi_native_ad")
    val shouldShowBmiNativeAd: Boolean,
    @SerialName("show_native_ads")
    val shouldShowNativeAd: Boolean,
    @SerialName("show_inter_ads")
    val shouldShowInterAd: Boolean,
    @SerialName("cta_top_language")
    val ctaTopLanguage: Boolean,
    @SerialName("cta_top_onboarding")
    val ctaTopOnboarding: Boolean,
    @SerialName("show_blood_pressure_native_ad")
    val shouldShowPressureNativeAd: Boolean,
    @SerialName("show_blood_sugar_native_ad")
    val shouldShowBloodSugarNativeAd: Boolean,
    @SerialName("show_inter_sugar")
    val shouldShowInterSugar: Boolean,
    @SerialName("show_inter_water")
    val shouldShowInterWater: Boolean,
    @SerialName("show_inter_heart_rate")
    val shouldShowInterHeartRate: Boolean,
    @SerialName("show_inter_bmi")
    val shouldShowInterBmi: Boolean,
)
