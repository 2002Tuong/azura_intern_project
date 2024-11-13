package com.screentheme.app.data.remote.config

import com.google.gson.Gson
import com.screentheme.app.BuildConfig
import com.screentheme.app.models.CallThemeConfigModel
import com.screentheme.app.models.PremiumConfig
import org.json.JSONArray

data object AppRemoteConfig : ConfigModel {
    val shouldHideNavigationBar by config("hide_navigation_bar", false)
    val minVersionToShowAds by config("version_enable_ads", "")
    val offsetTimeShowInterAds by config("offset_time_show_inter_ads", 0L)

    val languageCtaTop by config("native_language_cta_top", false)
    val onboardingCtaTop by config("native_onboarding_cta_top", false)

    val premiumConfigsInString by config("PREMIUM_PLANS", "")
    val premiumConfigs: List<PremiumConfig>
        get() = try {
            parseJSONToSubscriptionList(premiumConfigsInString)
        } catch (e: Exception) {
            emptyList()
        }

    val hideNavigationBar by config("hide_navigation_bar", true)
    val interSplash by config("inter_splash_highfloor", false)
    val nativeSplash by config("native_splash", false)
    val interPreviewVideo by config("inter_preview_video", false)
    val nativeLanguageDuplicate by config("native_language_duplicate", true)
    val nativeLanguage by config("native_language_highfloor", true)
    val nativeSaveTheme by config("native_save_theme", true)
    val languageScreenType by config("language_screen_type", "option1")

    val cmpRequire by config("cmp_require", true)

    val callThemeConfigInString by config("call_theme_configs", "")
    fun callThemeConfigs(): CallThemeConfigModel {
        return try {
            Gson().fromJson(callThemeConfigInString, CallThemeConfigModel::class.java)
        } catch (e: Exception) {
            CallThemeConfigModel()
        }
    }

    val collapsibleBannerHome by config("collapsible_banner_home", false)

    fun offAllAds(): Boolean {
        val versionName: String = BuildConfig.VERSION_NAME
        if (minVersionToShowAds.isEmpty()) {
            return false
        }
        val currentVersion = versionName.toVersionInt()
        return currentVersion > minVersionToShowAds.toVersionInt()
    }

    private fun String.toVersionInt(): Int {
        return try {
            val numArray = this.split(".")
            return numArray[0].toInt() * 10000 + numArray[1].toInt() * 100 + numArray[2].toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun parseJSONToSubscriptionList(jsonString: String): ArrayList<PremiumConfig> {
        val premiumConfigList = ArrayList<PremiumConfig>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonSubscription = jsonArray.getJSONObject(i)

                val productId = jsonSubscription.getString("product_id")
                val isBest = jsonSubscription.getBoolean("is_best")
                val show = jsonSubscription.getBoolean("show")
                val index = jsonSubscription.getInt("index")
                val discount =
                    if (jsonSubscription.isNull("discount")) null else jsonSubscription.getDouble("discount")
                val type = jsonSubscription.getString("type")

                val premiumConfig = PremiumConfig(
                    id = productId,
                    show = show,
                    discount = "$discount",
                    isBest = isBest,
                    index = index,
                    type = type
                )
                premiumConfigList.add(premiumConfig)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return premiumConfigList
    }
}