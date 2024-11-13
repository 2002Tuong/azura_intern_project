package com.wifi.wificharger.ui.picklanguage

import android.content.res.Resources
import com.wifi.wificharger.R
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.model.Language

class LanguageProvider(
    private val appDataStore: AppDataStore
) {

    private val supportedLanguages = listOf(
        Language(R.drawable.ic_flag_england, languageNameId = R.string.lang_english, languageCode = "en"),
        Language(R.drawable.ic_flag_portugal, languageNameId = R.string.lang_portugal, languageCode = "pt"),
        Language(R.drawable.ic_flag_india, languageNameId = R.string.lang_hindi, languageCode = "hi"),
        Language(R.drawable.ic_flag_es, languageNameId = R.string.lang_spanish, languageCode = "es"),
        Language(R.drawable.ic_flag_france, languageNameId = R.string.lang_french, languageCode = "fr"),
        Language(R.drawable.ic_flag_russian, languageNameId = R.string.lang_russian, languageCode = "ru"),
        Language(R.drawable.ic_flag_saudi_arabia, languageNameId = R.string.lang_arabic, languageCode = "ar"),
        Language(R.drawable.ic_flag_chinese, languageNameId = R.string.lang_chinese, languageCode = "zh"),
        Language(R.drawable.ic_flag_bangladesh, languageNameId = R.string.lang_bengali, languageCode = "bn"),
        Language(R.drawable.ic_flag_urdu, languageNameId = R.string.lang_urdu, languageCode = "ur"),
        Language(R.drawable.ic_flag_germany, languageNameId = R.string.lang_german, languageCode = "de"),
        Language(R.drawable.ic_flag_japan, languageNameId = R.string.lang_japanese, languageCode = "ja"),
        Language(R.drawable.ic_flag_india, languageNameId = R.string.lang_marathi, languageCode = "mr"),
        Language(R.drawable.ic_flag_india, languageNameId = R.string.lang_telugu, languageCode = "te"),
        Language(R.drawable.ic_flag_turkey, languageNameId = R.string.lang_turkish, languageCode = "tr"),
        Language(R.drawable.ic_flag_india, languageNameId = R.string.lang_tamil, languageCode = "ta"),
        Language(R.drawable.ic_flag_korea, languageNameId = R.string.lang_korean, languageCode = "ko"),
        Language(R.drawable.ic_flag_vietnam, languageNameId = R.string.lang_vietnamese, languageCode = "vi"),
        Language(R.drawable.ic_flag_italy, languageNameId = R.string.lang_italian, languageCode = "it"),
        Language(R.drawable.ic_flag_thailand, languageNameId = R.string.lang_thai, languageCode = "th")
    )

    fun getLanguageList(): ArrayList<Language> {
        var deviceLanguageCode = ""

        val currentAppLanguage = appDataStore.selectedLanguage

        deviceLanguageCode = currentAppLanguage.ifEmpty {
            Resources.getSystem().configuration.locale.language
        }

        val isDeviceLanguageSupported = supportedLanguages.any { it.languageCode == deviceLanguageCode }

        return if (isDeviceLanguageSupported) {
            val deviceLanguageItem = supportedLanguages.firstOrNull { it.languageCode == deviceLanguageCode }
            val languageList = ArrayList<Language>()

            languageList.addAll(supportedLanguages.filter { it.languageCode != deviceLanguageCode }
                .take(4))
            deviceLanguageItem?.let {
                languageList.add(2, it)
            }

            languageList
        } else {
            ArrayList(supportedLanguages.take(6))
        }
    }

    fun getAllLanguageList(): ArrayList<Language> {
        val deviceLanguageCode: String

        val currentAppLanguage = appDataStore.selectedLanguage

        deviceLanguageCode = currentAppLanguage.ifEmpty {
            Resources.getSystem().configuration.locale.language
        }

        val isDeviceLanguageSupported = supportedLanguages.any { it.languageCode == deviceLanguageCode }

        return if (isDeviceLanguageSupported) {
            val deviceLanguageItem = supportedLanguages.firstOrNull { it.languageCode == deviceLanguageCode }
            val languageList = ArrayList<Language>()

            deviceLanguageItem?.let {
                languageList.add(it)
            }

            languageList.addAll(supportedLanguages.filter { it.languageCode != deviceLanguageCode }
                .take(20))

            languageList
        } else {
            ArrayList(supportedLanguages)
        }
    }

}
