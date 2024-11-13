package com.screentheme.app.data.remote.config

import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.screentheme.app.utils.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object RemoteConfig {
    private val defaultValues: MutableMap<String, Any> = HashMap()
    class Builder {

        /**
         * Initialize Remote Config SDK with parameters.
         * See [Firebase Docs](https://firebase.google.com/docs/reference/android/com/google/firebase/remoteconfig/FirebaseRemoteConfigSettings.Builder.html)
         */
        var minimumFetchIntervalInSeconds: Long? = null

        /**
         * Initialize Remote Config SDK with parameters.
         * See [Firebase Docs](https://firebase.google.com/docs/reference/android/com/google/firebase/remoteconfig/FirebaseRemoteConfigSettings.Builder.html)
         */
        var fetchTimeoutInSeconds: Long? = null

        /**
         * Register ConfigModel to set default value to Remote Config
         *
         * @param model ConfigModel to register
         */
        @Suppress("unused")
        fun registerModels(vararg model: ConfigModel) = Unit
    }

    fun initialize(block: Builder.() -> Unit): Task<Void> {
        val builder = Builder()
        block(builder)

        Firebase.remoteConfig.apply {
            val config = remoteConfigSettings {
                builder.minimumFetchIntervalInSeconds?.let { minimumFetchIntervalInSeconds = it }
                builder.fetchTimeoutInSeconds?.let { fetchTimeoutInSeconds = it }
            }
            return setConfigSettingsAsync(config)
                .continueWithTask {
                    setDefaultsAsync(defaultValues)
                }
//                .continueWithTask {
//                    fetchAndActivate()
//                }
        }
    }

    internal fun register(clazz: Class<out Any>, key: String, default: Any) {
        if (defaultValues.containsKey(key)) {
            Logger.w( "Key $key of ${clazz.simpleName} is already registered.")
        } else {
            defaultValues[key] = default
        }
    }

    /**
     * Fetch remote values from Remote Config
     */
    suspend fun fetch() = runCatching {
        suspendCoroutine { continuation ->
            Firebase.remoteConfig.fetch()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }.getOrNull()

    /**
     * Fetch remote values from Remote Config
     */
    suspend fun fetchAndActive() = runCatching {
        suspendCoroutine<Boolean> { continuation ->
            Firebase.remoteConfig.fetchAndActivate()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }.getOrNull()

    /**
     * Activate fetched remote values
     */
    suspend fun activate() = runCatching {
        suspendCoroutine<Boolean> { continuation ->
            Firebase.remoteConfig.activate()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }.getOrNull()
}