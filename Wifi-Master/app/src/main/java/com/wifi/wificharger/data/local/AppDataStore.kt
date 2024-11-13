package com.wifi.wificharger.data.local

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class AppDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wifi_preference")

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

    val isRated: Boolean get() = runBlocking { context.dataStore.data.first()[IS_USER_RATED] == true }

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
        private val IS_USER_RATED = booleanPreferencesKey("is_user_rated")
    }
}
