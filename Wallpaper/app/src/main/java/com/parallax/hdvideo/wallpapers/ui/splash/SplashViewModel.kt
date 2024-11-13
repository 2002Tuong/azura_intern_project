package com.parallax.hdvideo.wallpapers.ui.splash

import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val api: ApiClient,
                                                   private val storage: LocalStorage,
                                                   private val database: AppDatabase): BaseViewModel() {

//    val nexScreen = MutableLiveData<Boolean>()
//
//    fun getAppConfig() {
//        if (isLoading) {
//            return
//        }
//        isLoading = true
//        composite.add(
//            getLanguage().flatMap {
//                RemoteConfig.countryName = it
//                api.getString(WallpaperURLBuilder.shared.urlServerInfo2)
//                    .flatMap {res ->
//                        if (res.isNotEmpty() && !res.contains("firebase")) {
//                            Single.just(res)
//                        } else {
//                            api.getString(WallpaperURLBuilder.getURLByRegion(it, WallpaperURLBuilder.shared.urlServerInfo))
//                        }
//                }
//            }.flatMap {res ->
//                val model = ConfigModel.newInstance(res)
//                if (model.commonInfo != null) {
//                    storage.putString(Constants.PreferencesKey.CONFIG_DATA_APP, res)
//                    Single.just(model)
//                } else {
//                    getFirebaseRemote().map {
//                        if (it.isNotEmpty()) storage.putString(Constants.PreferencesKey.CONFIG_DATA_APP, it)
//                        ConfigModel.newInstance(it)
//                    }
//                }
//            }.doFinally { isLoading = false }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({res -> setupConfig(res)
//            }, {
//                if (it.isOnline())
//                    handleError()
//            }))
//    }
//
//    private fun handleError() {
//        val model = storage.getData(Constants.PreferencesKey.CONFIG_DATA_APP, ConfigModel::class)
//        if (model == null) {
//            nexScreen.postValue(false)
//            FirebaseAnalyticHelper.recordEvent(
//                FirebaseAnalyticHelper.Type.LoadedConfigFailed)
//        } else {
//            RemoteConfig.countryName = storage.countryName
//            setupConfig(model)
//        }
//    }
//
//    private fun getLanguage(): Single<String> {
//
//        return Single.create(SingleOnSubscribe<String> {emitter ->
//            api.getString(WallpaperURLBuilder.shared.urlCountry)
//                .subscribe({ res -> emitter.onSuccess(saveLanguage(res)) }, {
//                    // Nếu bị lỗi thì gọi lại lần nữa
//                    api.getString(WallpaperURLBuilder.shared.urlCountry)
//                        .subscribe({ res -> emitter.onSuccess(saveLanguage(res)) }, {
//                            emitter.onSuccess(storage.countryName)
//                        })
//                })
//        })
//    }
//
//    private fun saveLanguage(res: String) : String {
//        val lang = res.trim().toUpperCase()
//        return if (lang.length == 2) {
//            storage.countryName = lang
//            lang
//        } else {
//            storage.countryName
//        }
//    }
//
//    private fun setupConfig(configModel: ConfigModel) {
//        val model = configModel.commonInfo
//        if (model == null || !NetworkUtils.isNetworkConnected()) {
//            nexScreen.postValue(false)
//            return
//        }
//        if (!TextUtils.isEmpty(model.urlDefaultWall)) {
//            val list = model.urlDefaultWall.split(",")
//            if (list.size > 1) {
//                WallpaperURLBuilder.shared.urlDefaultInfo2 = list[1]
//            }
//            WallpaperURLBuilder.shared.urlDefaultInfo = list[0]
//        }
//
//        RemoteConfig.commonInfo = model
//        WallpaperURLBuilder.shared.setConfigUrl()
//        nexScreen.postValue(true)
//    }
//
//    private fun getFirebaseRemote(): Single<String> {
//        return Single.create(SingleOnSubscribe<String> {
//            FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnCompleteListener {config ->
//                if (!it.isDisposed) {
//                    if (config.isSuccessful) {
//                        val res = FirebaseRemoteConfig.getInstance()
//                            .getString(getFirebaseRemoteKey("configs"))
//                        if (res.isNotEmpty()) {
//                            it.onSuccess(res)
//                        } else {
//                            it.onError(Throwable())
//                        }
//                    } else {
//                        it.onError(Throwable())
//                    }
//                }
//            }
//        })
//    }
//
//
//    private fun getFirebaseRemoteKey(key: String): String {
//        return key + "_" + App.instance.packageName.replace(".", "_")
//    }
}