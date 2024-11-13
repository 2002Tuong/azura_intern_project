package com.screentheme.app.utils.helpers

import android.content.Context
import android.content.SharedPreferences

object SharePreferenceHelper {
    private const val PREFS_NAME = "CallThemePrefs"

    // Get SharedPreferences instance
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save a value of any type in SharedPreferences
    private fun <T> saveValue(context: Context, key: String, value: T) {
        val editor = getSharedPreferences(context).edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Boolean -> editor.putBoolean(key, value)
            else -> throw IllegalArgumentException("Unsupported data type")
        }
        editor.apply()
    }

    // Retrieve a value of any type from SharedPreferences
    private inline fun <reified T> getValue(context: Context, key: String, defaultValue: T): T {
        val sharedPreferences = getSharedPreferences(context)
        return when (T::class) {
            String::class -> sharedPreferences.getString(key, defaultValue as? String) as T? ?: defaultValue
            Int::class -> sharedPreferences.getInt(key, defaultValue as? Int ?: 0) as T
            Long::class -> sharedPreferences.getLong(key, defaultValue as? Long ?: 0L) as T
            Float::class -> sharedPreferences.getFloat(key, defaultValue as? Float ?: 0.0f) as T
            Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    // Save a Boolean value in SharedPreferences
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        saveValue(context, key, value)
    }

    // Retrieve a Boolean value from SharedPreferences
    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        return getValue(context, key, defaultValue)
    }

    // Save a String value in SharedPreferences
    fun saveString(context: Context, key: String, value: String) {
        saveValue(context, key, value)
    }

    // Retrieve a String value from SharedPreferences
    fun getString(context: Context, key: String, defaultValue: String): String {
        return getValue(context, key, defaultValue)
    }

    // Save an Int value in SharedPreferences
    fun saveInt(context: Context, key: String, value: Int) {
        saveValue(context, key, value)
    }

    // Retrieve an Int value from SharedPreferences
    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        return getValue(context, key, defaultValue)
    }

    const val KEY_ALREADY_WENT_THROUGH_INFO = "KEY_ALREADY_WENT_THROUGH_INFO"
    const val KEY_ALREADY_SET_LANGUAGE = "KEY_ALREADY_SET_LANGUAGE"
    const val KEY_YOUR_AVATARS = "KEY_YOUR_AVATARS"
    const val KEY_YOUR_BACKGROUNDS = "KEY_YOUR_BACKGROUNDS"
    const val KEY_APP_LANGUAGE = "KEY_APP_LANGUAGE"
    const val KEY_IS_PURCHASED = "KEY_IS_PURCHASED"
    const val KEY_CUSTOM_THEME_ITEM_CLICKED = "KEY_CUSTOM_THEME_ITEM_CLICKED"
}