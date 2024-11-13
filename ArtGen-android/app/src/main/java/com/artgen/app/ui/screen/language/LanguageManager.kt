package com.artgen.app.ui.screen.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

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
