package com.example.claptofindphone.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.claptofindphone.presenter.ClapToFindApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceSupplier @Inject constructor(private val sharedPreferencesInstance: SharedPreferences) {
    companion object {
        const val KEY_FILE = "clap_to_find_shared_preference"
        const val KEY_IS_FIRST_TIME_USE = "KEY_IS_FIRST_TIME_USE"
        const val KEY_ENABLE_SHOW_NOTIFICATION = "KEY_ENABLE_SHOW_NOTIFICATION"
        const val KEY_ENABLE_UPDATE_THEME_AS_SYSTEM = "KEY_ENABLE_UPDATE_THEME_AS_SYSTEM"
        const val KEY_IS_PERMISSION_RECORD_GRANTED = "KEY_IS_PERMISSION_RECORD_GRANTED"
        const val KEY_IS_FLASH_ENABLE = "KEY_IS_FLASH_ENABLE"
        const val KEY_IS_VIBRATION_ENABLE = "KEY_IS_VIBRATION_ENABLE"
        const val KEY_IS_PLAY_SOUND_ENABLE = "KEY_IS_PLAY_SOUND_ENABLE"
        const val KEY_VOLUME_VALUE = "KEY_VOLUME_VALUE"
        const val KEY_DURATION_VALUE = "KEY_DURATION_VALUE"
        const val KEY_SOUND_TYPE_VALUE = "KEY_SOUND_TYPE_VALUE"
        const val KEY_SENSITIVITY_VALUE = "KEY_SENSITIVITY"
        const val KEY_IS_ENABLE_LIGHT_UP_SCREEN = "KEY_SHOULD_LIGHT_UP_SCREEN"
        const val KEY_FLASH_TYPE = "KEY_FLASH_TYPE"
        const val KEY_VIBRATION_TYPE = "KEY_VIBRATION_MODE"
        const val KEY_IS_CLAP_FIND_PHONE_ACTIVATED = "KEY_IS_CLAP_FIND_PHONE_ACTIVATED"
        const val LANGUAGE_UNIQUE_CODE = "LANGUAGE_UNIQUE_CODE"
        const val ON_BOARDING = "ON_BOARDING"
        internal const val IS_PRO_USER = "IS_PRO_USER"
        private const val IS_FIRST_OPEN = "IS_FIRST_OPEN"
    }

    fun isFirstTimeUser() = sharedPreferencesInstance.getBoolean(KEY_IS_FIRST_TIME_USE, true)
    fun setFirstTimeUser(isFirstTimeUser: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_FIRST_TIME_USE, isFirstTimeUser).apply()
    }

    fun isFindPhoneActivated() = sharedPreferencesInstance.getBoolean(KEY_IS_CLAP_FIND_PHONE_ACTIVATED, false)
    fun setFindPhoneActivated(isFirstTimeUser: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_CLAP_FIND_PHONE_ACTIVATED, isFirstTimeUser).apply()
    }

    fun isShouldLightUpScreen() = sharedPreferencesInstance.getBoolean(KEY_IS_ENABLE_LIGHT_UP_SCREEN, true)
    fun setShouldLightUpScreen(isFirstTimeUser: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_ENABLE_LIGHT_UP_SCREEN, isFirstTimeUser).apply()
    }

    fun isShouldTurnOnFlash() = sharedPreferencesInstance.getBoolean(KEY_IS_FLASH_ENABLE, true)

    fun setShouldTurnOnFlash(shouldTurnOnFlash: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_FLASH_ENABLE, shouldTurnOnFlash).apply()
    }

    fun isShouldVibration() = sharedPreferencesInstance.getBoolean(KEY_IS_VIBRATION_ENABLE, true)

    fun setShouldVibration(shouldVibration: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_VIBRATION_ENABLE, shouldVibration).apply()
    }

    fun getSoundType() = sharedPreferencesInstance.getInt(KEY_SOUND_TYPE_VALUE, 0)
    fun setSoundType(soundType: Int) {
        sharedPreferencesInstance.edit().putInt(KEY_SOUND_TYPE_VALUE, soundType).apply()
    }

    fun getSensitivity() = sharedPreferencesInstance.getInt(KEY_SENSITIVITY_VALUE, 90)
    fun setSensitivity(sensitivity: Int) {
        sharedPreferencesInstance.edit().putInt(KEY_SENSITIVITY_VALUE, sensitivity).apply()
    }

    fun getFlashMode() = sharedPreferencesInstance.getInt(KEY_FLASH_TYPE, 0)
    fun setFlashMode(flashMode: Int) {
        sharedPreferencesInstance.edit().putInt(KEY_FLASH_TYPE, flashMode).apply()
    }

    fun getVibrationMode() = sharedPreferencesInstance.getInt(KEY_VIBRATION_TYPE, 0)
    fun setVibrationMode(vibrationMode: Int) {
        sharedPreferencesInstance.edit().putInt(KEY_VIBRATION_TYPE, vibrationMode).apply()
    }

    fun isShouldPlaySound() = sharedPreferencesInstance.getBoolean(KEY_IS_PLAY_SOUND_ENABLE, true)

    fun setShouldPlaySound(shouldVibration: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_PLAY_SOUND_ENABLE, shouldVibration).apply()
    }

    fun getVolume() = sharedPreferencesInstance.getInt(KEY_VOLUME_VALUE, 100)

    fun setVolume(volume: Int) {
        sharedPreferencesInstance.edit().putInt(KEY_VOLUME_VALUE, volume).apply()
    }

    fun getDuration() = sharedPreferencesInstance.getInt(KEY_DURATION_VALUE, 15)

    fun setDuration(volume: Int) {
        sharedPreferencesInstance.edit().putInt(KEY_DURATION_VALUE, volume).apply()
    }

    fun isRecordPermissionGranted() =
        sharedPreferencesInstance.getBoolean(KEY_IS_PERMISSION_RECORD_GRANTED, false)

    fun setIsRecordPermissionGranted(isGranted: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_IS_PERMISSION_RECORD_GRANTED, isGranted).apply()
    }

    fun setAllowShowNotification(isAllow: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_ENABLE_SHOW_NOTIFICATION, isAllow).apply()
    }

    fun getShowNotificationPermission(): Boolean {
        return sharedPreferencesInstance.getBoolean(KEY_ENABLE_SHOW_NOTIFICATION, false)
    }

    fun shouldChangeThemeAsSystem(isAllow: Boolean) {
        sharedPreferencesInstance.edit().putBoolean(KEY_ENABLE_UPDATE_THEME_AS_SYSTEM, isAllow).apply()
    }

    fun getShouldChangeThemeAsSystem(): Boolean {
        return sharedPreferencesInstance.getBoolean(KEY_ENABLE_UPDATE_THEME_AS_SYSTEM, false)
    }

    fun getBooleanData( key: String?, defaultValue: Boolean = false): Boolean {
        return sharedPreferencesInstance
            .getBoolean(key, defaultValue)
    }

    private fun getStringData(key: String?): String? {
        return sharedPreferencesInstance.getString(key, null)
    }

    fun saveData(key: String?, value: Boolean) {
        sharedPreferencesInstance
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    private fun saveData(key: String?, value: String?) {
        sharedPreferencesInstance
            .edit()
            .putString(key, value)
            .apply()
    }

    fun deleteData(key: String?) {
        sharedPreferencesInstance.edit().remove(key).apply()
    }

    private val application: Application
        get() = ClapToFindApplication.get()
    var languageCode: String
        get() = getStringData(LANGUAGE_UNIQUE_CODE) ?: ""
        set(value) = saveData(LANGUAGE_UNIQUE_CODE, value)


    suspend fun setProUser(value: Boolean) {
        application.dataStore.edit { setting ->
            setting[booleanPreferencesKey(IS_PRO_USER)] = value
        }
    }

    var isProUser: Boolean = false
        get() = runBlocking {
            application.dataStore.data.map {
                it[booleanPreferencesKey(IS_PRO_USER)] ?: false
            }
                .first()
        }
        private set

    var firstOpenComplete: Boolean
        get() = getBooleanData(IS_FIRST_OPEN, false)
        set(value) = saveData(IS_FIRST_OPEN, value)

}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clap-to-find-myphone-prefs")