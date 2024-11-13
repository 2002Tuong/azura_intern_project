package com.slideshowmaker.slideshow.data

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.slideshowmaker.slideshow.VideoMakerApplication

class TrackingEvent(
    private var name: String,
    private var bundle: Bundle? = Bundle(),
) {

    fun putParams(params: Bundle) {
        bundle?.putAll(params)
    }

    fun track() {
        if (name in listOf(
                "lifetime_app_get_remote_config_success",
                "lifetime_app_open_app",
                "lifetime_app_show_ads_open_success",
                "lifetime_app_go_main_app_success",
                "lifetime_app_click_create_slideshow",
                "lifetime_app_go_editor_slideshow",
                "lifetime_app_saver_slideshow_success",
                "lifetime_app_share_videoslideshow"
            )
        ) {
            VideoMakerApplication.instance.showToast(name)
        }
        Firebase.analytics.logEvent(name, bundle)
    }
}
