package com.wifi.wificharger.utils

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RemoteConfigManager private constructor() {
    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val isRemoteConfigLoaded = MutableStateFlow(false)

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
    }

    suspend fun waitRemoteConfigLoaded(): Boolean? {
        return isRemoteConfigLoaded.filter { it }.firstOrNull()
    }

    suspend fun fetchRemoteConfigAsync() {
        return suspendCancellableCoroutine { continuation ->
            Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                task.exception?.let { }
                isRemoteConfigLoaded.update { true }
                continuation.resume(Unit)
            }
        }
    }

    fun clear() {
        isRemoteConfigLoaded.update { false }
    }

    companion object {
        @Volatile
        private var instance: RemoteConfigManager? = null

        fun getInstance(): RemoteConfigManager {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigManager().also { instance = it }
            }
        }
    }
}