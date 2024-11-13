package com.parallax.hdvideo.wallpapers.di.storage.frefs

import android.content.SharedPreferences
import com.parallax.hdvideo.wallpapers.data.model.ConfigInfo
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import kotlin.reflect.KClass

interface LocalStorage {

    fun putString(key: String, value: String?)
    fun getString(key: String): String?
    fun remove(key: String)
    fun get(): SharedPreferences

    var authorization: String?

    fun <T: Any> putData(key: String, t: T?)

    fun <T : Any> getData(key: String): T?

    fun <T : Any> getData(key: String, clazz: KClass<T>): T?

//    fun putGender(isMale: Boolean)
//
//    fun getGender(): String

    var sex : String

    var sexOrNull : String?

    var isOnNotification: Boolean

    var countryName: String

    var playlistCurIndex: Int

    var isFirstTimeIntroImageDisplayed : Boolean

    var isFirstTimeIntro4DDisplayed: Boolean

    // minute
    var wallpaperChangeDelayTimeInMin: Int

    var shouldAutoChangeWallpaper: Boolean

    var askedViewDownloaded: Boolean

    var isAutoPlayVideo: Boolean

    var contextChangedWallpaperIndex: Int

    var indexContentForNotificationType: IntArray

    var bestStoreVideoDownload: String?

    var pageNumberVideo: Int

    var pageNumberWall: Int

    var pageNumberParallaxTab4D: Int

    var pageNumberParallaxTabHome: Int

    var isSoundingVideo: Boolean

    var configApp: ConfigInfo?

    var openAppCount: Int

    var didNotifyTopTenDevice: Boolean

    var videoWallpaperUnsupportedDialogDisplayed: Boolean

    var accountName : String?

    var hadTouchedCollection: Boolean

    var ratingApp: Boolean

    var countRating: Int
    var openedLastTime: Long

    var lastDayOpenApp: String

    var aspectRatio: Float?

    val firstOpen: Boolean
        get() = openAppCount < 2

    var isVipUser: Boolean

    var curListTopics: Set<String>

    var didCheckIsSupportVideoWall: Boolean

    var curSloganPosition: Int

    var curVideoIntro: Int

    var lastDayModifyVideoIntro: Long

    var lastDayModifySlogan: Long

    var lastName: String?

    var parallaxInfo: WallpaperModel?

    var parallaxInfoChange: WallpaperModel?

    var languageCode: String

    var firstOpenComplete: Boolean

}