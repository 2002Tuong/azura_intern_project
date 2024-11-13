package com.example.videoart.batterychargeranimation.data.remote

import android.util.Log
import com.example.videoart.batterychargeranimation.BuildConfig
import com.example.videoart.batterychargeranimation.extension.appVersionToInt
import com.example.videoart.batterychargeranimation.model.Category
import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object RemoteConfig {

    val isRemoteConfigLoaded = MutableStateFlow(false)
    private val gson = Gson()

    private const val THEME_LIST_KEY = "theme_list"
    private const val CATEGORY_KEY = "category"
    suspend fun waitRemoteConfigLoaded(): Boolean? {
        return isRemoteConfigLoaded.filter { it }.firstOrNull()
    }

    suspend fun fetchRemoteConfigAsync() {
        return suspendCancellableCoroutine { continuation ->
            Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                task.exception?.let { }
                isRemoteConfigLoaded.update { true }
                Log.d("RemoteConfig", "Load success")
                continuation.resume(Unit)
            }.addOnFailureListener {
                isRemoteConfigLoaded.update { true }
                Log.d("RemoteConfig", "Load failure")
            }.addOnCanceledListener {
                isRemoteConfigLoaded.update { true }
                Log.d("RemoteConfig", "Load cancel")
            }
        }
    }
    val cmpRequire: Boolean
        get() = Firebase.remoteConfig.getBoolean("cmp_require")
    val remoteVersion: String
        get() = Firebase.remoteConfig.getString("version_ads_enable")

    val localVersion: String
        get() = BuildConfig.VERSION_NAME
    val category: List<Category>?
        get() = fromJson(Firebase.remoteConfig.getString(CATEGORY_KEY))
    val remoteThemes: List<RemoteTheme>?
        get() = fromJson(Firebase.remoteConfig.getString(THEME_LIST_KEY))

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
            Log.d("AppFlyers", "${remoteVersion}")

            return currentVersionValue <= configVersionValue
        }

}