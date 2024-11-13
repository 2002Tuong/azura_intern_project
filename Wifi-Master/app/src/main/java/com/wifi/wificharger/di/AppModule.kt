package com.wifi.wificharger.di

import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.remote.RemoteConfig
import com.wifi.wificharger.utils.LanguageUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AppDataStore(androidContext()) }
    single { LanguageUtils(get()) }
//    single { AdsInitializer() }
//    single { AdsManager() }
//    single { BannerAdsManager(androidContext(), get(), get(), get()) }
//    single { OpenAdsManager(androidContext(), get(), get()) }
//    single { NativeAdsManager(androidContext(), get(), get()) }
//    factory { RewardAdsManager(androidContext(), get(), get(), get()) }
//    single { BillingClientLifecycle(androidContext(), get()) }
//    single { AppUpdateChecker(get(), get()) }
//    single { NotificationController(androidContext()) }
}
