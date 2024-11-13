package com.parallax.hdvideo.wallpapers.services.log

interface Event {
    val nameEvent: String
}

enum class AdEvent(override val nameEvent: String) : Event {
    LoadRewardedAdSuccess("e1_rewarded_load_success"),
    LoadNativeAdSuccess("e1_native_load_success"),
    LoadInterstitialAdSuccess("e1_inter_load_success"),
    OpenAdLoadSuccess("e1_open_ad_load_success"),

    LoadRewardedAdFailFinal("e1_rewarded_load_fail_final"),
    LoadInterstitialAdFailFinal("e1_inter_load_fail_final"),
    OpenAdLoadFailFinal("e1_open_ad_load_fail_final"),
    LoadNativeAdFailFinal("e1_native_load_fail_final"),

    RewardedAdShowed("e1_rewarded_showed"),
    InterstitialAdShowed("e1_inter_showed"),
    OpenAdShowed("e1_open_ad_showed"),

    RewardedLoadSuccessShowFail("e1_rewarded_load_success_show_fail"),
    InterLoadSuccessShowFail("e1_inter_load_success_show_fail"),
    OpenLoadSuccessShowFail("e1_open_ad_load_success_show_fail"),

    RewardedFailToShow("e1_rewarded_fail_to_show"),
    InterFailToShow("e1_inter_fail_to_show"),
    OpenAdFailToShow("e1_open_ad_fail_to_show"),

    UserEarnedReward("e1_rewarded_earned_reward"),

    RewardedClickView("e2_rewarded_click_view"),
    RewardedClickCancel("e2_rewarded_click_cancel"),
    RewardedClickViewOnExplain("e2_rewarded_click_view_on_explain"),
    RewardedDeclineOnExplain("e2_rewarded_decline_on_explain"),
    E1_LOAD_ADS("e1_load_ads"),
    E1_SHOW_ADS("e1_show_ads"),
    E1_LOAD_FAIL_ADS("e1_load_fail_ads"),
}

