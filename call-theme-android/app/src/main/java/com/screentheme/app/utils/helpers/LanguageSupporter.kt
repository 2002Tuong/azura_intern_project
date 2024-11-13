package com.screentheme.app.utils.helpers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.screentheme.app.models.LanguageModel
import com.screentheme.app.utils.extensions.saveLanguage
import java.util.Locale

object LanguageSupporter {

    fun setAppLanguage(context: Context, language: LanguageModel) {

        val languageCode = language.languageCode

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)

        SharePreferenceHelper.saveLanguage(context, language)
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