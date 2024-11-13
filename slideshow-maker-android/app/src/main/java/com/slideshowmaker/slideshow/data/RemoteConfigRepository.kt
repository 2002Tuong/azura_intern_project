package com.slideshowmaker.slideshow.data

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.response.*
import com.slideshowmaker.slideshow.utils.extentions.appVersionToInt
import com.slideshowmaker.slideshow.utils.extentions.isFirstInstall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.coroutines.resume

object RemoteConfigRepository {

    private const val VERSION_ENABLE_ADMOB_KEY = "VERSION_ENABLE_ADMOB"
    private const val THEME_CONFIG_KEY = "THEME_CONFIG"
    private const val MUSIC_CONFIG_KEY = "MUSICS_CONFIG"
    private const val CATEGORIES_CONFIG_KEY = "MUSIC_CATEGORIES"
    private const val SUGGESTED_APPS_KEY = "SUGGESTED_APPS"
    private const val TRANSITION_CONFIG_KEY = "TRANSITION_CONFIG"
    private const val VIDEO_EFFECT_CONFIG_KEY = "VIDEO_EFFECT_CONFIG"
    private const val PREMIUM_PLANS_KEY = "PREMIUM_PLANS"
    private const val NATIVE_ADS_CONFIG_KEY = "NATIVE_ADS"
    private const val APP_OPEN_ADS_CONFIG_KEY = "APP_OPEN_ADS"
    private const val INTERSTITIAL_ADS_KEY = "INTERSTITIAL_ADS"
    private const val RANDOM_TRANSITION_KEY = "RANDOM_TRANSITION_CONFIG"
    private const val CONFIG_KEY = "CONFIG"
    private const val THEME_CONFIG_V2_KEY = "THEME_CONFIG_V2"
    private const val FRAME_CONFIG_KEY = "FRAME_CONFIG"
    private const val FILTER_CONFIG_V2_KEY = "FILTER_CONFIG_V2"
    private const val VERSION_CONFIG_KEY = "VERSION_CONFIG"
    private const val SAVE_VIDEO_QUALITY_CONFIG_KEY = "VIDEO_SAVE_QUALITY_CONFIG"
    private const val CMP_REQUIRE_KEY = "cmp_require"
    private val gson = Gson()
    val isRemoteConfigLoaded = MutableStateFlow(false)
    private val localVersion: String
        get() = BuildConfig.VERSION_NAME
    private val remoteVersion: String
        get() = Firebase.remoteConfig.getString(VERSION_ENABLE_ADMOB_KEY)

    val cmpRequire: Boolean
        get() = try {
            Firebase.remoteConfig.getBoolean(CMP_REQUIRE_KEY)
        } catch (_: Exception) {
            false
        }

    fun updateRealtimeListener() {
        Firebase.remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Firebase.remoteConfig.activate()
            }

