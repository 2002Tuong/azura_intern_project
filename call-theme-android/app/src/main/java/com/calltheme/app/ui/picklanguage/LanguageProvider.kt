package com.calltheme.app.ui.picklanguage

import android.content.Context
import android.content.res.Resources
import com.screentheme.app.R
import com.screentheme.app.models.LanguageModel
import com.screentheme.app.utils.extensions.getLanguage
import com.screentheme.app.utils.helpers.SharePreferenceHelper

class LanguageProvider(private val context: Context) {

    private val supportedLanguages = listOf(
        LanguageModel(R.drawable.ic_flag_england, languageNameId = R.string.lang_english, languageCode = "en"),
        LanguageModel(R.drawable.ic_flag_portugal, languageNameId = R.string.lang_portugal, languageCode = "pt"),
        LanguageModel(R.drawable.ic_flag_india, languageNameId = R.string.lang_hindi, languageCode = "hi"),
        LanguageModel(R.drawable.ic_flag_es, languageNameId = R.string.lang_spanish, languageCode = "es"),
        LanguageModel(R.drawable.ic_flag_france, languageNameId = R.string.lang_french, languageCode = "fr"),
        LanguageModel(R.drawable.ic_flag_russian, languageNameId = R.string.lang_russian, languageCode = "ru"),
        LanguageModel(R.drawable.ic_flag_saudi_arabia, languageNameId = R.string.lang_arabic, languageCode = "ar"),
        LanguageModel(R.drawable.ic_flag_chinese, languageNameId = R.string.lang_chinese, languageCode = "zh"),
        LanguageModel(R.drawable.ic_flag_bangladesh, languageNameId = R.string.lang_bengali, languageCode = "bn"),
        LanguageModel(R.drawable.icon_flag_urdu, languageNameId = R.string.lang_urdu, languageCode = "ur"),
        LanguageModel(R.drawable.ic_flag_germany, languageNameId = R.string.lang_german, languageCode = "de"),
        LanguageModel(R.drawable.ic_flag_japan, languageNameId = R.string.lang_japanese, languageCode = "ja"),
        LanguageModel(R.drawable.ic_flag_india, languageNameId = R.string.lang_marathi, languageCode = "mr"),
        LanguageModel(R.drawable.ic_flag_india, languageNameId = R.string.lang_telugu, languageCode = "te"),
        LanguageModel(R.drawable.icon_flag_turkey, languageNameId = R.string.lang_turkish, languageCode = "tr"),
        LanguageModel(R.drawable.ic_flag_india, languageNameId = R.string.lang_tamil, languageCode = "ta"),
        LanguageModel(R.drawable.ic_flag_korea, languageNameId = R.string.lang_korean, languageCode = "ko"),
        LanguageModel(R.drawable.icon_flag_vietnam, languageNameId = R.string.lang_vietnamese, languageCode = "vi"),
        LanguageModel(R.drawable.ic_flag_italy, languageNameId = R.string.lang_italian, languageCode = "it"),
        LanguageModel(R.drawable.ic_flag_thailand, languageNameId = R.string.lang_thai, languageCode = "th")
    )

    fun getLanguageList(): ArrayList<LanguageModel> {
        var deviceLanguageCode = ""

        val currentAppLanguage = SharePreferenceHelper.getLanguage(context)

        deviceLanguageCode = currentAppLanguage?.languageCode ?: Resources.getSystem().configuration.locale.language

        val isDeviceLanguageSupported = supportedLanguages.any { it.languageCode == deviceLanguageCode }

        return if (isDeviceLanguageSupported) {
            val deviceLanguageItem = supportedLanguages.firstOrNull { it.languageCode == deviceLanguageCode }
            val languageList = ArrayList<LanguageModel>()

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

    fun getAllLanguageList(): ArrayList<LanguageModel> {
        var deviceLanguageCode = ""

        val currentAppLanguage = SharePreferenceHelper.getLanguage(context)

        deviceLanguageCode = currentAppLanguage?.languageCode ?: Resources.getSystem().configuration.locale.language

        val isDeviceLanguageSupported = supportedLanguages.any { it.languageCode == deviceLanguageCode }

        return if (isDeviceLanguageSupported) {
            val deviceLanguageItem = supportedLanguages.firstOrNull { it.languageCode == deviceLanguageCode }
            val languageList = ArrayList<LanguageModel>()

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
