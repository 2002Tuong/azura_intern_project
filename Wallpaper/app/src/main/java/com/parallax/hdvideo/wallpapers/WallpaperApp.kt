package com.parallax.hdvideo.wallpapers

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.provider.Settings.Secure
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.google.android.gms.ads.MobileAds
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.AppInfo
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WallpaperApp: VioAdmobMultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var localStorage: LocalStorage
    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppConfiguration.setup(applicationContext)
        RemoteConfig.initPrivateKey(AppInfo.shared, null)
        RemoteConfig.countryName = localStorage.countryName
        RemoteConfig.ANDROID_ID = Secure.getString(contentResolver, Secure.ANDROID_ID) + "_sdk" + Build.VERSION.SDK_INT
        MobileAds.initialize(this) {}
        MobileAds.setAppMuted(true)
        initAds()
    }


    private fun initAds() {
        val systemEnvironment = if (BuildConfig.DEBUG) {
            VioAdmobConfig.ENVIRONMENT_DEVELOP
        } else {
            VioAdmobConfig.ENVIRONMENT_PRODUCTION
        }

        vioAdmobConfig = VioAdmobConfig(this, VioAdmobConfig.PROVIDER_ADMOB, systemEnvironment)
        vioAdmobConfig.mediationProvider = VioAdmobConfig.PROVIDER_ADMOB
        val adjustConfig = AdjustConfig("adjustToken")
        vioAdmobConfig.adjustConfig = adjustConfig
        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431")
        vioAdmobConfig.listDeviceTest = listTestDevice
        Admob.getInstance().setFan(true)
        VioAdmob.getInstance().init(this, vioAdmobConfig, false)
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
    }
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    companion object {
        lateinit var instance: WallpaperApp
    }
}