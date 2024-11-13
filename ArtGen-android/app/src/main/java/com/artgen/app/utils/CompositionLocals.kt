package com.artgen.app.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.artgen.app.ads.AdsManager
import com.artgen.app.ads.OpenAdsManager
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.ui.screen.language.LanguageManager
import com.artgen.app.ui.screen.rating.RatingManager
import com.artgen.app.ui.screen.settings.ShareController

val LocalLanguageManager = staticCompositionLocalOf<LanguageManager> {
    error("LanguageManager is not provided")
}

val LocalAdsManager = staticCompositionLocalOf<AdsManager> {
    error("AdsManager is not provided")
}

val LocalAdUtil = staticCompositionLocalOf<AdsUtils> { error("LocalAdUtil is not provided") }

val LocalOpenAdsManager =
    staticCompositionLocalOf<OpenAdsManager> { error("LocalAppOpenAdsManager is not provided") }

val LocalRemoteConfig = staticCompositionLocalOf<RemoteConfig> {
    error("RemoteConfig is not provided")
}

val LocalShareController = staticCompositionLocalOf<ShareController> {
    error("ShareController is not provided")
}

val LocalRatingManager = staticCompositionLocalOf<RatingManager> {
    error("RatingManager is not provided")
}
