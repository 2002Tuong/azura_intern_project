package com.example.claptofindphone.presenter

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.example.claptofindphone.AdsUnitId
import com.example.claptofindphone.BuildConfig
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.presenter.common.FlashHelper
import com.example.claptofindphone.presenter.common.VibrationController
import com.example.claptofindphone.presenter.splash.SplashScreenActivity
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import java.util.Arrays
import javax.inject.Inject

@HiltAndroidApp
class ClapToFindApplication : VioAdmobMultiDexApplication() {


    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier

    private lateinit var mFlashHelper: FlashHelper
    private lateinit var mVibrationController: VibrationController
    override fun onCreate() {
        super.onCreate()
        myApplicationInstance = this
        mVibrationController = VibrationController(this)
        mFlashHelper = FlashHelper(this)
        MobileAds.initialize(this)
        val request = RequestConfiguration.Builder().setTestDeviceIds(listOf("E28DC7475FBE5549DDD6F5729BBC3B0E", "EC25F576DA9B6CE74778B268CB87E431"))
        MobileAds.setRequestConfiguration(request.build())
        initAds()
        initRemoteConfig()
        Log.d("Ads", "isTestDevice: ${AdRequest.Builder().build().isTestDevice(this)}")
    }

    private fun initAds() {
        val systemEnvironment = if (BuildConfig.is_debug) {
            VioAdmobConfig.ENVIRONMENT_DEVELOP
        } else {
            VioAdmobConfig.ENVIRONMENT_PRODUCTION
        }
        vioAdmobConfig = VioAdmobConfig(this, VioAdmobConfig.PROVIDER_ADMOB, systemEnvironment)
        vioAdmobConfig.mediationProvider = VioAdmobConfig.PROVIDER_ADMOB
        val adjustConfig = AdjustConfig("adjustToken")
        vioAdmobConfig.adjustConfig = adjustConfig
        vioAdmobConfig.idAdResume = AdsUnitId.app_open
        listTestDevice.add("33BE2250B43518CCDA7DE426D04EE231")
        listTestDevice.add("E28DC7475FBE5549DDD6F5729BBC3B0E")
        vioAdmobConfig.listDeviceTest = listTestDevice
        Admob.getInstance().setFan(true)
        VioAdmob.getInstance().init(this, vioAdmobConfig, false)
        AppOpenManager.getInstance().disableAppResumeWithActivity(AdActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashScreenActivity::class.java)
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
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

    fun getVibrationHelper() = mVibrationController

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startVibrate(vibrateType: Int, shouldRepeat: Boolean) {
        mVibrationController.vibrate(vibrateType, shouldRepeat)
    }

    fun stopVibrate() {
        mVibrationController.stopVibrate()
    }

    fun turnOnFlash(flashMode: Int, duration: Int) {
        mFlashHelper.turnOnFlash(flashMode, duration)
    }

    fun turnOfFlash() {
        mFlashHelper.turnOffFromOutSide()
    }

    companion object {

        private lateinit var myApplicationInstance: ClapToFindApplication
        fun getContext() = myApplicationInstance.applicationContext!!
        fun get(): ClapToFindApplication {
            return myApplicationInstance
        }
    }

    fun setActiveFindStatus(isActive: Boolean) {
        preferenceSupplier.setFindPhoneActivated(isActive)
    }
}