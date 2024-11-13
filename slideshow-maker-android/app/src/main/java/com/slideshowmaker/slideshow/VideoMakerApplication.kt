package com.slideshowmaker.slideshow

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.RemoteException
import android.provider.Settings
import android.widget.Toast
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.application.VioAdmobMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.VioAdmobConfig
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.slideshowmaker.slideshow.broadcast.NetworkStateChange
import com.slideshowmaker.slideshow.data.*
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.Result
import com.slideshowmaker.slideshow.data.remote.VideoMakerServerInterface
import com.slideshowmaker.slideshow.data.response.IpInfoModelSetting
import com.slideshowmaker.slideshow.modules.audio_manager_v3.AudioManagerV3
import com.slideshowmaker.slideshow.modules.audio_manager_v3.AudioManagerV3Impl
import com.slideshowmaker.slideshow.modules.local_storage.LocalStorageData
import com.slideshowmaker.slideshow.modules.local_storage.LocalStorageDataImpl
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayerImpl
import com.slideshowmaker.slideshow.ui.HomeScreenViewModel
import com.slideshowmaker.slideshow.ui.music_picker.MusicPickerViewModel
import com.slideshowmaker.slideshow.ui.my_video.MyVideoViewModel
import com.slideshowmaker.slideshow.ui.pick_media.PickMediaViewModel
import com.slideshowmaker.slideshow.ui.picker.ImagePickerViewModel
import com.slideshowmaker.slideshow.ui.premium.PremiumPlanViewModel
import com.slideshowmaker.slideshow.ui.select_music.SelectMusicViewModel
import com.slideshowmaker.slideshow.ui.splash.SplashScreenActivity
import com.slideshowmaker.slideshow.ui.splash.SplashScreenViewModel
import com.slideshowmaker.slideshow.utils.extentions.isFirstInstall
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.*

class VideoMakerApplication : VioAdmobMultiDexApplication(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@VideoMakerApplication))

        bind<LocalStorageData>() with singleton { LocalStorageDataImpl() }
        bind() from provider { SelectMusicViewModel(instance()) }
        bind<AudioManagerV3>() with provider { AudioManagerV3Impl() }
        bind<MusicPlayer>() with provider { MusicPlayerImpl() }
        bind<VideoMakerServerInterface>() with singleton {
            val retrofit = Retrofit.Builder()
                .client(OkHttpClient.Builder().build())
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
            retrofit.create(VideoMakerServerInterface::class.java)
        }
        bind<SubscriptionRepository>() with singleton {
            SubscriptionRepository(
                this@VideoMakerApplication,
                RemoteConfigRepository, instance()
            )
        }
        bind<ImageRepository>() with singleton {
            ImageRepository(this@VideoMakerApplication)
        }
        bind<PremiumPlanViewModel>() with provider {
            PremiumPlanViewModel(instance(), instance())
        }
        bind<SplashScreenViewModel>() with provider {
            SplashScreenViewModel(instance())
        }
        bind<HomeScreenViewModel>() with provider {
            HomeScreenViewModel(instance(), this@VideoMakerApplication)
        }
        bind<ImagePickerViewModel>() with provider {
            ImagePickerViewModel(instance())
        }
        bind<MusicPickerViewModel>() with provider {
            MusicPickerViewModel(instance())
        }
        bind<PickMediaViewModel>() with provider {
            PickMediaViewModel(instance())
        }
        bind<MyVideoViewModel>() with provider {
            MyVideoViewModel(this@VideoMakerApplication)
        }
    }

    val restServer: VideoMakerServerInterface by instance()
    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        MobileAds.initialize(this)
        Firebase.initialize(this)
        RemoteConfigRepository.updateRealtimeListener()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(NetworkStateChange(), filter)
        checkInfoAndRegisterToken()
        if (SharedPreferUtils.isFirstLaunch && isFirstInstall()) {
            TrackingFactory.Common.firstLaunch().track()
            SharedPreferUtils.isFirstLaunch = false
        }
        ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
        initAds()
    }

    private fun initAds() {
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
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashScreenActivity::class.java)
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
    }


    private lateinit var referrerClient: InstallReferrerClient

    @SuppressLint("HardwareIds")
    private fun checkInfoAndRegisterToken() {
        val packageInfo = packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
        if ((SharedPreferUtils.hadRegisteredDeviceToken || packageInfo.firstInstallTime != packageInfo.lastUpdateTime) &&
            SharedPreferUtils.referrer == null
        ) {
            getAndSaveReferrer()
        }
        if (SharedPreferUtils.hadRegisteredDeviceToken || BuildConfig.FLAVOR != "PROD") return
        // Ignore if it's an update
        if (packageInfo.firstInstallTime != packageInfo.lastUpdateTime) {
            return
        }
        referrerClient = InstallReferrerClient.newBuilder(this).build()
        CoroutineScope(Dispatchers.IO).launch {
            val ipResponseResult = safeApiCall {
                restServer.getIpInfoAsync()
            }
            if (ipResponseResult is Result.Success<IpInfoModelSetting>) {
                SharedPreferUtils.countryCode = ipResponseResult.data.country
            }
            referrerClient.startConnection(object : InstallReferrerStateListener {

                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                registerToken(referrerClient.installReferrer.installReferrer)
                            } catch (exception: Exception) {
                                registerToken()
                            }
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            registerToken()
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            registerToken()
                        }
                    }
                    referrerClient.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                    registerToken()
                    referrerClient.endConnection()
                }
            })
        }
    }

    private fun getAndSaveReferrer() {
        referrerClient = InstallReferrerClient.newBuilder(this).build()
        CoroutineScope(Dispatchers.IO).launch {
            referrerClient.startConnection(object : InstallReferrerStateListener {

                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                SharedPreferUtils.referrer =
                                    referrerClient.installReferrer.installReferrer.orEmpty()
                            } catch (exception: RemoteException) {
                                SharedPreferUtils.referrer = ""
                            }
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            SharedPreferUtils.referrer = ""
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            SharedPreferUtils.referrer = ""
                        }
                    }
                    referrerClient.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                    SharedPreferUtils.referrer = ""
                    referrerClient.endConnection()
                }
            })
        }
    }

    @SuppressLint("HardwareIds")
    private fun registerToken(referrer: String = "") {
        SharedPreferUtils.referrer = referrer
        if (SharedPreferUtils.hadRegisteredDeviceToken) return
        CoroutineScope(Dispatchers.IO).launch {
            val country = SharedPreferUtils.countryCode ?: Locale.getDefault().country
            val result = safeApiCall {
                restServer.registerTokenAsync(
                    deviceId = Settings.Secure.getString(
                        contentResolver,
                        Settings.Secure.ANDROID_ID,
                    ),
                    country = country,
                    referrer = referrer,
                    installFromStore = packageManager.getInstallerPackageName(applicationContext.packageName)
                        ?.equals(GOOGLE_PLAY_INSTALLER_PACKAGE_NAME).orFalse(),
                )
            }
            if (result is Result.Success) {
                SharedPreferUtils.hadRegisteredDeviceToken = true
            }
        }
    }

    fun getNativeAds(): NativeAd? = null
    fun showToast(msg: String) {
        if (BuildConfig.DEBUG)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    companion object {
        private const val GOOGLE_PLAY_INSTALLER_PACKAGE_NAME = "com.android.vending"

        lateinit var instance: VideoMakerApplication
        fun getContext() = instance.applicationContext!!
        var languageSelectCount = 0
    }

}