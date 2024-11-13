package com.artgen.app.data.remote

import com.artgen.app.BuildConfig
import com.artgen.app.R
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.data.model.PremiumConfig
import com.artgen.app.data.model.RateAppConfig
import com.artgen.app.ui.screen.updateapp.UpdateInfo
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlin.coroutines.resume

class RemoteConfig {
    private val json by lazy { JsonProvider.json }

    private val isREmoteConfigLoaded = MutableStateFlow(false)

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        FirebaseRemoteConfig.getInstance().setDefaultsAsync(R.xml.remote_config_defaults)
        FirebaseRemoteConfig.getInstance().setConfigSettingsAsync(configSettings)
    }
    val premiumConfigs: List<PremiumConfig>
        get() = try {
            val jsonString =FirebaseRemoteConfig.getInstance().getString("premium_plans")
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }

    val productIds: List<String>
        get() = premiumConfigs.filter { !it.isSubscription }.map { it.id }

    val subIds: List<String>
        get() = premiumConfigs.filter { it.isSubscription }.map { it.id }

    fun clear() {
        isREmoteConfigLoaded.update { false }
    }

    suspend fun waitRemoteConfigLoaded() {
        isREmoteConfigLoaded.filter { it }.firstOrNull()
    }

    suspend fun fetchRemoteConfigAsync() {
        return suspendCancellableCoroutine { continuation ->
            Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                task.exception?.let { }
                isREmoteConfigLoaded.update { true }
                continuation.resume(Unit)
            }
        }
    }

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

    val artStyles: List<ArtStyle>
        get() = try {
            val jsonString = FirebaseRemoteConfig.getInstance().getString("art_styles")
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }

    private fun String.toVersionInt(): Int {
        return try {
            val numArray = this.split(".")
            return numArray[0].toInt() * 10000 + numArray[1].toInt() * 100 + numArray[2].toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun getAppUpdate(): UpdateInfo {
        val jsonString = FirebaseRemoteConfig.getInstance().getString("notify_update_new_version")
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            UpdateInfo(null, 0, null, null, null)
        }
    }

    val onboardingXml
        get() = FirebaseRemoteConfig.getInstance().getBoolean("onboarding_xml")

    val nativeLanguageCtaTop get() = FirebaseRemoteConfig.getInstance().getBoolean("native_language_cta_top")
    val nativeOnboardCtaTop
        get() = FirebaseRemoteConfig.getInstance().getBoolean("native_onboarding_cta_top")

    fun getRatingAppConfig(): RateAppConfig {
        val config = FirebaseRemoteConfig.getInstance().getString("rating_config")
        return try {
            json.decodeFromString(config)
        } catch (e: Exception) {
            RateAppConfig()
        }
    }

    companion object {
        private const val DEFAULT_MAX_INPUT_IMAGE_SIZE = 1280L
    }
}
