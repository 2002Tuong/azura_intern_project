package com.wifi.wificharger

import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.wifi.wificharger.utils.Logger
import com.wifi.wificharger.di.appModule
import com.wifi.wificharger.di.repositoryModule
import com.wifi.wificharger.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : VioAdmobMultiDexApplication() {
//    private val openAdsManager: OpenAdsManager by inject()
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                listOf(appModule, repositoryModule, viewModelModule)
            )
        }
        Logger.setup(BuildConfig.DEBUG)
        initRemoteConfig()
        registerOpenAdsCallback()
        initAds()
    }

    private fun initAds() {
        val environment = if (BuildConfig.DEBUG) {
            VioAdmobConfig.ENVIRONMENT_DEVELOP
        } else {
            VioAdmobConfig.ENVIRONMENT_PRODUCTION
        }
        vioAdmobConfig = VioAdmobConfig(this, VioAdmobConfig.PROVIDER_ADMOB, environment)
        vioAdmobConfig.mediationProvider = VioAdmobConfig.PROVIDER_ADMOB
        val adjustConfig = AdjustConfig("adjustToken")
        vioAdmobConfig.adjustConfig = adjustConfig
        vioAdmobConfig.idAdResume = BuildConfig.app_open
        vioAdmobConfig.listDeviceTest = listTestDevice
        Admob.getInstance().setFan(true)
        VioAdmob.getInstance().init(this, vioAdmobConfig, false)
//        ReminderUtils.scheduleNextFullScreenReminder(this)
    }

    private fun initRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0L
        }
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
        }
    }

    private fun registerOpenAdsCallback() {
//        registerActivityLifecycleCallbacks(openAdsManager)
//        ProcessLifecycleOwner.get().lifecycle.addObserver(openAdsManager)
    }
}