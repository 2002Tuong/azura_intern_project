package com.wifi.wificharger.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.model.Language
import java.util.Locale

class LanguageUtils(
    private val appDataStore: AppDataStore
) {

    suspend fun setAppLanguage(context: Context, language: Language) {

        val languageCode = language.languageCode

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        appDataStore.setSelectedLanguage(language.languageCode)
    }

    fun setAppLanguageByCode(context: Context, languageCode: String) {

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}
