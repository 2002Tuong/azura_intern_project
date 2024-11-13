package com.wifi.wificharger.data.remote

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.wifi.wificharger.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

class RemoteConfig {
    @OptIn(ExperimentalSerializationApi::class)
    private val json by lazy {
        Json {
            prettyPrint = true
            isLenient = true
            explicitNulls = false
            ignoreUnknownKeys = true
            useAlternativeNames = false
        }
    }

//    val premiumConfigs: List<PremiumConfig>
//        get() = try {
//            val jsonString = FirebaseRemoteConfig.getInstance().getString("premium_plans")
//            json.decodeFromString(jsonString)
//        } catch (e: Exception) {
//            emptyList()
//        }
//
//    val enablePremium: Boolean
//        get() = try {
//            FirebaseRemoteConfig.getInstance().getBoolean("enable_premium")
//        } catch (e: Exception) {
//            false
//        }
//
//    val productIds: List<String>
//        get() = premiumConfigs.filter { !it.isSubscription }.map { it.id }
//
//    val subIds: List<String>
//        get() = premiumConfigs.filter { it.isSubscription }.map { it.id }

    fun shouldHideNavigationBar(): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean("hide_navigation_bar")
    }

    fun offAllAds(): Boolean {
        val versionName: String = BuildConfig.VERSION_NAME
        val minVersionToShowAds = FirebaseRemoteConfig.getInstance().getString("version_enable_ads")
        if (minVersionToShowAds.isEmpty()) {
            return false
        }
        val currentVersion = versionName.toVersionInt()
        return currentVersion > minVersionToShowAds.toVersionInt()
    }

    fun offInterSplashAds(): Boolean {
        val versionName: String = BuildConfig.VERSION_NAME
        val minVersionToShowAds =
            FirebaseRemoteConfig.getInstance().getString("version_enable_inter_ads_splash")
        if (minVersionToShowAds.isEmpty()) {
            return false
        }
        val currentVersion = versionName.toVersionInt()
        return currentVersion > minVersionToShowAds.toVersionInt()
    }

    fun shouldShowInterExitAd(): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean("should_show_inter_exit_ad")
    }

    fun offsetTimeShowInterAds(): Long {
        return FirebaseRemoteConfig.getInstance().getLong("offset_time_show_inter_ads")
    }

    fun getMaxInputImageSize(): Long {
        val maxSize = FirebaseRemoteConfig.getInstance().getLong("max_input_image_size")
        if (maxSize <= 0L) {
            return DEFAULT_MAX_INPUT_IMAGE_SIZE
        }
        return maxSize
    }

    val cmpRequire: Boolean
        get() = try {
            FirebaseRemoteConfig.getInstance().getBoolean("cmp_require")
        } catch (e: Exception) {
            false
        }

    private fun String.toVersionInt(): Int {
        return try {
            val numArray = this.split(".")
            return numArray[0].toInt() * 10000 + numArray[1].toInt() * 100 + numArray[2].toInt()
        } catch (e: Exception) {
            0
        }
    }

//    fun getAppUpdate(): UpdateInfo {
//        val jsonString = FirebaseRemoteConfig.getInstance().getString("notify_update_new_version")
//        return try {
//            json.decodeFromString(jsonString)
//        } catch (e: Exception) {
//            UpdateInfo(null, 0, null, null, null)
//        }
//    }

//    fun getRatingAppConfig(): RateAppConfig {
//        val config = FirebaseRemoteConfig.getInstance().getString("rating_config")
//        return try {
//            json.decodeFromString(config)
//        } catch (e: Exception) {
//            RateAppConfig()
//        }
//    }

    companion object {
        private const val DEFAULT_MAX_INPUT_IMAGE_SIZE = 1280L
    }
}
