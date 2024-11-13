package com.example.videoart.batterychargeranimation

import android.util.Log
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.example.videoart.batterychargeranimation.di.localModule
import com.example.videoart.batterychargeranimation.di.viewModelModule
import com.example.videoart.batterychargeranimation.ui.batterycharger.AnimationChargerActivity
import com.example.videoart.batterychargeranimation.utils.ReminderUtils
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : VioAdmobMultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        initRemoteConfig()
        initAds()
        startKoin {
            androidContext(this@App)
            modules(listOf(viewModelModule, localModule))
        }
        Log.d("Firebase", "${FirebaseApp.getInstance().options.projectId}")

        ReminderUtils.scheduleNextFullScreenReminder(this)
    }

    private fun initRemoteConfig() {
        val remoteConfigSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }

        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(remoteConfigSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }

    }

    private fun initAds() {
        MobileAds.initialize(this)
        val environment = if (BuildConfig.is_debug) {
            VioAdmobConfig.ENVIRONMENT_DEVELOP
        } else {
            VioAdmobConfig.ENVIRONMENT_PRODUCTION
        }

        vioAdmobConfig = VioAdmobConfig(this, VioAdmobConfig.PROVIDER_ADMOB, environment)
        vioAdmobConfig.mediationProvider = VioAdmobConfig.PROVIDER_ADMOB
        val adjustConfig = AdjustConfig("adjustToken")
        vioAdmobConfig.adjustConfig = adjustConfig
        vioAdmobConfig.idAdResume = BuildConfig.appopen_resume
        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431")
        vioAdmobConfig.listDeviceTest = listTestDevice
        Admob.getInstance().setFan(true)
        VioAdmob.getInstance().init(this, vioAdmobConfig, false)
        AppOpenManager.getInstance().disableAppResumeWithActivity(AdActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(AnimationChargerActivity::class.java)
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
    }

    companion object {
        lateinit var instance: App
    }
}