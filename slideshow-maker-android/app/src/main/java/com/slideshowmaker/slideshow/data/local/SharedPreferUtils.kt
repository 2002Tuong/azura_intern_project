package com.slideshowmaker.slideshow.data.local

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object SharedPreferUtils {
    private val PREF_APP_KEY = "snapslide"
    internal const val PRO_USER_KEY = "PRO_USER"
    internal const val SUBSCRIPTION_TYPE_KEY = "SUBSCRIPTION_TYPE"
    internal const val SUBSCRIPTION_EXPIRED_AT_KEY = "SUBSCRIPTION_EXPIRED_AT"
    internal const val IS_LIFETIME_KEY = "IS_LIFETIME"
    internal const val REFERRER_KEY = "REFERRER"
    internal const val REGISTERED_DEVICE_TOKEN_KEY = "REGISTERED_DEVICE_TOKEN"
    private const val FIRST_LAUNCH_KEY = "first_launch"
    private const val CLICK_SAVE_SLIDESHOW_FIRST_TIME_KEY = "CLICK_SAVE_SLIDESHOW_FIRST_TIME"
    private const val EXPORTING_SLIDESHOW_FIRST_TIME_KEY = "EXPORTING_SLIDESHOW_FIRST_TIME"
    private const val SHARE_SLIDESHOW_SUCCESS_FIRST_TIME_KEY = "SHARE_SLIDESHOW_SUCCESS_FIRST_TIME"
    private const val SAVE_SLIDESHOW_SUCCESS_FIRST_TIME_KEY = "SAVE_SLIDESHOW_SUCCESS_FIRST_TIME"
    private const val OPEN_SLIDESHOW_EDITOR_FIRST_TIME_KEY = "OPEN_SLIDESHOW_EDITOR_FIRST_TIME"
    private const val CLICK_SLIDESHOW_FIRST_TIME_KEY = "CLICK_SLIDESHOW_FIRST_TIME"
    private const val SHOW_APP_OPEN_ADS_FIRST_TIME_KEY = "SHOW_APP_OPEN_ADS_FIRST_TIME"
    private const val GET_REMOTE_CONFIG_SUCCESS_FIRST_TIME_KEY = "GET_REMOTE_CONFIG_SUCCESS_FIRST_TIME"
    private const val OPEN_MAIN_APP_FIRST_TIME_KEY = "OPEN_MAIN_APP_FIRST_TIME"
    private const val LANGUAGE_CODE_KEY = "LANGUAGE_CODE"
    private const val FIRST_OPEN_KEY = "FIRST_OPEN"
    private const val ON_BOARDING_KEY = "ON_BOARDING"

    var showOpenMainAdsOnFirstLaunch
        get() = getBooleanData(app, OPEN_MAIN_APP_FIRST_TIME_KEY, true)
        set(value) = saveData(app, OPEN_MAIN_APP_FIRST_TIME_KEY, value)
    var showApsOpenAppOnFirstLaunch
        get() = getBooleanData(app, SHOW_APP_OPEN_ADS_FIRST_TIME_KEY, true)
        set(value) = saveData(app, SHOW_APP_OPEN_ADS_FIRST_TIME_KEY, value)
    var isGetRemoteConfigSuccessFirstTime
        get() = getBooleanData(app, GET_REMOTE_CONFIG_SUCCESS_FIRST_TIME_KEY, true)
        set(value) = saveData(app, GET_REMOTE_CONFIG_SUCCESS_FIRST_TIME_KEY, value)
    var clickSlideShowOnFirstLaunch
        get() = getBooleanData(app, CLICK_SLIDESHOW_FIRST_TIME_KEY, true)
        set(value) = saveData(app, CLICK_SLIDESHOW_FIRST_TIME_KEY, value)
    var openSlideshowEditorOnFirstLaunch
        get() = getBooleanData(app, OPEN_SLIDESHOW_EDITOR_FIRST_TIME_KEY, true)
        set(value) = saveData(app, OPEN_SLIDESHOW_EDITOR_FIRST_TIME_KEY, value)
    var saveSlideshowSuccessOnFirstLaunch
        get() = getBooleanData(app, SAVE_SLIDESHOW_SUCCESS_FIRST_TIME_KEY, true)
        set(value) = saveData(app, SAVE_SLIDESHOW_SUCCESS_FIRST_TIME_KEY, value)
    var shareSlideshowSuccessOnFirstLaunch
        get() = getBooleanData(app, SHARE_SLIDESHOW_SUCCESS_FIRST_TIME_KEY, true)
        set(value) = saveData(app, SHARE_SLIDESHOW_SUCCESS_FIRST_TIME_KEY, value)
    var doesSaveSlideShowFirstTime
        get() = getBooleanData(app, CLICK_SAVE_SLIDESHOW_FIRST_TIME_KEY, true)
        set(value) = saveData(app, CLICK_SAVE_SLIDESHOW_FIRST_TIME_KEY, value)
    var exportSlideshowFirstTime
        get() = getBooleanData(app, EXPORTING_SLIDESHOW_FIRST_TIME_KEY, true)
        set(value) = saveData(app, EXPORTING_SLIDESHOW_FIRST_TIME_KEY, value)

    var isFirstLaunch: Boolean
        get() = getBooleanData(app, FIRST_LAUNCH_KEY, true)
        set(value) = saveData(app, FIRST_LAUNCH_KEY, value)


    var hadRegisteredDeviceToken: Boolean
        get() = getBooleanData(app, REGISTERED_DEVICE_TOKEN_KEY, false)
        set(value) = saveData(app, REGISTERED_DEVICE_TOKEN_KEY, value)

    var countryCode: String? = "VN"

    private val app: Application
        get() = VideoMakerApplication.instance


    var subscriptionType: String
        get() = getStringData(app, SUBSCRIPTION_TYPE_KEY).orEmpty()
        set(value) = saveData(app, SUBSCRIPTION_TYPE_KEY, value)

    var isLifetime: Boolean
        get() = getBooleanData(app, IS_LIFETIME_KEY).orFalse()
        set(value) = saveData(app, IS_LIFETIME_KEY, value)

    var proUser: Boolean = false
        get() = runBlocking {
            app.dataStore.data.map {
                it[booleanPreferencesKey(PRO_USER_KEY)] ?: false
            }
                .first()
        }
        private set


    var subscriptionExpiredAt: Long
        get() = getLongData(app, SUBSCRIPTION_EXPIRED_AT_KEY)
        set(value) = saveData(app, SUBSCRIPTION_EXPIRED_AT_KEY, value)


    var referrer: String?
        get() = getStringData(app, REFERRER_KEY)
        set(value) = saveData(app, REFERRER_KEY, value)

    fun getLongData(context: Context, key: String?): Long {
        return context.getSharedPreferences(PREF_APP_KEY, Context.MODE_PRIVATE).getLong(key, 0L)
    }

    fun getBooleanData(context: Context, key: String?, defaultValue: Boolean = false): Boolean {
        return context.getSharedPreferences(PREF_APP_KEY, Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    fun getStringData(context: Context, key: String?): String? {
        return context.getSharedPreferences(PREF_APP_KEY, Context.MODE_PRIVATE).getString(key, null)
    }

    fun saveData(context: Context, key: String?, value: Boolean) {
        context.getSharedPreferences(PREF_APP_KEY, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun saveData(context: Context, key: String?, value: String?) {
        context.getSharedPreferences(PREF_APP_KEY, Context.MODE_PRIVATE).edit().putString(key, value)
            .apply()
    }

    fun saveData(context: Context, key: String?, value: Long) {
        context.getSharedPreferences(PREF_APP_KEY, Context.MODE_PRIVATE).edit().putLong(key, value)
            .apply()
    }

    suspend fun setProUser(value: Boolean) {
        app.dataStore.edit { setting ->
            setting[booleanPreferencesKey(PRO_USER_KEY)] = value
        }
    }

    suspend fun setFirstOpenComplete(value: Boolean) {
        app.dataStore.edit { setting ->
            setting[booleanPreferencesKey(FIRST_OPEN_KEY)] = value
        }
    }

    fun setLanguageCode(value: String) {
        runBlocking {
            app.dataStore.edit { setting ->
                setting[stringPreferencesKey(LANGUAGE_CODE_KEY)] = value
            }
        }
    }

    val langCode: String
        get() = runBlocking {
            app.dataStore.data.map {
                it[stringPreferencesKey(LANGUAGE_CODE_KEY)] ?: ""
            }
                .first()
        }
    val isFirstOpenComplete: Boolean
        get() = runBlocking {
            app.dataStore.data.map {
                it[booleanPreferencesKey(FIRST_OPEN_KEY)] ?: false
            }
                .first()
        }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "snap-edit-prefs")
