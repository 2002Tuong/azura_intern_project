package com.example.videoart.batterychargeranimation.data.local

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.videoart.batterychargeranimation.App
import com.example.videoart.batterychargeranimation.model.ClosingMethod
import com.example.videoart.batterychargeranimation.model.Duration

object PreferenceUtils {
    private val PREF_APP = "chargeranimation"
    const val STORAGE_PERMISSION_GRANTED = "STORAGE_PERMISSION_GRANTED"
    const val DISPLAY_OVER_APP_GRANTED = "DISPLAY_OVER_APP_GRANTED"
    const val IS_FIRST_OPEN_COMPLETE = "IS_FIRST_OPEN_COMPLETE"
    const val LANGUAGE_CODE = "LANGUAGE_CODE"
    const val DURATION = "DURATION"
    const val CLOSING_METHOD ="CLOSING_METHOD"
    const val SOUND_ACTIVE = "SOUND_ACTIVE"
    const val COUNT_SET_SUCCESS = "COUNT_SET_SUCCESS"
    private val application: App
        get() = App.instance
    val storagePermissionGranted: Boolean
        get() = getBooleanData(application, STORAGE_PERMISSION_GRANTED, false)
    val displayOverAppGranted: Boolean
        get() = getBooleanData(application, DISPLAY_OVER_APP_GRANTED, false)
    var isFirstOpenComplete: Boolean
        get() = getBooleanData(application, IS_FIRST_OPEN_COMPLETE, false)
        set(value) {
            saveData(application, IS_FIRST_OPEN_COMPLETE, value)
        }
    var langCode: String
        get() = getStringData(application, LANGUAGE_CODE) ?: ""
        set(value) = saveData(application, LANGUAGE_CODE, value)

    val durationLiveData: MutableLiveData<Duration> = MutableLiveData(duration)
    var duration: Duration
        get() {
            val ordinal = getIntData(application, DURATION, Duration.DURATION_5.ordinal)
            return when(ordinal) {
                Duration.DURATION_5.ordinal -> Duration.DURATION_5
                Duration.DURATION_10.ordinal -> Duration.DURATION_10
                Duration.DURATION_30.ordinal -> Duration.DURATION_30
                Duration.DURATION_ALWAYS.ordinal -> Duration.DURATION_ALWAYS
                else -> Duration.DURATION_5
            }
        }
        set(value) {
            durationLiveData.postValue(value)
            saveData(application, DURATION, value.ordinal)
        }

    val closingMethodLiveData: MutableLiveData<ClosingMethod> = MutableLiveData(closingMethod)
    var closingMethod: ClosingMethod
        get() {
            val ordinal = getIntData(application, CLOSING_METHOD, ClosingMethod.SINGLE_TAP.ordinal)
            return when(ordinal) {
                ClosingMethod.SINGLE_TAP.ordinal -> ClosingMethod.SINGLE_TAP
                ClosingMethod.DOUBLE_TAP.ordinal -> ClosingMethod.DOUBLE_TAP
                else ->  ClosingMethod.SINGLE_TAP
            }
        }
        set(value) {
            closingMethodLiveData.postValue(value)
            saveData(application, CLOSING_METHOD, value.ordinal)
        }

    var soundActive: Boolean
        get() = getBooleanData(application, SOUND_ACTIVE, true)
        set(value) = saveData(application, SOUND_ACTIVE, value)

    var countSetAnimation: Int
        get() = getIntData(application, COUNT_SET_SUCCESS, 0)
        set(value) = saveData(application, COUNT_SET_SUCCESS, value)

    fun getIntData(context: Context, key: String?, defaultValue: Int): Int {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getInt(key, defaultValue)
    }

    fun getLongData(context: Context, key: String?): Long {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getLong(key, 0L)
    }

    fun getBooleanData(context: Context, key: String?, defaultValue: Boolean = false): Boolean {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    fun getStringData(context: Context, key: String?): String? {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString(key, null)
    }

    fun saveData(context: Context, key: String?, value: Boolean) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun saveData(context: Context, key: String?, value: String?) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString(key, value)
            .apply()
    }

    fun saveData(context: Context, key: String?, value: Long) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putLong(key, value)
            .apply()
    }

    fun saveData(context: Context, key: String?, value: Int) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putInt(key, value)
            .apply()
    }

    fun setStoragePermission(isGranted: Boolean) {
        saveData(application, STORAGE_PERMISSION_GRANTED, isGranted)
    }

    fun setDisplayOverPermission(isGranted: Boolean) {
        saveData(application, DISPLAY_OVER_APP_GRANTED, isGranted)
    }

}