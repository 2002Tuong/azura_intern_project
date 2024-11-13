package com.parallax.hdvideo.wallpapers.services.log

interface UserEvent : Event
interface UserEventWithStatus : Event

sealed class DownloadStatus

data class FailDownload(val error: String?) : DownloadStatus() {
    enum class FailDownLoadUserEvent(override val nameEvent: String) : UserEventWithStatus {
        DownloadFail("e2_download_fail"),
        LoadFail("e2_load_fail"),
        LoadImageFail("e2_load_image_fail"),
        LoadVideoFail("e2_load_video_fail");
    }
}

enum class EventDownload(override val nameEvent: String) : UserEvent {
    ClickedDownloadImage("e2_download_image"),
    ClickedDownloadVideo("e2_download_video"),
    ClickedDownloadAll("e2_download_all"),
    ClickedDownloadFromHome("e2_download_from_home"),
    ClickedDownloadFromHomeContentType("e2_download_from_home_"),
    ClickedDownloadFromHashTagList("e2_download_from_hashtag_list"),
    ClickedDownloadHashTag("e2_download_from_hashtag_"),
    DownloadFromSearch("e2_download_from_search"),
    DownLoadFromHashTagListFromST("e2_download_from_hashtag_list_from_ST"),
    DownLoadFromTopDown("e2_download_from_top_download"),
    DownLoadFromTrending("e2_download_from_trending"),
    DownLoadFromCategory("e2_download_from_category"),
    ClickedDownloadHashTagSimilar("e2_download_from_hashtag_similar"),
    E2_DOWN_COUNT("e2_down_count"),
    E2_DOWN_FROM_SWITCH("e2_down_from_switch"),
    E2_DOWN_SUCCESS("e2_download_success"),
    DownloadSuccessAll("e2_download_success_all"),
    DownloadSuccessImage("e2_download_success_image"),
    DownloadSuccessVideo("e2_download_success_video"),
}

enum class EventSetWall(override val nameEvent: String) : UserEvent {
    SetWallpaperOnHomeScreen("e2_set_wallpaper_image_home_screen"),
    SetWallpaperOnLockScreen("e2_set_wallpaper_image_lock_screen"),
    SetWallpaperOnBothScreen("e2_set_wallpaper_image_both"),
    SetWallpaperImageSuccess("e2_set_wallpaper_image_success"),
    SetWallpaperImageFail("e2_set_wallpaper_image_fail"),
    SetWallpaperVideoSuccess("e2_set_wallpaper_video_success"),
    SetWallpaperVideoFail("e2_set_wallpaper_video_fail"),
    SetWallSuccessFromDeviceImage("e2_set_wall_success_from_device_image"),
    E2_SET_WALLPAPER_CLICK("e2_set_wallpaper_click"),
    E2_SET_WALLPAPER_STATE("e2_set_wallpaper_state"),
}

enum class EventFavoriteWall(override val nameEvent: String) : UserEvent {
    FavoriteWallpaper("e2_favorite_wallpaper");
  }

enum class EventSearch(override val nameEvent: String) : UserEvent {
    OpenSearch("e2_search_open"),
    SearchWithKeyWord("e2_search_with_keyword"),
    SearchByTopDownload("e2_search_open_top_download"),
    SearchByColor("e2_search_open_colors"),
    OpenHashTagsSearchTrend("e2_hashtag_list_open_from_search_trend"),
    OpenTrendingFromSearch("e2_open_trending_from_search"),
    SearchWithHashTag("e2_search_with_hashtag"),
    FilterWallpaper("e2_hashtag_list_choose_image_tab"),
    FilterVideo("e2_hashtag_list_choose_video_tab"),
    SearchNoResult("e2_search_no_result"),
    OpenHashTagsFromHome("e2_hashtag_list_open_from_home");
}

enum class EventMoreApp(override val nameEvent: String) : UserEvent {
    DownloadApp("e2_more_app_click_app");
   }

