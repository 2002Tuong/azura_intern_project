package com.parallax.hdvideo.wallpapers.services.log

import android.app.Activity
import com.parallax.hdvideo.wallpapers.data.model.MoreAppModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper

object TrackingSupport {
    private const val RATING_STAR = "rating_star"
    private const val TOTAL_WALLPAPER = "total_wallpaper"
    private const val KEYWORD = "keyword"
    private const val PERMISSION = "permission"
    const val ERROR = "error"
    var cachedImageId = mutableSetOf<String>()

    /**
     * Tracking event with event name
     * @param eventName: Name of event is tracked
     */
    fun recordEvent(eventName: Event, vararg data: Pair<String, String?>) {
        FirebaseAnalyticSupport.recordEvent(eventName.nameEvent)
    }

    fun recordEventOnlyFirebase(eventName: Event) {
        FirebaseAnalyticSupport.recordEvent(eventName.nameEvent)
    }

    fun recordEventOnlyFirebase(eventName: String) {
        FirebaseAnalyticSupport.recordEvent(eventName)
    }

    /**
     * Tracking event with event name
     * @param eventName: Name of event is tracked
     * @param data: data.second is screen name in this case
     * Check event 79, 80 in file 11 Events - HD Wallpaper for more information
     */
    fun recordEventWithScreenName(eventName: Event, data: Pair<String, String>) {
        FirebaseAnalyticSupport.recordEvent(eventName.nameEvent + data.second)
    }

    //region SetWall event
    fun recordSetWallEvent(type: WallpaperHelper.WallpaperType, wallpaper: WallpaperModel) {
        if (wallpaper.isVideo) return
        when (type) {
            WallpaperHelper.WallpaperType.HOME -> recordEventOnlyFirebase(EventSetWall.SetWallpaperOnHomeScreen)
            WallpaperHelper.WallpaperType.LOCK -> recordEventOnlyFirebase(EventSetWall.SetWallpaperOnLockScreen)
            else -> recordEventOnlyFirebase(EventSetWall.SetWallpaperOnBothScreen)
        }
    }
    //endregion

    //region detail event
    fun recordSlideWallInDetail(userEvent: EventDetail, wallpaper: WallpaperModel) {
        recordEventOnlyFirebase(userEvent)
    }

    fun recordSearchKeywords(userEvent: EventSearch, keyword: String) {
        FirebaseAnalyticSupport.recordEvent(eventName = userEvent.nameEvent)
    }
    //endregion

    //region More App event
    fun recordMoreAppEvent(userEvent: EventMoreApp, app: MoreAppModel) {
        FirebaseAnalyticSupport.recordEvent(
            eventName = userEvent.nameEvent,
            itemId = app.id,
            label = app.name
        )
    }
    //endregion

    fun recordRateAppEvent(userEvent: EventRateApp, star: Int? = null) {
        star ?: return
        val value = star.coerceIn(1..5)
        when (value) {
            1 -> recordEventOnlyFirebase(EventRateApp.Rated1Star)
            2 -> recordEventOnlyFirebase(EventRateApp.Rated2Star)
            3 -> recordEventOnlyFirebase(EventRateApp.Rated3Star)
            4 -> recordEventOnlyFirebase(EventRateApp.Rated4Star)
            5 -> recordEventOnlyFirebase(EventRateApp.Rated5Star)
        }
    }

    fun recordPermissionEvent(userEvent: EventPermission, permission: String, value: Long) {
        FirebaseAnalyticSupport.recordEvent(
            eventName = userEvent.nameEvent,
            label = permission,
            value = value
        )
    }

    fun recordScreenView(activity: Activity?, screenName: String) {
        FirebaseAnalyticSupport.recordScreen(activity, screenName)
    }

    fun recordFailConstructViewModel(event: EventData, fromComponent: String) {
        FirebaseAnalyticSupport.recordEvent(event.nameEvent + fromComponent)
    }
}