            override fun onError(error: FirebaseRemoteConfigException) = Unit

        })
    }

    suspend fun waitRemoteConfigLoaded(): Boolean? {
        return isRemoteConfigLoaded.filter { it }.firstOrNull()
    }

    suspend fun fetchRemoteConfigAsync() {
        try {
            val remoteConfig = Firebase.remoteConfig
            val remoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0L)
                .build()
            remoteConfig.setConfigSettingsAsync(remoteConfigSettings).await()
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()
            suspendCancellableCoroutine { continuation ->
                remoteConfig.fetchAndActivate().addOnCompleteListener {task ->
                    Timber.d("RemoteConfig ${supportTransitionConfig.orEmpty().map { it.isPro }}")
                    if (SharedPreferUtils.isGetRemoteConfigSuccessFirstTime && VideoMakerApplication.getContext().isFirstInstall()) {
                        TrackingFactory.Common.getRemoteConfigSuccessFirstLaunch().track()
                        SharedPreferUtils.isGetRemoteConfigSuccessFirstTime = false
                    }

                    task.exception?.let { }
                    isRemoteConfigLoaded.update { true }
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    isRemoteConfigLoaded.update { true }
                }.addOnCanceledListener {
                    isRemoteConfigLoaded.update { true }
                }
            }
        }catch (_: Exception) {
            isRemoteConfigLoaded.update { true }
        }
    }

    suspend fun fetch() {
        try {
            val remoteConfig = Firebase.remoteConfig
            val remoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0L)
                .build()
            remoteConfig.setConfigSettingsAsync(remoteConfigSettings).await()
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()
            remoteConfig.fetchAndActivate().await()
            Timber.d("RemoteConfig ${supportTransitionConfig.orEmpty().map { it.isPro }}")
            if (SharedPreferUtils.isGetRemoteConfigSuccessFirstTime && VideoMakerApplication.getContext().isFirstInstall()) {
                TrackingFactory.Common.getRemoteConfigSuccessFirstLaunch().track()
                SharedPreferUtils.isGetRemoteConfigSuccessFirstTime = false
            }
            Log.d("Remote", "call finish")
        } catch (_: Exception) {

        }
    }

    val versionConfig: VersionConfig?
        get() = safetyParse(
            Firebase.remoteConfig.getString(
                VERSION_CONFIG_KEY,
            ),
            VersionConfig::class.java,
        )

    val appConfig: AppConfig?
        get() = safetyParse(
            Firebase.remoteConfig.getString(CONFIG_KEY),
            AppConfig::class.java
        )

    val videoQualityConfigs: List<VideoQualityConfigSetting>?
        get() = fromJson<List<VideoQualityConfigSetting>>(Firebase.remoteConfig.getString(SAVE_VIDEO_QUALITY_CONFIG_KEY))

    val audioConfigs: List<ItemAudio>?
        get() = fromJson<List<ItemAudio>>(Firebase.remoteConfig.getString(MUSIC_CONFIG_KEY))


    val categories: List<String>?
        get() = fromJson<List<String>>(Firebase.remoteConfig.getString(CATEGORIES_CONFIG_KEY))

    val themeConfig: List<ThemeConfig>?
        get() = fromJson<List<ThemeConfig>>(Firebase.remoteConfig.getString(THEME_CONFIG_KEY))

    val suggestedAppConfigs: List<SuggestedAppConfig>?
        get() = fromJson<List<SuggestedAppConfig>>(Firebase.remoteConfig.getString(SUGGESTED_APPS_KEY))


    val supportTransitionConfig: List<ItemTransition>?
        get() = fromJson<List<ItemTransition>>(Firebase.remoteConfig.getString(TRANSITION_CONFIG_KEY))

    val videoEffectConfig: List<ItemEffect>?
        get() = fromJson<List<ItemEffect>>(Firebase.remoteConfig.getString(VIDEO_EFFECT_CONFIG_KEY))

    val premiumPlans: List<PremiumPlan>?
        get() = fromJson<List<PremiumPlan>>(Firebase.remoteConfig.getString(PREMIUM_PLANS_KEY))

    val themeConfigV2: List<ThemeConfig>
        get() = fromJson<List<ThemeConfig>>(Firebase.remoteConfig.getString(THEME_CONFIG_V2_KEY)) ?: emptyList()
    val frameConfig: List<FrameDataSetting>
        get() = fromJson<List<FrameDataSetting>>(Firebase.remoteConfig.getString(FRAME_CONFIG_KEY)) ?: emptyList()

    val filterConfig: List<FilterDataSetting>
        get() = fromJson<List<FilterDataSetting>>(Firebase.remoteConfig.getString(FILTER_CONFIG_V2_KEY)) ?: emptyList()

    val nativeAdsConfig
        get() = if (isAdsEnable) {
            safetyParse(
                Firebase.remoteConfig.getString(NATIVE_ADS_CONFIG_KEY),
                NativeAdsConfig::class.java,
            )
        } else {
            null
        }

    val appOpenAdsConfig
        get() = if (isAdsEnable) {
            safetyParse(
                Firebase.remoteConfig.getString(APP_OPEN_ADS_CONFIG_KEY),
                AppOpenAdsConfig::class.java,
            )
        } else {
            null
        }

    val interstitialAdsConfig
        get() = if (isAdsEnable) {
            safetyParse(
                Firebase.remoteConfig.getString(INTERSTITIAL_ADS_KEY),
                InterstitialAdsConfigSetting::class.java,
            )
        } else {
            null
        }

    val randomTransitionConfig
        get() = safetyParse(
            Firebase.remoteConfig.getString(RANDOM_TRANSITION_KEY),
            RandomTransitionConfig::class.java
        ) ?: RandomTransitionConfig(RandomTransition())

    private fun <T> safetyParse(json: String, clazz: Class<T>) = try {
        gson.fromJson(json, clazz)
    } catch (exception: Exception) {
        null
    }

    private inline fun <reified T> fromJson(json: String) =
        try {
            gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        } catch (exception: Exception) {
            null
        }

    val isAdsEnable: Boolean
        get() {
            val currentVersionValue = localVersion.appVersionToInt()

            val configVersionValue = remoteVersion.appVersionToInt()
            return currentVersionValue <= configVersionValue && !SharedPreferUtils.proUser
        }

}