enum class EventRateApp(override val nameEvent: String) : UserEvent {
    ShowInviteRate("e2_rate_popup_invite_show"),
    ClickedLike("e2_rate_popup_invite_like"),
    ClickedDislike("e2_rate_popup_invite_dislike"),
    ClickedNotNow("e2_rate_popup_not_now"),
    E2RatePopupRate("e2_rate_popup_rate"),
    Rated1Star("e2_rate_popup_rate_1_star"),
    Rated2Star("e2_rate_popup_rate_2_star"),
    Rated3Star("e2_rate_popup_rate_3_star"),
    Rated4Star("e2_rate_popup_rate_4_star"),
    Rated5Star("e2_rate_popup_rate_5_star"),
    ShowFeedback("e2_rate_popup_feedback_show"),
    SubmitFeedback("e2_rate_popup_feedback_submit"),
    OpenStore("e2_rate_popup_jump_to_play_store");
}

enum class EventPermission(override val nameEvent: String) : UserEvent {
    DeniedPermission("e2_permission_denied"),
    AllowPermission("e2_permission_allowed");
}

enum class EventDetail(override val nameEvent: String) : UserEvent {
    OpenDetail("e2_detail_open"),
    OpenDetailFromHome("e2_detail_open_from_home"),
    OpenDetailFromTopDown("e2_detail_open_from_top_download"),
    OpenDetailFromTrending("e2_detail_open_from_trending"),
    OpenDetailFromSearchResult("e2_detail_open_from_search_result"),
    OpenDetailFromHashTag("e2_detail_open_from_hashtag"),
    OpenDetailFromHashTagFromHome("e2_detail_open_from_hashtag_from_home"),
    OpenDetailFromHashTagFromST("e2_detail_open_from_hashtag_from_ST"),
    OpenDetailFromCategory("e2_detail_open_from_category"),
    OpenDetailFromHomeTopDown("e2_detail_open_from_home_topdown"),
    OpenDetailHomeTrending("e2_detail_open_from_home_trending"),
    OpenDetailFromHomeNew("e2_detail_open_from_home_new"),
    OpenDetailFromHomeHashTag("e2_detail_open_from_home_hashtag"),
    DetailShowBarIn2Second("e2_detail_show_bar_in_2s_case_switch"),
    E2DetailOpen("e2_detail_open"),
    DetailSlideWallpaper("e2_detail_switch_wallpaper"),
    SwitchToPreviewHomeScreen("e2_detail_switch_preview_to_homescreen"),
    SwitchToPreviewLockScreen("e2_detail_switch_preview_to_lockscreen"),
}

enum class EventOpen(override val nameEvent: String) : UserEvent {
    OpenCategory("e2_open_category"),
    OpenCategoryList("e2_open_category_list"),
    OpenLeftMenu("e2_open_left_menu"),
    OpenDownloadList("e2_open_download_list"),
    OpenFavoriteList("e2_open_favorite_list"),
    E2HashTagList("e2_open_hashtag_list"),
    E2OpenTrending("e2_open_trending"),
}

enum class EventSetting(override val nameEvent: String) : UserEvent {
    OpenSetting("e2_open_setting"),
    OpenLocalStorageList("e2_open_local_list"),
    OpenPolicy("e2_open_policy"),
    SendMailSupport("e2_send_email"),
    RequestNewWallpaper("e2_request_new_wallpaper"),
    ResetDefaultSetting("e2_reset_wallpaper");
}

enum class EventOther(override val nameEvent: String) : UserEvent {
    ReOpenAppInADay("e2_reopen_app_in_a_day"),
    Scroll("e2_scroll"),
    ClickBackToTop("e2_click_back_to_top"),
    NotificationOff("e2_noti_off"),
    NotificationOn("e2_noti_on"),
    AutoPlayVideoOff("e2_autoplay_video_off"),
    AutoPlayVideoOn("e2_autoplay_video_on"),
    OpenTrendingFromHome("e2_open_trending_from_home_tab"),
    E2UseAppXMinute("e2_use_app_%dm"),
    E2UseAppTime("e2_use_app_time"),
    ShareWallpaper("e2_share_wallpaper"),
    ErrorAndroid12Yes("e2_error_set_video_android_12_yes"),
    ErrorAndroid12No("e2_error_set_video_android_12_no"),
    Reload("e2_reload_list");
}

