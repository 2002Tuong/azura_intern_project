package com.artgen.app.ui.screen.language

import com.artgen.app.R
import java.util.Locale

class LanguageProvider {

    fun provide(): List<Language> {
        return listOf(
            Language(R.drawable.ic_flag_en, R.string.lang_english, "en"),
            Language(R.drawable.ic_flag_es, R.string.lang_spanish, "es"),
            Language(R.drawable.ic_flag_pt, R.string.lang_portugal, "pt"),
            Language(R.drawable.ic_flag_india, R.string.lang_hindi, "hi"),
            Language(R.drawable.ic_flag_france, R.string.lang_french, "fr"),
            Language(R.drawable.ic_flag_arabic, R.string.lang_arabic, "ar"),
            Language(R.drawable.ic_flag_russian, R.string.lang_russian, "ru"),
            Language(R.drawable.ic_flag_bangladesh, R.string.lang_bengali, "bn"),
            Language(R.drawable.ic_flag_urdu, R.string.lang_urdu, "ur"),
            Language(R.drawable.ic_flag_germany, R.string.lang_german, "de"),
            Language(R.drawable.ic_flag_japan, R.string.lang_japanese, "ja"),
            Language(R.drawable.ic_flag_india, R.string.lang_marathi, "mr"),
            Language(R.drawable.ic_flag_india, R.string.lang_telugu, "te"),
            Language(R.drawable.ic_flag_turkey, R.string.lang_turkish, "tr"),
            Language(R.drawable.ic_flag_india, R.string.lang_tamil, "ta"),
            Language(R.drawable.ic_flag_korea, R.string.lang_korean, "ko"),
            Language(R.drawable.ic_flag_vn, R.string.lang_vietnamese, "vi"),
            Language(R.drawable.ic_flag_thailand, R.string.lang_thai, "th"),
            Language(R.drawable.ic_flag_italy, R.string.lang_italian, "it"),
        )
    }

    fun getCurrentSystemLanguage(): String {
        return Locale.getDefault().language
    }

    fun isLanguageSupported(language: String): Boolean {
        return provide().map { it.languageTag }.contains(language)
    }
}
