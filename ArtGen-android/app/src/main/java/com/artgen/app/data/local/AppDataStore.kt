package com.artgen.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class AppDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "art_gen_preference")

    var authorizationExpiredAt: Long
        set(value) {
            runBlocking {
                context.dataStore.edit { settings ->
                    settings[AUTHORIZATION_EXPIRED_AT] = value
                }
            }
        }
        get() {
            return runBlocking { context.dataStore.data.first()[AUTHORIZATION_EXPIRED_AT] ?: 0L }
        }

    var authorization: String
        set(value) {
            runBlocking {
                context.dataStore.edit { settings ->
                    settings[AUTHORIZATION] = value
                }
            }
        }
        get() = runBlocking { context.dataStore.data.first()[AUTHORIZATION].orEmpty() }

    val isPurchased: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_PURCHASED] == true }

    val isPurchasedFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences -> preferences[IS_PURCHASED] == true }

    val isLanguageSelected: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_LANGUAGE_SELECTED] == true }

    val isOnboardingShownFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[IS_ONBOARDING_SHOWN] == true
        }

    val isOnboardingShown: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_ONBOARDING_SHOWN] == true }

    val selectedLanguage: String
        get() = runBlocking { context.dataStore.data.first()[SELECTED_LANGUAGE].orEmpty() }

    val selectedPhotoUriFlow: Flow<String>
        get() = context.dataStore.data.map { preferences -> preferences[PHOTO_URI].orEmpty() }

    val selectedStyleIdFlow: Flow<String>
        get() = context.dataStore.data.map { preferences ->
            preferences[SELECTED_ART_STYLE_ID].orEmpty()
        }

    val isRated: Boolean get() = runBlocking { context.dataStore.data.first()[IS_USER_RATED] == true }
    val savePhotoCountFlow: Flow<Int>
        get() = context.dataStore.data.map { preferences ->
            preferences[SAVE_PHOTO_COUNT] ?: 0
        }

    val savePhotoCount: Int
        get() = runBlocking { context.dataStore.data.first()[SAVE_PHOTO_COUNT] ?: 0 }
    val exitAppCountFlow: Flow<Int>
        get() = context.dataStore.data.map { preferences ->
            preferences[EXIT_APP_COUNT] ?: 0
        }
    val exitAppCount: Int
        get() = runBlocking { context.dataStore.data.first()[EXIT_APP_COUNT] ?: 0 }

    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun setLanguageSelected(isSelected: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_LANGUAGE_SELECTED] = isSelected
        }
    }

    suspend fun setOnboardingShown(isOnboardingShown: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_ONBOARDING_SHOWN] = isOnboardingShown
        }
    }

    suspend fun setPurchase(isPurchased: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_PURCHASED] = isPurchased
        }
    }

    suspend fun setSelectedArtStyle(styleId: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_ART_STYLE_ID] = styleId
        }
    }

    suspend fun setPhotoUri(uriString: String) {
        context.dataStore.edit { settings ->
            settings[PHOTO_URI] = uriString
        }
    }

    suspend fun increaseSavePhotoCount() {
        savePhotoCountFlow.collectLatest {
            context.dataStore.edit { settings ->
                settings[SAVE_PHOTO_COUNT] = it + 1
            }
        }
    }

    suspend fun increaseExitAppCount() {
        exitAppCountFlow.collectLatest {
            context.dataStore.edit { settings ->
                settings[EXIT_APP_COUNT] = it + 1
            }
        }
    }

    suspend fun setRatedStatus(isRated: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_USER_RATED] = isRated
        }
    }

    companion object {
        private val IS_LANGUAGE_SELECTED = booleanPreferencesKey("is_language_selected")
        private val IS_PURCHASED = booleanPreferencesKey("is_purchased")
        private val IS_ONBOARDING_SHOWN = booleanPreferencesKey("is_onboarding_shown")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val AUTHORIZATION_EXPIRED_AT = longPreferencesKey("authorization_expired_at")
        private val AUTHORIZATION = stringPreferencesKey("authorization")
        private val SELECTED_ART_STYLE_ID = stringPreferencesKey("selected_art_style_id")
        private val PHOTO_URI = stringPreferencesKey("photo_uri")
        private val IS_USER_RATED = booleanPreferencesKey("is_user_rated")
        private val SAVE_PHOTO_COUNT = intPreferencesKey("save_photo_count")
        private val EXIT_APP_COUNT = intPreferencesKey("exit_app_count")
    }
}
