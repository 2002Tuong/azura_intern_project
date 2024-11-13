package com.bloodpressure.app.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.bloodpressure.app.screen.language.Language

class LanguageManager {

    fun updateLanguage(language: Language) {
        if (language.languageTag == LANGUAGE_TAG_AUTO) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.getEmptyLocaleList()
            )
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.languageTag)
            )
        }
    }

    companion object {
        const val LANGUAGE_TAG_AUTO = "auto"
    }
}
