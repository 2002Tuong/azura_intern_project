package com.artgen.app.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine

class AdsInitializer {

    private val isAdsInitialized = MutableStateFlow(false)

    suspend fun waitUntilAdsInitialized() {
        isAdsInitialized.filter { it }.firstOrNull()
    }

    suspend fun init(context: Context) {
        return suspendCancellableCoroutine { continuation ->
            MobileAds.initialize(context) {
                isAdsInitialized.update { true }
                continuation.resume(Unit)
            }
        }
    }

    fun clear() {
        isAdsInitialized.update { false }
    }
}
