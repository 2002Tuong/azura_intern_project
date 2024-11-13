package com.bloodpressure.app.data.remote

import android.content.Context
import com.bloodpressure.app.BuildConfig
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.data.model.PremiumConfig
import com.bloodpressure.app.receiver.ReminderMode
import com.bloodpressure.app.screen.home.info.InfoItemId
import com.bloodpressure.app.screen.updateapp.UpdateInfo
import com.bloodpressure.app.utils.JsonProvider
import com.bloodpressure.app.utils.Logger
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlin.coroutines.resume

class RemoteConfig(private val context: Context) {

    val premiumConfigs: List<PremiumConfig>
        get() = try {
            val jsonString = FirebaseRemoteConfig.getInstance().getString("PREMIUM_PLANS")
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }

    val productIds: List<String>
        get() = premiumConfigs.filter { !it.isSubscription }.map { it.id }

    val subIds: List<String>
        get() = premiumConfigs.filter { it.isSubscription }.map { it.id }

    private val json by lazy { JsonProvider.json }

    private val isREmoteConfigLoaded = MutableStateFlow(false)

    private var _adsConfig: AdsConfig? = null
    val adsConfig: AdsConfig
        get() {
            if (_adsConfig != null) {
                return _adsConfig!!
            }
            val config = try {
                val jsonString = FirebaseRemoteConfig.getInstance().getString("ads_config")
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                AdsConfig(
                    shouldShowInterSplashAd = true,
                    shouldShowOpenBetaAd = true,
                    shouldShowClickHistoryItemInterAd = true,
                    shouldShowClickInfoItemInterAd = true,
                    shouldShowClickAddRecordInterAd = true,
                    shouldShowExitInterAd = true,
                    shouldShowHomeNativeAd = true,
                    shouldShowOnboardingNativeAd = true,
                    shouldShowHistoryNativeAd = true,
                    shouldShowInfoNativeAd = true,
                    shouldShowLanguageNativeAd = true,
                    shouldShowLanguageSettingNativeAd = true,
                    shouldShowAddRecordNativeAd = true,
                    shouldShowExitAppNativeAd = true,
                    shouldShowHomeBannerAd = true,
                    shouldShowInfoDetailBannerAd = true,
                    shouldShowHearRateNativeAd = true,
                    shouldShowPressureNativeAd = true,
                    shouldShowBloodSugarNativeAd = true,
                    shouldShowBmiNativeAd = true,
                    shouldShowNativeAd = true,
                    shouldShowInterAd = true,
                    ctaTopLanguage = true,
                    ctaTopOnboarding =  true,
                    shouldShowInterSugar = true,
                    shouldShowInterWater = true,
                    shouldShowInterHeartRate = true,
                    shouldShowInterBmi = true,
                )
            }
            _adsConfig = config
            Logger.d("remote Config: $config")
            return config
        }

    private var _shouldHideNavigationBar: Boolean? = null
    val shouldHideNavigationBar: Boolean
        get() {
            if (_shouldHideNavigationBar != null) {
                return _shouldHideNavigationBar == true
            }
            val hideNavigationBar =
                FirebaseRemoteConfig.getInstance().getBoolean("hide_navigation_bar")
            _shouldHideNavigationBar = hideNavigationBar
            return hideNavigationBar
        }

    fun clear() {
        isREmoteConfigLoaded.update { false }
        _adsConfig = null
        _shouldHideNavigationBar = null
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

            continuation.invokeOnCancellation { isREmoteConfigLoaded.update { true } }
        }
    }

    fun getNotes(): List<Note> {
        return try {
            listOf(
                context.getString(R.string.note_left),
                context.getString(R.string.note_right),
                context.getString(R.string.note_after_medication),
                context.getString(R.string.note_after_walking),
                context.getString(R.string.note_before_meal),
                context.getString(R.string.note_after_meal),
                context.getString(R.string.note_sitting),
                context.getString(R.string.note_lying),
                context.getString(R.string.note_period),
            ).mapIndexed { index: Int, note: String ->
                Note(note, index)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getInfoByLanguage(@InfoItemId id: String, language: String?): String {
        return try {
            val key = if (language.isNullOrEmpty()) "infos" else "infos_$language"
            val jsonString =
                FirebaseRemoteConfig.getInstance().getString(key).takeIf { it.isNotEmpty() }
                    ?: FirebaseRemoteConfig.getInstance().getString("infos_en")
            val infoMap = json.decodeFromString<Map<String, String>>(jsonString)
            return infoMap[id].orEmpty()
        } catch (e: Exception) {
            ""
        }
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

    fun offsetTimeShowInterAds(): Long {
        return FirebaseRemoteConfig.getInstance().getLong("offset_time_show_inter_ads")
    }

    fun getAppUpdate(): UpdateInfo {
        val jsonString = FirebaseRemoteConfig.getInstance().getString("notify_update_new_version")
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            UpdateInfo(null, 0, null, null, null)
        }
    }

    fun getShowNotificationTimeMillis(): Long {
        return FirebaseRemoteConfig.getInstance().getLong("show_notification_time_millis")
    }

    fun shouldShowPremiumPopup(): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean("show_premium_popup")
    }

    private fun String.toVersionInt(): Int {
        return try {
            val numArray = this.split(".")
            return numArray[0].toInt() * 10000 + numArray[1].toInt() * 100 + numArray[2].toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun getReminderMode(): ReminderMode {
        val reminderModeInString = FirebaseRemoteConfig.getInstance().getString("reminder_mode")
        return if (reminderModeInString == ReminderMode.FULL.value) {
            ReminderMode.FULL
        } else ReminderMode.SIMPLE
    }
}
