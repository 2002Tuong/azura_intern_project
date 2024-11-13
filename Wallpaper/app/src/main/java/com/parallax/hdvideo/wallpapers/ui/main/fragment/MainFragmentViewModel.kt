package com.parallax.hdvideo.wallpapers.ui.main.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.ListWallpaperModel
import com.parallax.hdvideo.wallpapers.data.model.ServerInfoModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.fromJson
import com.parallax.hdvideo.wallpapers.extension.getStringFromAssets
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.EventData
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class MainFragmentViewModel @Inject constructor(private val apiClient: ApiClient,
                                                             private val localStorage: LocalStorage,
                                                             private val database: AppDatabase) : BaseViewModel() {

    private var listVideoWallpaper = listOf<WallpaperModel>()
    private var listImageWallpaper = listOf<WallpaperModel>()
    private var listparallaxWallpaper = listOf<WallpaperModel>()
    private var listHashTag = listOf<HashTag>()
    private val maxVideoNumber = 10
    private val maxParallaxNumber = 10
    private val spaceVideos = AppConstants.DISTANCE_VIDEOS_IN_HOME_SCREEN
    private var started = false
    private var hasLoadedDefaultVideo = false
    private var hasLoadedDefaultImage = false
    private val _isDataAvailableLiveData = MutableLiveData<Boolean>()
    private val downloadVideoComposite = CompositeDisposable()
    private var startTimeInMillis = 0L
    private val listVideosIsDownloading = mutableMapOf<String, WallpaperModel>()
    val loadMorePublishSubject = PublishSubject.create<List<WallpaperModel>>()
    val mainAdapter =
        FeaturesAdapter(scrollListener = object : MainFragmentAdapter.OnScrollListener {
            override fun loadMoreData() {
                //getData(isRefresh = false)
            }

            override fun downloadVideo(model: WallpaperModel) {
//                downloadThumbVideo(model)
            }
        })

    fun setup(fragment: MainFragment) {
        mainAdapter.requestManagerInstance = Glide.with(fragment)
        mainAdapter.isAutoPlayVideo = if (AppConfiguration.lowMemory) {
            localStorage.isAutoPlayVideo = false
            false
        } else false
    }

    override fun onCleared() {
        super.onCleared()
        downloadVideoComposite.dispose()
        mainAdapter.requestManagerInstance.onDestroy()
        mainAdapter.release()
    }

    fun getData(isRefresh: Boolean) {
        if (isRefresh) {
            val online = NetworkUtils.isNetworkConnected()
            if (!online) {
                NetworkUtils.pushNotification(online)
                return
            }
            listVideoWallpaper = listOf()
            listImageWallpaper = listOf()
            listparallaxWallpaper = listOf()
            hasLoadedDefaultVideo = false
            hasLoadedDefaultImage = false
            downloadVideoComposite.clear()
        }
        cancelAll()
//        if (RemoteConfig.commonInfo.isActiveServer) {
            loadDataOnHomeScreen(isRefresh)
//        } else {
//            loadWallpaperDefault(isRefresh)
//        }
    }

    private fun loadDataOnHomeScreen(isRefresh: Boolean) {
        startTimeInMillis = System.currentTimeMillis()
        compositeDisposable.add(Single.just(isRefresh).flatMap {
            getBestHashTags(it)
        }.flatMap {
            Single.zip(downloadVideoModels(it.second),
                downloadImageModels(it.first),
                downloadParallaxModels()
            ) { t1, t2, t3 ->
                Pair(it.third, mixWallpaper(t1, t2, t3))
            }
        }.map {
            val listWallpaper = it.second
            updateStatusFavoriteWallpaper(listWallpaper)
            Pair(it.first, listWallpaper)
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                if (result.first) {
                    listCache.clear()
                    listCache.addAll(result.second)
                    mainAdapter.setData(result.second)
                    Log.d("MainFragment", "add data 1")
                }
                else {
                    mainAdapter.addData(result.second)
                    listCache.addAll(result.second)
                    Log.d("MainFragment", "add data 2")
                }
                loadMorePublishSubject.onNext(result.second)
                dismissLoading()
                started = true
                _isDataAvailableLiveData.value = true
            }, {
                mainAdapter.onNextItemsLoaded()
                TrackingSupport.recordEvent(EventData.TimesOfLoadHomePageFail)
                if (mainAdapter.emptyData) {
                    loadWallpaperDefault()
                }
                dismissLoading()
                started = true
            }))
    }

    private fun updateStatusFavoriteWallpaper(listWall : List<WallpaperModel>) {
        compositeDisposable.add(database.wallpaper().selectFavorite(true).subscribe {
            listWall.forEach { wallpaperModel ->
                val found = it.any { it.id == wallpaperModel.id }
                if(found) wallpaperModel.isFavorite = true
            }
        })
    }

    private fun getBestHashTags(isRefresh: Boolean) : Single<Triple<String, String, Boolean>> {
        return Single.just(isRefresh).map {
            val lastHashTagVideo = localStorage.getString(AppConstants.PreferencesKey.BEST_HASH_TAGS_VIDEO) ?: RemoteConfig.hashTagDefault
            val lastHashTagImage = localStorage.getString(AppConstants.PreferencesKey.BEST_HASH_TAGS_IMAGE) ?: RemoteConfig.hashTagDefault
            if (it) {
                val hashTagVideo = database.hashTagsDao().getBestHashTags(isImage = false, limit = RemoteConfig.commonData.bestHashTagNum)
                if (lastHashTagVideo != hashTagVideo) {
                    localStorage.pageNumberVideo = 2
                    localStorage.putString(AppConstants.PreferencesKey.BEST_HASH_TAGS_VIDEO, hashTagVideo)
                }

                val hashTagWall = database.hashTagsDao().getBestHashTags(isImage = true, limit = RemoteConfig.commonData.bestHashTagNum)
                if (hashTagWall != lastHashTagImage) {
                    localStorage.pageNumberWall = 2
                    localStorage.putString(AppConstants.PreferencesKey.BEST_HASH_TAGS_IMAGE, hashTagWall)
                }
                Triple(hashTagWall, hashTagVideo, it)
            } else {
                Triple(lastHashTagImage, lastHashTagVideo, it)
            }
        }
    }

    private fun downloadImageModels(bestHashTags: String): Single<List<WallpaperModel>> {
        return if (listImageWallpaper.size < maxVideoNumber * spaceVideos) {
            Single.just(bestHashTags).map {
                Pair(it, localStorage.pageNumberWall.toString())
            }.flatMap { data ->
                    //server was broken -> load default
                    val url = WallpaperURLBuilder.shared.getImageUrlDefault(data.second, localStorage.sex, hashTags = data.first)
                    val task = if (hasLoadedDefaultImage) apiClient.getListWallpapers(url)
                    else {
                        val url2 = WallpaperURLBuilder.shared.urlDefaultInfo2.plus("?country=")
                            .plus(RemoteConfig.countryName).plus("&page=").plus(data.second)
                        apiClient.getListWallpapers(url)
                            .onErrorResumeNext(apiClient.getListWallpapers(url2).doAfterSuccess {
                                if (it.ServerInfo.firstOrNull()?.wallpapers?.isNotEmpty() == true) {
                                    hasLoadedDefaultImage = true
                                }
                            })
                    }
                    task
                    .map { res ->
                            val checkEmpty = res.ServerInfo.firstOrNull()?.wallpapers?.isEmpty() ?: false
                            //Get Image from default asset -> server broken
                            if (res.ServerInfo.isEmpty() || checkEmpty || true) {
                                if (!hasLoadedDefaultImage) {
                                    hasLoadedDefaultImage = true
                                    val serverInfoModel = ServerInfoModel()
                                    val m = ListWallpaperModel()
                                    m.country = RemoteConfig.countryName.toLowerCase(Locale.ENGLISH)
                                    m.wallpapers = defaultImages
                                    m.pageId = (localStorage.pageNumberWall + 1).toString()
                                    serverInfoModel.ServerInfo = mutableListOf(m)
                                    serverInfoModel
                                } else res
                            } else res
                        }
                }
                .map { res ->
                    val language = RemoteConfig.countryName.toLowerCase(Locale.ENGLISH)
                    val languageDefault = RemoteConfig.DEFAULT_LANGUAGE.toLowerCase(Locale.ENGLISH)
                    val list =
                        res.ServerInfo.filter { it.country == language || it.country == languageDefault }
                    val pageNumber = list.firstOrNull()?.pageId
                    localStorage.pageNumberWall = pageNumber?.toIntOrNull() ?: localStorage.pageNumberWall + 1
                    val listWallpapers = list.flatMap { it.wallpapers }.toMutableList()
                    if (!AppConfiguration.lowMemory && RemoteConfig.commonData.isSupportedDevice) {
                        listHashTag = listHashTag + list.flatMap { it.listHashTag }
                    }
                    val result = mutableListOf<WallpaperModel>()
                    result.addAll(listImageWallpaper)
                    result.addAll(listWallpapers)
                    result
                }
        } else Single.just(listImageWallpaper)
    }

    private fun mixWallpaperVsHashTag(
        listHashTags: MutableList<HashTag>,
        listWallAndVideos: MutableList<WallpaperModel>
    ) {
        var count = listWallAndVideos.size
        //pos = 7: check DWT-668: change distance of hashtag on home, consider to add remote config
        var pos = 7
        var flag = true
        while (pos < count) {
            if (flag) {
                flag = false
                if (listHashTags.size < 3) break
                listWallAndVideos.add(pos, WallpaperModel().also {
                    it.listHashTags = (0 until 3).map { listHashTags.removeFirst() }
                })
            } else {
                flag = true
                if (listHashTags.isEmpty()) break
                listWallAndVideos.add(pos, WallpaperModel().also {
                    it.listHashTags =  listOf(listHashTags.removeFirst())
                })
            }
            count = listWallAndVideos.size
            pos += 9
        }
        this.listHashTag = listHashTags
    }

    private fun downloadVideoModels(bestHashTagsVideo: String): Single<MutableList<WallpaperModel>> {
        return if (WallpaperHelper.isSupportLiveWallpaper) {
            if (RemoteConfig.commonData.isActiveServer && listVideoWallpaper.size < maxVideoNumber) {
                Single.just(Pair(bestHashTagsVideo, listVideoWallpaper.toMutableList())).flatMap { res ->
//                    Single.just(
//                        if (!hasLoadedDefaultVideo) {
//                            hasLoadedDefaultVideo = true
//                            ResponseModel(defaultVideos)
//                        } else ResponseModel()
//                    )
                    apiClient.getListWallpapersFromCategory(
                        WallpaperURLBuilder.shared.getVideoUrlDefault(
                            pageNumber = localStorage.pageNumberVideo.toString(),
                            gender = localStorage.sex,
                            hashTags = res.first
                        )
                    ).onErrorReturn {
                        if (!hasLoadedDefaultVideo) {
                            hasLoadedDefaultVideo = true
                            ResponseModel(defaultVideos)
                        }
                        else ResponseModel()
                    }
                        .map {
                            localStorage.pageNumberVideo++
                            val result = mutableListOf<WallpaperModel>()
                            result.addAll(res.second)
                            result.addAll(it.data.toMutableList())
                            if (result.isEmpty() && !hasLoadedDefaultVideo) {
                                hasLoadedDefaultVideo = true
                                defaultVideos
                            } else {
                                result
                            }
                        }
                }
            } else {
                Single.just(if (listVideoWallpaper.isNotEmpty()) {
                    listVideoWallpaper.toMutableList()
                } else if (!hasLoadedDefaultImage){
                    hasLoadedDefaultImage = true
                    defaultVideos
                } else mutableListOf())
            }
        } else Single.just(mutableListOf())
    }

    private fun downloadParallaxModels() : Single<MutableList<WallpaperModel>> {
        return if (WallpaperHelper.isSupportLiveWallpaper) {
            if (RemoteConfig.commonData.isActiveServer && listparallaxWallpaper.size < maxParallaxNumber) {
                Single.just(listparallaxWallpaper.toMutableList()).flatMap { res ->
                    apiClient.getListWallpapersFromCategory(
                        WallpaperURLBuilder.shared.getParallaxUrlDefault(localStorage.pageNumberParallaxTab4D.toString(), localStorage.sex)
                    ).map {
                        localStorage.pageNumberParallaxTab4D = if(it.pageId.isNullOrEmpty()) 2 else it.pageId!!.toInt()
                        val listResult = mutableListOf<WallpaperModel>()
                        listResult.addAll(res)
                        listResult.addAll(it.data.toMutableList())
                        listResult
                    }
                }
            } else {
                Single.just(if (listparallaxWallpaper.isNotEmpty()) {
                    listparallaxWallpaper.toMutableList()
                } else mutableListOf())
            }
        } else Single.just(mutableListOf())
    }

    private fun loadWallpaperDefault(isRefresh: Boolean = false) {
        compositeDisposable.add(Single.timer(200, TimeUnit.MILLISECONDS).map {
            if (!hasLoadedDefaultImage && !hasLoadedDefaultVideo) {
                hasLoadedDefaultImage = true
                hasLoadedDefaultVideo = true
                mixWallpaper(videos = defaultVideos, images = defaultImages, parallaxModels = mutableListOf(), isAll = true)
            } else listOf()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            //.doOnSubscribe {  if (!hasStarted) showLoading() }
            .subscribe({
                if (isRefresh)
                    mainAdapter.setData(it)
                else mainAdapter.addData(it)
                loadMorePublishSubject.onNext(it)
                dismissLoading()
                started = true
                _isDataAvailableLiveData.value = true
            }, {
                started = true
                dismissLoading()
            }))
    }

    private fun mixWallpaper(
        videos: MutableList<WallpaperModel>,
        images: List<WallpaperModel>,
        parallaxModels: MutableList<WallpaperModel>,
        isAll: Boolean = false
    ): List<WallpaperModel> {
        val result = mutableListOf<WallpaperModel>()
        var size1: Int
        var size2: Int
        val size = images.size
        var maxImageOfPage = if (isAll) size else maxVideoNumber * spaceVideos + maxParallaxNumber * spaceVideos
        maxImageOfPage = min(maxImageOfPage, size)
        val lastList = mutableListOf<WallpaperModel>()
        var isVideoSpot = true
        images.forEachIndexed { index, item ->
            if (index < maxImageOfPage) {
                result.add(item)
                size1 = videos.size
                size2 = parallaxModels.size
                if ((index + 1) % spaceVideos == 0) {
                    if (isVideoSpot) {
                        if (size1 > 0) {
                            result.add(videos.removeAt(0))
                            isVideoSpot = false
                        }
                    } else {
                        if (size2 > 0) {
                            result.add(parallaxModels.removeAt(0))
                            isVideoSpot = true
                        }
                    }
                }
            } else {
                lastList.add(item)
            }
        }
        listImageWallpaper = lastList
        listVideoWallpaper = videos
        listparallaxWallpaper = parallaxModels
        if (!AppConfiguration.lowMemory && RemoteConfig.commonData.isSupportedDevice) {
           // mixWallpaperVsHashTag(listHashTags.toMutableList(), result)
        }
        return result
    }

    private val defaultVideos: MutableList<WallpaperModel> get()  {
        return try {
            val json = getStringFromAssets(AppConstants.FILE_NAME_VIDEO_WALLPAPER) ?: ""
            fromJson<MutableList<WallpaperModel>>(json)
        }catch (e: Exception) { mutableListOf() }
    }

    private val defaultImages: MutableList<WallpaperModel> get()  {
        return try {
            val json = getStringFromAssets(AppConstants.FILE_NAME_IMAGE_WALLPAPER) ?: ""
            fromJson<MutableList<WallpaperModel>>(json)
        } catch (e: Exception) { mutableListOf() }
    }

    private fun downloadThumbVideo(model: WallpaperModel) {
        val id = model.id
        if (listVideosIsDownloading[id] != null) {
            return
        }
        val file = FileUtils.createTempFile(getFileName(model))
        if (file.exists()) {
            model.pathCacheVideo = file.path
            mainAdapter.playVideo()
            return
        }
        listVideosIsDownloading[id] = model
        downloadVideoComposite.add(Single.just(model).flatMap {
            val url = it.toUrl(isMin = false, isThumb = true, isVideo = true)
            Logger.d("downloadThumbVideo", url, listVideosIsDownloading.size)
            apiClient.downloadFile(url).map { res ->
                val f = FileUtils.write(res, fileName = getFileName(it))
                if (f.exists()) it.pathCacheVideo = f.path
                it.id
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                listVideosIsDownloading.remove(res)
                mainAdapter.playVideo()
            }, {
                listVideosIsDownloading.remove(id)
            })
        )
    }

    private fun getFileName(model: WallpaperModel): String {
        val url = model.url
        val last = if (url == null) {
            model.id.plus(".mp4")
        } else {
            FileUtils.getFileNameFromURL(url)
        }
        return "temp_video_$last"
    }

    fun cancelAll() {
        compositeDisposable.clear()
    }

    companion object {
        var listCache = mutableListOf<WallpaperModel>()
        fun upDateCacheList(wallpaper : WallpaperModel){
            listCache.forEachIndexed { index, item ->
                if(item.url == wallpaper.url){
                    listCache[index] = wallpaper
                    return@forEachIndexed
                }
            }
        }
    }
}