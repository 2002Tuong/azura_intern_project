package com.parallax.hdvideo.wallpapers.utils

import com.bumptech.glide.load.model.LazyHeaders
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.di.network.NetworkModule
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig


object AppConstants {

    const val EXPIRATION = "res_expired"
    val HEX_LOWERCASE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    const val PATTERN_DATE_TIME = "HH:mm dd/MM/yyyy"
    const val PATTERN_DATE = "dd/MM/yyyy"
    const val SEARCH_IGNORE_CHAR = "[+^\\\\\"*&%$#@!~=;:<>/?.()]"
    const val EMOJIS_REGEX = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]"
    const val BASE_URL = "https://www.google.com/"
    const val FILE_NAME_POLICY = "policy"
    const val FILE_NAME_IMAGE_WALLPAPER = "image.json"
    const val FILE_NAME_VIDEO_WALLPAPER = "video.json"
    var isActiveLogcat = true
    const val NEW_PACKAGE = "update_pkg_name_key"
    const val LATEST_VERSION_APP = "latest_version_app"
    const val SEX_MALE = "male"
    const val SEX_FEMALE = "female"
    const val SEX_UNSET = "unset"
    const val PLUS = "＋"
    const val MAX_COUNT_PLAYLIST_STRING = "9+"
    const val MINUS = "－"
    const val KEY_INTENT_SHARE = "SHARE_INTENT"
    const val DISTANCE_VIDEOS_IN_HOME_SCREEN = 2
    //  height / width
    const val RATIO_IMAGE = 1.78f
    const val EMPTY_STRING = ""
    const val KEY_INTENT_VIDEO_WITH_SOUND = "KEY_INTENT_VIDEO_WITH_SOUND"
    const val TIMEOUT_REQUEST_IMAGE = 30_000

    const val IMAGE_BACKGROUND_NAME = "bg.jpg"
    const val IMAGE_LAYER = "wall_"

    val imageHeader = LazyHeaders.Builder().let {
        it.addHeader("User-Agent", NetworkModule.userAgent)
        it.addHeader("Token", RemoteConfig.getToken(WallpaperApp.instance))
        it.addHeader("AppID", RemoteConfig.appId)
        it.build()
    }

    val TOP_10_DEVICES = mutableMapOf(
        "hinhnensamsunggalaxya51" to "Samsung A51",
        "hinhnensamsunggalaxya21" to "Samsung A21",
        "hinhnensamsunggalaxya20e" to "Samsung A20e",
        "hinhnensamsunggalaxya71" to "Samsung A71",
        "hinhnensamsunggalaxya11" to "Samsung A11",
        "hinhnensamsunggalaxya01" to "Samsung A01",
        "hinhnensamsunggalaxyj7" to "Samsung J7",
        "hinhnensamsunggalaxyj2" to "Samsung J2",
        "hinhnenmotorolamotoe5" to "Moto E5",
        "hinhnenvivo1906y11" to "Vivo Y11"
    )

    public object PreferencesKey {
        const val CONFIG_DATA_APP = "CONFIG_DATA_APP"
        const val COUNTRY_KEY = "setting_country"
        const val BEST_HASH_TAGS_IMAGE = "best_hash_tags_image"
        const val BEST_HASH_TAGS_VIDEO = "best_hash_tags_video"

        const val DEFAULT_ONLINE_WALL_PAGE_ID = "default_wall_page_id"
        const val DEFAULT_ONLINE_VIDEO_PAGE_ID = "default_video_page_id"
        const val DEFAULT_PAGE_ID_TAB_4D = "default_page_id_tab_4d"
        const val DEFAULT_PAGE_ID_TAB_HOME = "default_page_id_tab_home"

        const val RATE_APP_KEY = "rate_app_key"
        const val ON_NOTIFY = "on_notify"
        const val LAST_TIME_NOTIFY = "last_time_notify"

        const val KEY_SAVE_VIDEO_PATH = "key_save_video_path"

        const val KEY_CHOOSE_SEX = "key_choose_sex"
        const val KEY_VIDEO_BEST_STORAGE = "key_video_best_storage"
        const val KEY_VIDEO_BEST_STORAGE_TMP = "key_video_best_storage_tmp"
        const val KEY_ON_AUTO_CHANGE_WALLPAPER = "key_on_auto_change"

        //Set/Download Wallpaper keys
        const val SET_WALL_REPEAT_ASK_VIEW_DOWNLOADED = "repeat_ask_view_downloaded_wall"

        const val CURRENT_WALLPAPER_PLAYLIST_INDEX = "currentPlaylistIndex"

        const val WALLPAPER_CHANGE_DELAY_TIME = "wcDelayTime"

        const val AUTO_CHANGE_WALLPAPER_SCREEN_TYPE = "wallpaper_screen_type"

        const val LAST_INDEX_NOTIFICATION_HASH_TAGS = "index_notification_hashtags"

        const val AUTO_PLAY_VIDEO = "auto_play_video"

        const val FIRST_TIME_INTRO_IMAGE_DISPLAYED = "first_time_intro"

        const val FIRST_TIME_INTRO_4D_DISPLAYED = "first_time_intro_4D"

        const val COUNT_OPEN_APP = "count_open_app"

        const val SCENARIO_CHANGED_WALLPAPER_INDEX = "scenario_changed_wallpaper_index"

        const val INDEX_OF_CONTENT_NOTIFICATION_TYPE = "index_of_content_notification_type"

        const val LATEST_NOTIFICATION_ONLINE_ID = "latest_notification_online_id"

        const val LATEST_NOTIFICATION_ONLINE_DATA = "latest_notification_online_data"

        const val KEY_SOUND_VIDEO = "SOUND_VIDEO"

        const val KEY_NOTIFY_TOP_TEN_DEVICE = "TOP_TEN_DEVICE"

        const val KEY_DEVICE_HASH_TAG = "device_hastag"

        const val KEY_VIDEO_WALLPAPER_UNSUPPORTED = "videoWallpaperUnsupported"

        const val USER_NAME = "username"

        const val HAS_TOUCHED_COLLECTION = "HAS_TOUCHED_COLLECTION"

        const val LAST_DAY_OPEN_APP = "last_day_open_app"

        const val CURRENT_POS_SLOGAN = "current_pos_slogan"

        const val CURRENT_POS_VIDEO_INTRO = "current_pos_video_intro"

        const val LAST_DAY_CHANGE_VIDEO_INTRO = "last_day_change_video_intro"

        const val LAST_DAY_CHANGE_SLOGAN = "last_day_change_slogan"

        const val LAST_NAME = "last_name"

        const val PARALLAX_DATA_KEY = "parallaxData"

        const val PARALLAX_DATA_CHANGE = "parallax_data_change"

        const val LANGUAGE_CODE = "language_code"

        const val IS_COMPLETE = "is_complete"
    }

}