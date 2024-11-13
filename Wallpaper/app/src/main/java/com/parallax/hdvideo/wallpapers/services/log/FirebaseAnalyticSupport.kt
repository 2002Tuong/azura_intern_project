package com.parallax.hdvideo.wallpapers.services.log

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.AppInfo
import com.parallax.hdvideo.wallpapers.utils.Logger

object FirebaseAnalyticSupport {

    private val appCurVersion = AppInfo.shared.appVersion
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(WallpaperApp.instance)

    fun recordEvent(eventName: String) {
        val bundle = bundleOf(FirebaseAnalytics.Param.GROUP_ID to appCurVersion)
        try {
            firebaseAnalytics.logEvent(eventName, bundle)
            Logger.d("firebase monitoring $eventName")
        } catch (e: Exception) {
            Logger.e(e)
        }
    }

    fun recordEvent(
        eventName: String,
        itemId: String? = null,
        label: String? = null,
        value: Long = 1
    ) {
        val bundle = bundleOf(
            FirebaseAnalytics.Param.ITEM_ID to itemId,
            FirebaseAnalytics.Param.ITEM_NAME to label,
            FirebaseAnalytics.Param.VALUE to value,
            FirebaseAnalytics.Param.GROUP_ID to appCurVersion
        )
        try {
            firebaseAnalytics.logEvent(eventName, bundle)
            Logger.d("firebase monitoring $eventName")
        } catch (e: Exception) {
            Logger.e(e)
        }
    }

    fun recordScreen(activity: Activity?, screenName: String) {
        try {
            activity?.also {
                val bundle = Bundle()
                bundle.putString(
                    FirebaseAnalytics.Param.SCREEN_NAME,
                    screenName
                )
                bundle.putString(
                    FirebaseAnalytics.Param.SCREEN_CLASS,
                    it.javaClass.simpleName
                )
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            }
        } catch (e: Exception) {
            Logger.e(e)
        }
    }

    fun setupConfig() {
//        if (!BuildConfig.DEBUG)
        RemoteConfig.appId = "videoparallaxwallpapertkv2secv10"
    }
}