enum class EventNotification(override val nameEvent: String): UserEvent {
    DayOneNotificationReceived("e3_noti_receive_D1"),
    DayTwoNotificationReceived("e3_noti_receive_D2"),
    DayFourNotificationReceived("e3_noti_receive_D4"),
    DaySevenNotificationReceived("e3_noti_receive_D7"),
    WeeklyNotificationReceived("e3_noti_receive_repeat_each_week"),
    DayOneNotificationOpen("e3_noti_open_D1"),
    DayTwoNotificationOpen("e3_noti_open_D2"),
    DayFourNotificationOpen("e3_noti_open_D4"),
    DaySevenNotificationOpen("e3_noti_open_D7"),
    NotificationReceiveFcm("e3_noti_receive_fcm_"),
    NotificationReceiveAll("e3_noti_receive_all"),
    NotificationOpenAll("e3_noti_open_all"),
    E3NotificationReceive("e3_noti_receive"),
    E3NotificationOpen("e3_noti_open"),
    WeeklyNotificationOpen("e3_noti_open_repeat_each_week");
}

enum class EventData(override val nameEvent: String): UserEvent {
    EmptyDataEventInDetail("e4_empty_data_in_detail"),
    GetConfigFailed("e4_get_config_failed"),
    GetConfig("e4_get_config"),
    VideoEngineSurfaceCreated("e4_video_engine_created"),
    VideoEngineError("e4_video_engine_error"),
    VideoExoPlayerError("e4_video_play_exo"),
    IsSupportLiveWallDevice("e4_support_live_wall"),
    IsNotSupportLiveWallDevice("e4_support_live_wall_not"),
    LoadHomePageSucceedTimeInMs("e4_load_home_page_time"),
    TimesOfLoadHomePageFail("e4_load_home_page_failed_final"),
    WaitTimeLoad("e4_wait_time_load"),
    E4LanguageDevice("e4_lang_device"),
    E4_WAIT_TIME_DOWNLOAD("e4_wait_time_download"),
    FailConstructViewModel("e4_fail_view_model_from_"),
    E2ViewWallCount("e2_view_wall_count"),
}

enum class EventIap(override val nameEvent: String) : UserEvent {
    PurchaseSuccess("e0_iap_purchase_success"),
    PurchaseClickStartTrial("e0_iap_purchase_click_start_trial"),
    E0PurchaseClickStart("e0_iap_purchase_click_start"),
    PurchaseClickTrialStartCase("e0_iap_purchase_click_start_case_"),
    PurchaseSuccessMonth("e0_iap_purchase_success_monthly_"),
    PurchaseSuccessYear("e0_iap_purchase_success_yearly_"),
    PurchaseChooseYearly("e0_iap_choose_yearly"),
    PurchaseSuccessYearlySale("e0_iap_purchase_success_year_sale_"),
    PurchaseChooseMonthly("e0_iap_choose_monthly"),
    E0IapViewVip("e0_iap_view_vip"),
    E0IapViewVipInviteExpire("e0_iap_view_vip_invite_expire"),
    E0IapViewVipInviteSaleIn("e0_iap_view_vip_noti_sale_in_app"),
    E0IapViewVipInviteSaleOut("e0_iap_view_vip_noti_sale_out_app"),
    E0IapViewVipTopBar("e0_iap_view_vip_top_bar"),
    E0IapViewVipBackDetail("e0_iap_view_vip_back_detail"),
    E0IapViewVipBackDetailQuit2s("e0_iap_view_vip_back_detail_quit_in_2s"),
    E0IapClickVipFrom("e0_iap_click_vip_from_")
}
