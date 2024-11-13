package com.slideshowmaker.slideshow.data

import androidx.core.os.bundleOf

class TrackingFactory {
    object SlideshowEditor {
        fun buttonClick() = TrackingEvent("slideshow_click_button_create")
        fun viewEditor() = TrackingEvent("slideshow_view_editor")
        fun clickSave(
            quality: Int? = null,
            transitionId: String? = null,
            themeId: String? = null,
            filterId: String? = null,
            frameId: String? = null
        ) = TrackingEvent(
            "slideshow_click_saver",
            bundleOf(
                "quality" to quality,
                "transition_id" to transitionId,
                "theme_id" to themeId,
                "filter_id" to filterId,
                "frame_id" to frameId
            )
        )

        fun saveSuccess() = TrackingEvent("slideshow_saver_success")
        fun shareSuccess() = TrackingEvent("slideshow_share_video_success")
        fun useTransition(transitionId: String) =
            TrackingEvent("slideshow_use_transition", bundleOf("transition_id" to transitionId))

        fun useTheme(themeId: String) =
            TrackingEvent("slideshow_use_theme", bundleOf("theme_id" to themeId))

        fun useFrame(frameId: String) =
            TrackingEvent("slideshow_use_frame", bundleOf("frame_id" to frameId))

        fun useFilter(filterId: String) =
            TrackingEvent("slideshow_use_filter", bundleOf("filter_id" to filterId))
    }

    object EditVideo {
        fun buttonClick() = TrackingEvent("edit_video_click_button_edit")
        fun viewEditor() = TrackingEvent("edit_video_view_editor")
        fun clickSave(
            quality: Int? = null,
            ratio: Int? = null,
            effectId: String? = null,
            transitionId: String,
            frameId: String,
            filterId: String
        ) =
            TrackingEvent(
                "edit_video_click_saver",
                bundleOf(
                    "quality" to quality,
                    "ratio" to ratio,
                    "effect_id" to effectId,
                    "frame_id" to frameId,
                    "filter_id" to filterId,
                    "transition_id" to transitionId
                )
            )

        fun saveSuccess() = TrackingEvent("edit_video_saver_success")
        fun useTransition(effectId: String) =
            TrackingEvent("slideshow_use_effect", bundleOf("effect_id" to effectId))

        fun shareSuccess() = TrackingEvent("edit_video_share_video_success")
    }

    object JoinVideo {
        fun buttonClick() = TrackingEvent("join_video_click_button_join")
        fun viewEditor() = TrackingEvent("join_video_view_editor")
        fun clickSave() = TrackingEvent("join_video_click_saver")
        fun saveSuccess() = TrackingEvent("join_video_saver_success")
        fun shareSuccess() = TrackingEvent("join_video_share_video_success")
    }

    object TrimVideo {
        fun buttonClick() = TrackingEvent("trim_video_click_button_trim")
        fun viewEditor() = TrackingEvent("trim_video_view_editor")
        fun clickSave() = TrackingEvent("trim_video_click_saver")
        fun saveSuccess() = TrackingEvent("trim_video_saver_success")
        fun shareSuccess() = TrackingEvent("trim_video_share_video_success")
    }

    object MyVideo {
        fun buttonClick() = TrackingEvent("my_video_click_button")
        fun playVideo() = TrackingEvent("my_video_play_video")
    }

    object Music {
        fun useMusic(fileName: String, start: Long, end: Long) =
            TrackingEvent(
                "music_use_music",
                bundleOf("file_url" to fileName, "start" to start, "end" to end)
            )
    }

    object Common {
        fun showAdsInterstitial(screen: String) =
            TrackingEvent("show_ads_inter_view_editor", bundleOf("screen" to screen))

        fun firstLaunch() = TrackingEvent("lifetime_app_open_app")
        fun getRemoteConfigSuccessFirstLaunch() =
            TrackingEvent("lifetime_app_get_remote_config_success")

        fun showAdsOpenAppFirstLaunch() = TrackingEvent("lifetime_app_show_ads_open_success")
        fun openMainAppFirstLaunch() = TrackingEvent("lifetime_app_go_main_app_success")
        fun clickSlideShowFirstLaunch() = TrackingEvent("lifetime_app_click_create_slideshow")
        fun openSlideshowEditorFirstLaunch() = TrackingEvent("lifetime_app_go_editor_slideshow")
        fun clickSaveSlideShowFirstTime() = TrackingEvent("lifetime_app_click_save_slideshow")
        fun startExportingFirstTime() = TrackingEvent("lifetime_app_exporting_slideshow")
        fun saveSlideshowSuccessFirstLaunch() =
            TrackingEvent("lifetime_app_saver_slideshow_success")

        fun shareSlideshowSuccessFirstLaunch() = TrackingEvent("lifetime_app_share_videoslideshow")
        fun encodeSlideshowFailed() = TrackingEvent("encode_slideshow_failed")
        fun loadAppOpenAds() = TrackingEvent("load_splash_app_open_ads")
        fun loadAppOpenInterAds() = TrackingEvent("load_splash_app_open_inter_ads")
        fun permissionGranted() = TrackingEvent("permission_granted")
        fun showAdsInterSlideshowButton() = TrackingEvent("show_ads_inter_slideshow_click_button")
        fun showAdsInterMyVideo() = TrackingEvent("show_ads_inter_my_video_click_button")
    }

    object ImagePicker {
        fun openImagePicker() = TrackingEvent("image_picker_open")
        fun startLoadImages() = TrackingEvent("image_picker_load_images")
        fun startLoadAlbums() = TrackingEvent("image_picker_load_albums")
        fun onPermissionRationaleShouldBeShown() = TrackingEvent("on_permission_rationale")
    }
}