package com.bloodpressure.app.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.bloodpressure.app.ads.AdsManager
import com.bloodpressure.app.ads.BillingClientLifecycle
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.screen.home.settings.ShareController

val LocalLanguageManager = staticCompositionLocalOf<LanguageManager> {
    error("LanguageManager is not provided")
}

val LocalShareController = staticCompositionLocalOf<ShareController> {
    error("ShareController is not provided")
}

val LocalTextFormatter = staticCompositionLocalOf<TextFormatter> {
    error("TextFormatter is not provided")
}

val LocalBillingClientLifecycle = staticCompositionLocalOf<BillingClientLifecycle> {
    error("BillingClientLifecycle is not provided")
}

val LocalAdsManager = staticCompositionLocalOf<AdsManager> {
    error("AdsManager is not provided")
}

val LocalRemoteConfig = staticCompositionLocalOf<RemoteConfig> {
    error("RemoteConfig is not provided")
}
