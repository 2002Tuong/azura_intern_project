package com.example.claptofindphone.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.models.LanguageModel
import com.example.claptofindphone.utils.extensions.moveItemToPosition
import java.util.Locale

class LanguageSupporter (
    private val preferenceSupplier: PreferenceSupplier
) {
    private var curLocale: Locale? = null
    private val supportedLanguageList = listOf(
        LanguageModel("en", R.string.lang_english, R.drawable.icon_flag_england, false),
        LanguageModel("hi", R.string.lang_hindi, R.drawable.icon_flag_india, false),
        LanguageModel("es", R.string.lang_spanish, R.drawable.icon_flag_es, false),
        LanguageModel("pt", R.string.lang_portugal, R.drawable.icon_flag_portugal, false),
        LanguageModel("fr", R.string.lang_french, R.drawable.icon_flag_france, false),
        LanguageModel("ko", R.string.lang_korean, R.drawable.icon_flag_korea, false),
        LanguageModel("ja", R.string.lang_japanese, R.drawable.icon_flag_japan, false),
        LanguageModel("vi", R.string.lang_vietnamese, R.drawable.icon_flag_vietnam, false),
        LanguageModel("zh", R.string.lang_chinese, R.drawable.icon_flag_chinese, false),
        LanguageModel("ru", R.string.lang_russian, R.drawable.icon_flag_russian, false),
        LanguageModel("ar", R.string.lang_arabic, R.drawable.icon_flag_saudi_arabia, false),
        LanguageModel("bn", R.string.lang_bengali, R.drawable.icon_flag_bangladesh, false),
        LanguageModel("ur", R.string.lang_urdu, R.drawable.icon_flag_urdu, false),
        LanguageModel("de", R.string.lang_german, R.drawable.icon_flag_germany, false),
        LanguageModel("mr", R.string.lang_marathi, R.drawable.icon_flag_india, false),
        LanguageModel("te", R.string.lang_telugu, R.drawable.icon_flag_india, false),
        LanguageModel("tr", R.string.lang_turkish, R.drawable.icon_flag_turkey, false),
        LanguageModel("ta", R.string.lang_tamil, R.drawable.icon_flag_india, false),
        LanguageModel("it", R.string.lang_italian, R.drawable.icon_flag_italy, false),
        LanguageModel("th", R.string.lang_thai, R.drawable.icon_flag_thailand, false),
    )

    fun getSupportedLanguage(): List<LanguageModel> {
        val deviceLanguageCode = getDeviceLanguage()
        Log.d("Language", "Device Language: ${deviceLanguageCode}")
//        if (preferenceProvider.languageCode.isEmpty()) {
//            preferenceProvider.languageCode = "en"
//        }
        val languageDevicePosition = supportedLanguageList.indexOfFirst { it.languageCode == deviceLanguageCode }
        val supportedDeviceLanguageList = if (languageDevicePosition < 2) {
            supportedLanguageList
        } else {
            supportedLanguageList.moveItemToPosition(3) { it.languageCode == deviceLanguageCode }
        }
//        supportedDeviceLanguages.forEach {
//            it.isChoose = it.code == preferenceProvider.languageCode
//        }
        return supportedDeviceLanguageList
    }

    private fun saveLocale(lang: String) {
        preferenceSupplier.languageCode = lang
    }

    fun loadLocale(context: Context) {
        val languageCode: String = preferenceSupplier.languageCode
        if (languageCode == "") {
            val configuration = Configuration()
            val defaultLocale = Locale.getDefault()
            Locale.setDefault(defaultLocale)
            configuration.locale = defaultLocale
            context.resources
                .updateConfiguration(configuration, context.resources.displayMetrics)
        } else {
            changeLang(languageCode, context)
        }
    }

    fun changeLang(lang: String, context: Context) {
        if (lang.equals("", ignoreCase = true)) return
        curLocale = Locale(lang)
        saveLocale(lang)
        curLocale?.let { Locale.setDefault(it) }
        val configuration = Configuration()
        configuration.locale = curLocale
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    private fun getDeviceLanguage(): String {
        val systemLocale =
            Resources.getSystem().configuration.locales.get(0)
        return systemLocale.language
    }
}