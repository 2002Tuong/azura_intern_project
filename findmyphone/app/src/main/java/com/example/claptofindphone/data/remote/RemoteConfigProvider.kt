package com.example.claptofindphone.data.remote

import com.example.claptofindphone.BuildConfig
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RemoteConfigProvider(
    private val preferenceSupplier: PreferenceSupplier
) {

    private val isRemoteConfigLoading = MutableStateFlow(false)

    private val appLocalVersion: String
        get() = BuildConfig.VERSION_NAME
    private val appEnableAdsVersion: String
        get() = Firebase.remoteConfig.getString(KEY_VERSION_ENABLE_ADS)

    val isNativeAdLanguageCtaTop
        get() = FirebaseRemoteConfig.getInstance().getBoolean(KEY_IS_NATIVE_AD_LANGUAGE_CTA_TOP)

    val isNativeAdOnboardCtaTop
        get() = FirebaseRemoteConfig.getInstance().getBoolean(KEY_IS_NATIVE_AD_ONBOARD_CTA_TOP)

    val permissionBtnEnabledState
        get() = FirebaseRemoteConfig.getInstance().getBoolean(KEY_PERMISSION_BTN_ENABLE_STATE)

    fun clear() {
        isRemoteConfigLoading.update { false }
    }

    suspend fun waitRemoteConfigLoaded() {
        isRemoteConfigLoading.filter { it }.firstOrNull()
    }

    suspend fun fetchRemoteConfigAsync() {
        return suspendCancellableCoroutine { continuation ->
            Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                task.exception?.let { }
                isRemoteConfigLoading.update { true }
                continuation.resume(Unit)
            }

            continuation.invokeOnCancellation { isRemoteConfigLoading.update { true } }
        }
    }

    val isVersionAdsEnable: Boolean
        get() {
            val currentVersion = appLocalVersion.appVersionToInt()
            val configVersion= appEnableAdsVersion.appVersionToInt()
            return currentVersion <= configVersion && !preferenceSupplier.isProUser
        }

    companion object {
        private const val KEY_VERSION_ENABLE_ADS = "version_enable_ads"
        private const val KEY_IS_NATIVE_AD_LANGUAGE_CTA_TOP = "native_language_cta_top"
        private const val KEY_IS_NATIVE_AD_ONBOARD_CTA_TOP = "native_onboarding_cta_top"
        private const val KEY_PERMISSION_BTN_ENABLE_STATE= "permission_enable_state"
    }
}

fun String?.appVersionToInt(): Int {
    return this?.split(".")?.mapIndexed { index, value ->
        when (index) {
            0 -> value.toIntSafety() * 100
            1 -> value.toIntSafety() * 10
            else -> value.toIntSafety()
        }
    }.orEmpty().sumOf { it }
}
fun String.toIntSafety() = this.toIntOrNull() ?: 0