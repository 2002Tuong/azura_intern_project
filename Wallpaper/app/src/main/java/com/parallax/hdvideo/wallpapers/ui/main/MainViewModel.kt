package com.parallax.hdvideo.wallpapers.ui.main

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.ConfigInfo
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.MoreAppModel
import com.parallax.hdvideo.wallpapers.databinding.ItemSuggestionAppBinding
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.DefaultManager
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.EventData
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.services.worker.*
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.dialog.SetSoundWallpaperDialog
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.network.NetworkListener
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiClient: ApiClient,
    private val localStorage: LocalStorage,
    private val database: AppDatabase ): BaseViewModel() {
    val listener = NetworkListener()
    val hasSavedWallpaperLiveData = MutableLiveData<Boolean>()
    private var disposableOriginStorage: Disposable? = null
    val configState = MutableLiveData<Boolean>()
    private var configLoadedCount = 0
    var isHasConfig = false
    private var loadConfigDisposable: Disposable? = null
    private var bestStorageDisposable: Disposable? = null
    var listMoreAppModel = listOf<MoreAppModel>()
    var openSearchFromUI = "home"
    var useSearchFeature = false
    var arrayHashTag = ArrayList<HashTag>()

    val moreAppAdapter =  BaseAdapterList<MoreAppModel, ItemSuggestionAppBinding>({ R.layout.item_suggestion_app },
        onBind = { binding, model, pos ->
            binding.item = model
        })

    private var startTime = 0L

    init {
        isHasConfig = WallpaperURLBuilder.shared.setConfigUrl(localStorage)
    }

    override fun onCleared() {
        super.onCleared()
        bestStorageDisposable?.dispose()
        loadConfigDisposable?.dispose()
        disposableOriginStorage?.dispose()
    }

    @SuppressLint("NewApi")
    fun resetPageId() {
        val time = localStorage.openedLastTime
        val openedLastDate =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId())
        val currentDate = LocalDateTime.now()

        if ((currentDate.dayOfYear - openedLastDate.dayOfYear > 0)
            || (currentDate.year - openedLastDate.year > 0)
        ) {
            localStorage.pageNumberWall = 2
            localStorage.pageNumberVideo = 2
            localStorage.pageNumberParallaxTab4D = 2

            localStorage.openedLastTime = currentDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

    fun sendFeedback(content: String) {
        compositeDisposable.add(apiClient.getString(WallpaperURLBuilder.shared.getFeedbackUrl(content))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.d("sendFeedback", it)
            }, {})
        )
        localStorage.ratingApp = true
    }

    fun getAllHashtag() {
        compositeDisposable.add(apiClient.getHashTags(WallpaperURLBuilder.shared.getHashTagsUrl(localStorage.sex))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.d("all hashtag", it.data)
                arrayHashTag.clear()
                arrayHashTag.addAll(it.data)
            }, {})
        )

    }

    fun scheduleNotification() {
        if (!RemoteConfig.commonData.isSupportedDevice) return
        compositeDisposable.add(
            Completable.fromCallable {
                HashTagsNotificationWorker.cancel()
                NotificationWorker.cancel()
                val isOnNotification = localStorage.isOnNotification
                if (isOnNotification) {
                    val model = DefaultManager.loadNotificationData()
                    if (model != null) {
                    NotificationWorker.startSchedule(localStorage)
                }
            }
            DownloadNotificationWorker.schedule()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {  })
        NotificationUtils.subscribeTopics()
    }

    fun saveWallpaperVideo(uriString: String) {
        compositeDisposable.add(
            Single.fromCallable {
                PeriodicWallpaperChangeWorker.cancel()
                FileUtils.delete(localStorage.getString(AppConstants.PreferencesKey.KEY_SAVE_VIDEO_PATH))
                val file = FileUtils.copyFileFromUri(uriString)
                localStorage.putString(AppConstants.PreferencesKey.KEY_SAVE_VIDEO_PATH, file?.path)
                localStorage.isSoundingVideo = SetSoundWallpaperDialog.isSoundVideo
                file
            }.delay(200, TimeUnit.MILLISECONDS)
                .doOnSubscribe { showLoading() }
                .doFinally { dismissLoading() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file ->
                    hasSavedWallpaperLiveData.value = file != null
                    if (file != null)
                        PeriodicWallpaperChangeWorker.cancel()
                }, {
                    hasSavedWallpaperLiveData.value = false
                })
        )
    }

    fun cancelLastTimeChangeNotification() {
        localStorage.contextChangedWallpaperIndex = 0
        LastTimeModifyWallpaperWorker.cancel()
    }

    fun scheduleNotificationHashTags() {
        if (localStorage.isOnNotification && RemoteConfig.commonData.isSupportedDevice) {
            LastTimeModifyWallpaperWorker.schedule()
        }
    }

    fun getBestStorage() {
        bestStorageDisposable?.dispose()
        bestStorageDisposable =
        Completable.fromCallable {
            val storageUrl = localStorage.getString(AppConstants.PreferencesKey.KEY_VIDEO_BEST_STORAGE)
            if (!storageUrl.isNullOrEmpty()) {
                WallpaperURLBuilder.shared.videoStorage = storageUrl
            }
            val listStorage = RemoteConfig.commonData.checkVideoStorage.split(",")
            var time = 100_000L
            var bestURL = ""
            var bestURLSecond = ""
            for (url in listStorage) {
                val t = timeToGetFile(url.plus("/video7storage/check.txt "))
                if (t in 1 until time) {
                    time = t
                    bestURLSecond = bestURL
                    bestURL = url
                }
            }
            if (bestURL.isNotEmpty()) {
                val oldBestStorage = localStorage.getString(AppConstants.PreferencesKey.KEY_VIDEO_BEST_STORAGE_TMP)
                bestURL = bestURL.plus("/video7storage/")
                bestURLSecond = bestURLSecond.plus("/video7storage/")
                val best = if (bestURL.contains("spaces")) bestURLSecond else bestURL
                if (oldBestStorage == best) {
                    // ít nhất 2 hai gần lần ping tới server có kết quả giông nhau
                    localStorage.putString(AppConstants.PreferencesKey.KEY_VIDEO_BEST_STORAGE, best)
                } else {
                    localStorage.putString(AppConstants.PreferencesKey.KEY_VIDEO_BEST_STORAGE_TMP, best)
                }
                WallpaperURLBuilder.shared.videoStorage = best
                localStorage.bestStoreVideoDownload = bestURL
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})

    }

    private fun timeToGetFile(strUrl: String?): Long {
        var inputStream: InputStream? = null
        var httpConnection: HttpURLConnection? = null
        try {
            val time = System.currentTimeMillis()
            val url = URL(strUrl)
            httpConnection = url.openConnection() as HttpURLConnection
            httpConnection.readTimeout = 1000
            httpConnection.connectTimeout = 2000
            inputStream = url.openStream()
            return System.currentTimeMillis() - time
        } catch (e: Exception) {
            Logger.d(e, "timeToGetFile", strUrl)
        } finally {
            try {
                inputStream?.close()
                httpConnection?.disconnect()
            }catch (e : Exception) {}
        }
        return -1
    }

    fun getOriginStorage() {
        val interval = RemoteConfig.commonData.storageInterval.toLong()
        if (interval <= 0) return
        disposableOriginStorage?.dispose()
        disposableOriginStorage = Observable.interval(5, interval, TimeUnit.SECONDS, Schedulers.io())
            .flatMap {
                apiClient.getOriginStorage(WallpaperURLBuilder.shared.originStorageUrl)
                    .onErrorResumeNext(Observable.empty())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ res ->
                res?.fullStorage?.also {
                    if (it.isNotEmpty()) WallpaperURLBuilder.shared.fullStorage = it
                }

                res?.language?.also {
                    if (it.length > 3 && !it.contains(RemoteConfig.DEFAULT_LANGUAGE, true)) {
                        val lang = it.substring(3)
                        RemoteConfig.countryName = lang
                        WallpaperURLBuilder.shared.languageCountry = it
                        localStorage.countryName = lang
                    }
                }
                res?.storageOrigin?.also {
                    if (it.isNotEmpty()) {
                        WallpaperURLBuilder.shared.storageOrigin = it
                        disposableOriginStorage?.dispose()
                    }
                }
                Logger.d(" getOriginStorage", res.fullStorage)
            }, {
                disposableOriginStorage?.dispose()
            })
    }

    fun downloadAppConfig(isResetCount: Boolean = false) {
        if (RemoteConfig.onLoadedConfig) {
            if (configState.value != true)
                configState.postValue(true)
            return
        }
        TrackingSupport.recordEvent(EventData.GetConfig)
        if (isResetCount) configLoadedCount = 1
        else configLoadedCount += 1
        loadConfigDisposable?.dispose()
        loadConfigDisposable =
            getLanguage().flatMap {
                RemoteConfig.countryName = it
                apiClient.getString(WallpaperURLBuilder.shared.urlServerInfo2)
                        .flatMap { res ->
                            if (res.isNotEmpty() && !res.contains("firebase")) {
                                Single.just(res)
                            } else {
                                apiClient.getString(WallpaperURLBuilder.getURLByRegion(it, WallpaperURLBuilder.shared.urlServerInfo))
                            }
                        }
            }.flatMap { res ->
                val model = ConfigInfo.newInstance(res)
                if (model.commonData != null) {
                    localStorage.configApp = model
                    Single.just(model)
                } else {
                    getFirebaseRemote().map {
                        ConfigInfo.newInstance(it).also { con ->
                            localStorage.configApp = con
                        }
                    }
                }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({res ->
                        setupConfig(res)
                    }, {
                        reloadConfig()
                    })
    }

    fun getMoreApp() {
        val url = if (BuildConfig.DEBUG)
            "https://sg.dieuphoiwallpaper.xyz/wallforgirlgz/rest/apps?lang=en_VN&os=android&mobileid=ee3970e0ada2c447_sdk29900596658&token=female&appid=videogirlwallpapertkv2secv10"
        else WallpaperURLBuilder.shared.getMoreAppUrl(localStorage.sex)
        compositeDisposable.add(apiClient.getMoreApp(url).onErrorResumeNext(apiClient.getMoreApp(url))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.data.isNotEmpty()) {
                    moreAppAdapter.setData(it.data.subList(0, minOf(3, it.data.size)))
                }
                listMoreAppModel = it.data
            }, {
                Logger.d( "getMoreApp error")
            })
        )
    }

    private fun setupConfig(configInfo: ConfigInfo) {
        val commonData = configInfo.commonData
        if (commonData != null) {
            isHasConfig = true
            RemoteConfig.commonData = commonData
            RemoteConfig.onLoadedConfig = true
            WallpaperURLBuilder.shared.setConfigUrl()
//            BillingManager.recreateApiClient(App.instance, RemoteConfig.appId, App.instance.getString(R.string.app_name))
            configState.postValue(true)
        } else reloadConfig()
    }

    private fun reloadConfig() {
        if (NetworkUtils.isNetworkConnected()) {
            if (configLoadedCount < 2) {
                if (configLoadedCount <= 1) {
                    TrackingSupport.recordEvent(EventData.GetConfigFailed)
                }
                downloadAppConfig()
            } else {
                WallpaperURLBuilder.shared.setConfigUrl()
                configState.postValue(true)
            }
        }
    }

    private fun getLanguage(): Single<String> {
        return if (RemoteConfig.countryName == RemoteConfig.DEFAULT_LANGUAGE) {
            Single.create { emitter ->
                apiClient.getString(WallpaperURLBuilder.shared.urlCountry)
                    .subscribe({ res -> emitter.onSuccess(saveLanguage(res)) }, {
                        // Nếu bị lỗi thì gọi lại lần nữa
                        apiClient.getString(WallpaperURLBuilder.shared.urlCountry)
                            .subscribe({ res -> emitter.onSuccess(saveLanguage(res)) }, {
                                emitter.onSuccess(WallpaperURLBuilder.getCountryByLang())
                            })
                    })
            }
        } else {
            Single.just(RemoteConfig.countryName)
        }
    }

    private fun saveLanguage(res: String) : String {
        val language = res.trim().toUpperCase()
        return if (language.length == 2) {
            localStorage.countryName = language
            language
        } else {
            WallpaperURLBuilder.getCountryByLang()
        }
    }

    private fun getFirebaseRemote(): Single<String> {
        return Single.create {
            FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnCompleteListener { config ->
                if (!it.isDisposed) {
                    if (config.isSuccessful) {
                        val res = FirebaseRemoteConfig.getInstance()
                            .getString(getFirebaseRemoteKey("configs"))
                        if (res.isNotEmpty()) {
                            it.onSuccess(res)
                        } else {
                            it.onError(Throwable())
                        }
                    } else {
                        it.onError(Throwable())
                    }
                }
            }
        }
    }


    private fun getFirebaseRemoteKey(key: String): String {
        return key + "_" + WallpaperApp.instance.packageName.replace(".", "_")
    }

    companion object {
        fun setupNotificationHashTags(localStorage: LocalStorage) : Int {
            try {
                if (!localStorage.isOnNotification || !RemoteConfig.commonData.isSupportedDevice) return 0
                WallpaperURLBuilder.shared.setConfigUrl(localStorage)
                val list = RemoteConfig.commonData.scenarioNotify.split(";")
                val index = if (localStorage.openAppCount > 1) 1 else 0
                val durations = list[index].split(",")

                val lastConfig = localStorage.getData(AppConstants.PreferencesKey.LAST_INDEX_NOTIFICATION_HASH_TAGS, String::class)
                var current = 0
                if (lastConfig != null) {
                    val array = lastConfig.split(",")
                    val first = array[0].toInt()
                    current = array[1].toInt()
                    if (first == index) {
                        current += 1
                    }
                }
                if (current >= durations.size) current = 0
                localStorage.putData(AppConstants.PreferencesKey.LAST_INDEX_NOTIFICATION_HASH_TAGS, "$index,$current")
                val d = durations[current].toInt()
                val cal = Calendar.getInstance()
                val currentTime = cal.timeInMillis
                cal.add(Calendar.HOUR_OF_DAY, d)
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour in 22..23) {
                    cal.add(Calendar.HOUR_OF_DAY, 10) // từ 8h
                } else if (hour <= 7) {
                    cal.set(Calendar.HOUR_OF_DAY, 8)
                }
                val delay = ((cal.timeInMillis - currentTime) / (1000 * 3600)).toInt()
                return if (delay > 0) delay else 0
            }catch (e: Exception) {
                Logger.d("notificationOpenApp", e)
            }
            return 0
        }
    }
}