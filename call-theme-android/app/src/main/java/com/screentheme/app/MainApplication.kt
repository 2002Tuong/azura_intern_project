package com.screentheme.app

import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.data.remote.config.RemoteConfig
import com.screentheme.app.di.appModule
import com.screentheme.app.di.viewModelModule
import com.screentheme.app.utils.Logger
import com.screentheme.app.utils.ReminderUtils
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : VioAdmobMultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                listOf(appModule, /*repositoryModule,*/ viewModelModule)
            )
        }
        Logger.setup(BuildConfig.DEBUG)
        initRemoteConfig()
        initAds()
        ReminderUtils.scheduleNextFullScreenReminder(this)
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
        vioAdmobConfig.idAdResume = BuildConfig.appopen_resume
        vioAdmobConfig.listDeviceTest = listTestDevice
        Admob.getInstance().setFan(true)
        VioAdmob.getInstance().init(this, vioAdmobConfig, false)
    }

    private fun initRemoteConfig() {
        RemoteConfig.initialize {
            minimumFetchIntervalInSeconds = 0L
            registerModels(AppRemoteConfig)
        }
    }
}