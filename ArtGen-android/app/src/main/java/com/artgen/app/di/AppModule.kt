package com.artgen.app.di

import com.artgen.app.ads.AdsInitializer
import com.artgen.app.ads.BillingClientLifecycle
import com.artgen.app.ads.InterAdsManager
import com.artgen.app.ads.NativeAdsManager
import com.artgen.app.ads.OpenAdsManager
import com.artgen.app.ads.RewardAdsManager
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.data.remote.repository.ArtGenRepository
import com.artgen.app.data.repository.PhotoRepository
import com.artgen.app.ui.screen.language.LanguageManager
import com.artgen.app.ui.screen.language.LanguageProvider
import com.artgen.app.ui.screen.updateapp.AppUpdateChecker
import com.artgen.app.utils.AdsUtils
import com.artgen.app.utils.NotificationController
import com.artgen.app.utils.TextFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { RemoteConfig() }
    single { AppDataStore(androidContext()) }
    single { LanguageProvider() }
    factory { LanguageManager() }
    single { AdsInitializer() }
    single { OpenAdsManager(androidContext(), get(), get()) }
    factory { InterAdsManager(androidContext(), get(), get()) }
    single { NativeAdsManager(androidContext(), get(), get()) }
    factory { RewardAdsManager(androidContext(), get(), get(), get()) }
    single { PhotoRepository(androidContext()) }
    single { ArtGenRepository(get(), androidContext(), get()) }
    single { AppUpdateChecker(get(), get()) }
    single { AdsUtils(get()) }
    single { NotificationController(androidContext()) }
    single { BillingClientLifecycle(androidContext(), get()) }
    single { TextFormatter(androidContext()) }
}
