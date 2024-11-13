package com.example.videoart.batterychargeranimation.helper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.extension.moveItemToPosition
import com.example.videoart.batterychargeranimation.model.LanguageModel
import java.util.Locale

object LanguageHelper {
    private var deviceLocale: Locale? = null
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
        val deviceLanguage = getDeviceLanguage()
        val posLanguageDevice = supportedLanguageList.indexOfFirst { it.code == deviceLanguage }
        val supportDeviceLanguages = supportedLanguageList.moveItemToPosition(3) { it.code == deviceLanguage }
        supportDeviceLanguages.forEach {
            it.isChoose = it.code == PreferenceUtils.langCode
        }
        return supportDeviceLanguages
    }

    private fun saveLocale(lang: String) {
        PreferenceUtils.langCode = lang
    }

    fun loadLocale(context: Context) {
        val lang: String = PreferenceUtils.langCode
        if (lang == "") {
            val configuration = Configuration()
            val defLocale = Locale.getDefault()
            Locale.setDefault(defLocale)
            configuration.locale = defLocale
            context.resources
                .updateConfiguration(configuration, context.resources.displayMetrics)
        } else {
            changeLang(lang, context)
        }
    }

    fun changeLang(lang: String, context: Context) {
        if (lang.equals("", ignoreCase = true)) return
        deviceLocale = Locale(lang)
        saveLocale(lang)
        deviceLocale?.let { Locale.setDefault(it) }
        val configuration = Configuration()
        configuration.locale = deviceLocale
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    private fun getDeviceLanguage(): String {
        val locale =
            Resources.getSystem().configuration.locales.get(0)
        return locale.language
    }
}