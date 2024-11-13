package com.artgen.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.artgen.app.ads.OpenAdsManager
import com.artgen.app.di.appModule
import com.artgen.app.di.httpModule
import com.artgen.app.di.viewModelModule
import com.artgen.app.log.Logger
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.artgen.app.di.networkModule
import com.artgen.app.utils.ReminderUtils
import com.google.android.gms.ads.AdActivity

class ArtGenApplication : VioAdmobMultiDexApplication() {

    private val openAdsManager: OpenAdsManager by inject()
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ArtGenApplication)
            modules(
                listOf(appModule, viewModelModule, httpModule, networkModule)
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
//        vioAdmobConfig.idAdResume = getString(R.string.app_open_ad)
        vioAdmobConfig.listDeviceTest = listTestDevice
        Admob.getInstance().setFan(true)
        VioAdmob.getInstance().init(this, vioAdmobConfig, false)
        AppOpenManager.getInstance().disableAppResumeWithActivity(AdActivity::class.java)
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
        ReminderUtils.scheduleNextFullScreenReminder(this)
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
        registerActivityLifecycleCallbacks(openAdsManager)
        ProcessLifecycleOwner.get().lifecycle.addObserver(openAdsManager)
    }
}
