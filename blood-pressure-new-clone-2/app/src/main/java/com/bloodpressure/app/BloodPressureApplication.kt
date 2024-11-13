package com.bloodpressure.app

import android.app.Application
import androidx.core.os.bundleOf
import androidx.lifecycle.ProcessLifecycleOwner
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.bloodpressure.app.ads.OpenAdsManager
import com.bloodpressure.app.di.appModule
import com.bloodpressure.app.di.databaseModule
import com.bloodpressure.app.di.viewModelModule
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.DefaultReminderManager
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.ReminderUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class BloodPressureApplication : Application() {

    private val openAdsManager: OpenAdsManager by inject()
    private val defaultReminderManager: DefaultReminderManager by inject()
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BloodPressureApplication)
            modules(
                listOf(appModule, databaseModule, viewModelModule)
            )
        }
        Logger.setup(BuildConfig.DEBUG)
        initRemoteConfig()
        registerOpenAdsCallback()
        ReminderUtils.scheduleNextFullScreenReminder(
            this,
        )

        TrackingManager.logEvent(
            "app_open_version",
            bundleOf("version_name" to BuildConfig.VERSION_NAME)
        )

        if (BuildConfig.FLAVOR != "dev")
            initAppsflyer()
    }

    private fun initAppsflyer() {
        AppsFlyerLib.getInstance()
            .init(getString(R.string.appsflyer_dev_key), object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    TrackingManager.logOnConversionDataSuccess()
                }

                override fun onConversionDataFail(p0: String?) {
                    TrackingManager.logOnConversionDataFail()
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    TrackingManager.logOnAppOpenAttribution()
                }

                override fun onAttributionFailure(p0: String?) {
                    TrackingManager.logOnAttributionFailure()
                }

            }, this)
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG);
        AppsFlyerLib.getInstance().start(this);
        val afRevenueBuilder = AppsFlyerAdRevenue.Builder(this)

        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build())

    }

    private fun initRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }

    private fun registerOpenAdsCallback() {
        registerActivityLifecycleCallbacks(openAdsManager)
        ProcessLifecycleOwner.get().lifecycle.addObserver(openAdsManager)
    }
}
