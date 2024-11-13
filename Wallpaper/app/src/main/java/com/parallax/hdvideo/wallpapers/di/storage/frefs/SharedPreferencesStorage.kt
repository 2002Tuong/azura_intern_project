package com.parallax.hdvideo.wallpapers.di.storage.frefs

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parallax.hdvideo.wallpapers.data.model.ConfigInfo
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.extension.toHex
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.AppConstants.PreferencesKey.RATE_APP_KEY
import com.parallax.hdvideo.wallpapers.utils.other.AES
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.reflect.KClass


class SharedPreferencesStorage @Inject constructor(@ApplicationContext context: Context,
                                                   @PreferenceInfo val fileName: String): LocalStorage {

    private val sharedPreferencesInstance = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    private val secretDecrypt = fileName.toHex(fileName)

    override fun putString(key: String, value: String?) {
        with(sharedPreferencesInstance.edit()) {
            putString(key, value?.let { AES.enc(it, secretDecrypt) })
            apply()
        }
    }

    override fun getString(key: String): String? {
        val info = sharedPreferencesInstance.getString(key, null) ?: return null
        return AES.dec(info, secretDecrypt)
    }

    override fun remove(key: String) {
        sharedPreferencesInstance.edit().remove(key).apply()
    }

    override fun get()= sharedPreferencesInstance

    override var authorization: String?
        get() = getString("authorization")
        set(value) { putString("authorization", value)}


    override fun <T: Any> putData(key: String, t: T?) {
        val info = if (t != null) Gson().toJson(t) else null
        putString(key, info)
    }

    override fun <T: Any> getData(key: String): T? {
        val info = getString(key) ?: return null
        try {
            return Gson().fromJson(info, object : TypeToken<T>(){}.type)
        } catch (e: Exception) {}
        return null
    }

    override fun <T : Any> getData(key: String, clazz: KClass<T>): T? {
        val info = getString(key) ?: return null
        try {
            return Gson().fromJson(info, clazz.java)
        } catch (e: Exception) {}
        return null
    }

    fun <T: Any> getData(key: String, type: TypeToken<T>): T? {
        val info = getString(key) ?: return null
        try {
            return Gson().fromJson(info, type.type)
        } catch (e: Exception) {}
        return null
    }


    override var sex: String
        get() = getString(AppConstants.PreferencesKey.KEY_CHOOSE_SEX) ?: AppConstants.SEX_MALE
        set(value) {
            putString(AppConstants.PreferencesKey.KEY_CHOOSE_SEX, value)
        }

    override var sexOrNull: String?
        get() = getString(AppConstants.PreferencesKey.KEY_CHOOSE_SEX)
        set(value) {
            putString(AppConstants.PreferencesKey.KEY_CHOOSE_SEX, value)
        }


    override var isOnNotification: Boolean
        get() = getData(AppConstants.PreferencesKey.ON_NOTIFY, Boolean::class) ?: true
        set(value) {
            putData(AppConstants.PreferencesKey.ON_NOTIFY, value)
        }

    override var countryName: String
        get() = getString(AppConstants.PreferencesKey.COUNTRY_KEY) ?: RemoteConfig.DEFAULT_LANGUAGE
        set(value) { putString(AppConstants.PreferencesKey.COUNTRY_KEY, value)}

    override var playlistCurIndex: Int
        get() = getData(AppConstants.PreferencesKey.CURRENT_WALLPAPER_PLAYLIST_INDEX, Int::class)?: 0
        set(value) { putData(AppConstants.PreferencesKey.CURRENT_WALLPAPER_PLAYLIST_INDEX, value) }

    override var wallpaperChangeDelayTimeInMin: Int
        get() = getData(AppConstants.PreferencesKey.WALLPAPER_CHANGE_DELAY_TIME, Int::class) ?: 15
        set(value) {
            putData(AppConstants.PreferencesKey.WALLPAPER_CHANGE_DELAY_TIME, value)
        }

    override var shouldAutoChangeWallpaper: Boolean
        get() = getData(AppConstants.PreferencesKey.KEY_ON_AUTO_CHANGE_WALLPAPER, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.KEY_ON_AUTO_CHANGE_WALLPAPER, value)
        }

    override var askedViewDownloaded: Boolean
        get() = getData(AppConstants.PreferencesKey.SET_WALL_REPEAT_ASK_VIEW_DOWNLOADED, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.SET_WALL_REPEAT_ASK_VIEW_DOWNLOADED, value)
        }

    override var isFirstTimeIntroImageDisplayed: Boolean
        get() = getData(AppConstants.PreferencesKey.FIRST_TIME_INTRO_IMAGE_DISPLAYED, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.FIRST_TIME_INTRO_IMAGE_DISPLAYED, value)
        }

    override var isFirstTimeIntro4DDisplayed: Boolean
        get() = getData(AppConstants.PreferencesKey.FIRST_TIME_INTRO_4D_DISPLAYED, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.FIRST_TIME_INTRO_4D_DISPLAYED, value)
        }

    override var isAutoPlayVideo: Boolean
        get() = getData(AppConstants.PreferencesKey.AUTO_PLAY_VIDEO, Boolean::class) ?: true
        set(value) {
            putData(AppConstants.PreferencesKey.AUTO_PLAY_VIDEO, value)
        }

    override var contextChangedWallpaperIndex: Int
        get() = getData(AppConstants.PreferencesKey.SCENARIO_CHANGED_WALLPAPER_INDEX, Int::class) ?: 0
        set(value) {
            putData(AppConstants.PreferencesKey.SCENARIO_CHANGED_WALLPAPER_INDEX, value)
        }

    //"0,0,0,0,0" - save the current content index of 5 notification type, check DWT-537
    override var indexContentForNotificationType: IntArray
        get() {
            return getData(AppConstants.PreferencesKey.INDEX_OF_CONTENT_NOTIFICATION_TYPE, IntArray::class) ?: intArrayOf(0, 0, 0, 0, 0)
        }
        set(value) {
            putData(AppConstants.PreferencesKey.INDEX_OF_CONTENT_NOTIFICATION_TYPE, value)
        }


    override var bestStoreVideoDownload: String?
        get() = getString("KEY_VIDEO_BEST_STORAGE_DOWNLOAD")
        set(value) { putString("KEY_VIDEO_BEST_STORAGE_DOWNLOAD", value)}

    override var pageNumberVideo: Int
        get() {
            val number = getData(AppConstants.PreferencesKey.DEFAULT_ONLINE_VIDEO_PAGE_ID, Int::class) ?: 1
            return if (number <= 0) 1 else number
        }
        set(value) {
            putData(AppConstants.PreferencesKey.DEFAULT_ONLINE_VIDEO_PAGE_ID, value)
        }

    override var pageNumberWall: Int
        get() {
            val number = getData(AppConstants.PreferencesKey.DEFAULT_ONLINE_WALL_PAGE_ID, Int::class) ?: 1
            return if (number <= 0) 1 else number
        }
        set(value) {
            putData(AppConstants.PreferencesKey.DEFAULT_ONLINE_WALL_PAGE_ID, value)
        }

    override var pageNumberParallaxTab4D: Int
        get() {
            val number = getData(AppConstants.PreferencesKey.DEFAULT_PAGE_ID_TAB_4D, Int::class) ?: 1
            return if (number <= 0) 1 else number
        }
        set(value) {
            putData(AppConstants.PreferencesKey.DEFAULT_PAGE_ID_TAB_4D, value)
        }

    override var pageNumberParallaxTabHome: Int
        get() {
            val number = getData(AppConstants.PreferencesKey.DEFAULT_PAGE_ID_TAB_HOME, Int::class) ?: 1
            return if (number <= 0) 1 else number
        }
        set(value) {
            putData(AppConstants.PreferencesKey.DEFAULT_PAGE_ID_TAB_HOME, value)
        }

    override var isSoundingVideo: Boolean
        get() = getData(AppConstants.PreferencesKey.KEY_SOUND_VIDEO, Boolean::class) ?: true
        set(value) {
            putData(AppConstants.PreferencesKey.KEY_SOUND_VIDEO, value)
        }

    override var didNotifyTopTenDevice: Boolean
        get() = getData(AppConstants.PreferencesKey.KEY_NOTIFY_TOP_TEN_DEVICE, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.KEY_NOTIFY_TOP_TEN_DEVICE, value)
        }

    override var configApp: ConfigInfo?
        get() = getData(AppConstants.PreferencesKey.CONFIG_DATA_APP, ConfigInfo::class)
        set(value) {
            value?.commonData?.let {
                if (it.activeServer)
                    putData(AppConstants.PreferencesKey.CONFIG_DATA_APP, value)
            }
        }

    override var openAppCount: Int
        get() = getData(AppConstants.PreferencesKey.COUNT_OPEN_APP, Int::class) ?: 0
        set(value) {
            putData(AppConstants.PreferencesKey.COUNT_OPEN_APP, value)
        }

    override var videoWallpaperUnsupportedDialogDisplayed: Boolean
        get() = getData(AppConstants.PreferencesKey.KEY_VIDEO_WALLPAPER_UNSUPPORTED, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.KEY_VIDEO_WALLPAPER_UNSUPPORTED, value)
        }

    override var accountName: String?
        get() = getString(AppConstants.PreferencesKey.USER_NAME)
        set(value) {
            putString(AppConstants.PreferencesKey.USER_NAME, value)
        }


    override var hadTouchedCollection: Boolean
        get() = getData(AppConstants.PreferencesKey.HAS_TOUCHED_COLLECTION, Boolean::class) ?: false
        set(value) {
            putData(AppConstants.PreferencesKey.HAS_TOUCHED_COLLECTION, value)
        }

    override var ratingApp: Boolean
        get() = getData(RATE_APP_KEY, Boolean::class) ?: false
        set(value) {
            putData(RATE_APP_KEY, value)
        }

    override var countRating: Int
        get() = getData("COUNT_RATINGS", Int::class) ?: 0
        set(value) {
            putData("COUNT_RATINGS", value)
        }

    override var openedLastTime: Long
        get() = getData("OPENED_LAST_TIME", Long::class) ?: 0
        set(value) {
            putData("OPENED_LAST_TIME", value)
        }

    override var lastDayOpenApp: String
        get() = getData(AppConstants.PreferencesKey.LAST_DAY_OPEN_APP, String::class) ?: ""
        set(value) {
            putData(AppConstants.PreferencesKey.LAST_DAY_OPEN_APP, value)
        }

    override var aspectRatio: Float?
        get() = getData("ASPECT_RATIO", Float::class)
        set(value) {
            putData("ASPECT_RATIO", value)
        }

    override var isVipUser: Boolean
        get() = getData("isVipMember", Boolean::class) ?: false
        set(value) {
            putData("isVipMember", value)
        }


    override var curListTopics: Set<String>
        get() {
            return sharedPreferencesInstance.getStringSet("currentListTopics", HashSet<String>()) ?: HashSet<String>()
        }
        set(value) {
            sharedPreferencesInstance.edit().putStringSet("currentListTopics", value).apply()
        }

    override var didCheckIsSupportVideoWall: Boolean
        get() = getData("didCheckIsSupportVideoWall", Boolean::class) ?: false
        set(value) {
            putData("didCheckIsSupportVideoWall", value)
        }

    override var curSloganPosition: Int
        get() = getData(AppConstants.PreferencesKey.CURRENT_POS_SLOGAN, Int::class) ?: 0
        set(value) {
            putData(AppConstants.PreferencesKey.CURRENT_POS_SLOGAN, value)
        }

    override var curVideoIntro: Int
        get() = getData(AppConstants.PreferencesKey.CURRENT_POS_VIDEO_INTRO, Int::class) ?: 0
        set(value) {
            putData(AppConstants.PreferencesKey.CURRENT_POS_VIDEO_INTRO, value)
        }

    override var lastDayModifyVideoIntro: Long
        get() = getData(AppConstants.PreferencesKey.LAST_DAY_CHANGE_VIDEO_INTRO, Long::class) ?: 0
        set(value) {
            putData(AppConstants.PreferencesKey.LAST_DAY_CHANGE_VIDEO_INTRO, value)
        }

    override var lastDayModifySlogan: Long
        get() = getData(AppConstants.PreferencesKey.LAST_DAY_CHANGE_SLOGAN, Long::class) ?: 0
        set(value) {
            putData(AppConstants.PreferencesKey.LAST_DAY_CHANGE_SLOGAN, value)
        }
    override var lastName: String?
        get() = getString(AppConstants.PreferencesKey.LAST_NAME)
        set(value) {
            putString(AppConstants.PreferencesKey.LAST_NAME, value)
        }

    // Using preview
    override var parallaxInfo: WallpaperModel?
        get() = getData(AppConstants.PreferencesKey.PARALLAX_DATA_KEY, WallpaperModel::class)
        set(value) = putData(AppConstants.PreferencesKey.PARALLAX_DATA_KEY, value)

    //Using when user set wallpaper success
    override var parallaxInfoChange: WallpaperModel?
        get() = getData(AppConstants.PreferencesKey.PARALLAX_DATA_CHANGE, WallpaperModel::class)
        set(value) = putData(AppConstants.PreferencesKey.PARALLAX_DATA_CHANGE, value)


    override var languageCode: String
        get() = getString(AppConstants.PreferencesKey.LANGUAGE_CODE) ?: ""
        set(value) = putData(AppConstants.PreferencesKey.LANGUAGE_CODE, value)

    override var firstOpenComplete: Boolean
        get() = getData(AppConstants.PreferencesKey.IS_COMPLETE, Boolean::class) ?: false
        set(value) = putData(AppConstants.PreferencesKey.IS_COMPLETE, value)
}

