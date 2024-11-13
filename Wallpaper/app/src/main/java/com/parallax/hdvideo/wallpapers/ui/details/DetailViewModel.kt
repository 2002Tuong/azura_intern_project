package com.parallax.hdvideo.wallpapers.ui.details

import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isNullOrEmptyOrBlank
import com.parallax.hdvideo.wallpapers.extension.removeFirst
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.*
import com.parallax.hdvideo.wallpapers.services.worker.PeriodicWallpaperChangeWorker
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.dialog.SetSoundWallpaperDialog
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import com.parallax.hdvideo.wallpapers.utils.other.ImageHelper
import com.parallax.hdvideo.wallpapers.utils.other.SingleLiveEvent
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min


@HiltViewModel
class DetailViewModel @Inject constructor(private val apiClient: ApiClient,
                                                       private val database: AppDatabase,
                                                       private val storage: LocalStorage): BaseViewModel() {

    private val _photoLiveData = MutableLiveData<Pair<Int, WallpaperModel?>>()
    private val _videoLiveData = MutableLiveData<Pair<Int, WallpaperModel?>>()
    private val _parallaxLiveData = MutableLiveData<WallpaperModel>()
    val _loadMoreDataState = MutableLiveData<Boolean>()
    private var downloadVideoDisposable: Disposable? = null
    private var favoriteDisposable: Disposable? = null
    private lateinit var hashTagModel: WallpaperModel
    private var pageNumber = 1
    private var loadingMore = false
    private var hasLoadFirstVideo = false
    var screenName: String = ""
    var screenType: AppScreen = AppScreen.HOME
    private var loadMoreData = true
    private var timeStartedDownload = 0L
    private val downloadingTask = mutableSetOf<Int>()
    private var lastPos = 0
    var detailAdapter = DetailV2Adapter { position, model ->
        downloadThumbVideo(position, model)
    }
    var count = 0
    var hasDownloadedWallpaper = false
    var setWallLiveEvent = SingleLiveEvent<Boolean>()
    var listWallFromCategories = mutableListOf<WallpaperModel>()
    private var loadMoreVideo = true
    private var loadMorePhoto = true
    private var listVideoWallpaper = listOf<WallpaperModel>()
    private var listParallaxWallpaper = listOf<WallpaperModel>()
    private var pageNumVideo = 0
    private var pageNumImage= 1
    private val distanceVideos = AppConstants.DISTANCE_VIDEOS_IN_HOME_SCREEN
    private val maxNumOfVideo = 10

    override fun onCleared() {
        super.onCleared()
        cancelDownloadVideo()
        detailAdapter.release()
        favoriteDisposable?.dispose()
    }

    fun downloadData(code: Int, position: Int) {
        val wallModel = detailAdapter.getDataOrNull(position) ?: return
        if (wallModel.isFromLocalStorage) return

        when (wallModel.getWallpaperModelType()) {
            WallpaperModel.WallpaperModelType.PARALLAX -> downloadFileParallax(wallModel)
            WallpaperModel.WallpaperModelType.VIDEO -> downloadVideo(code, wallModel)
            WallpaperModel.WallpaperModelType.IMAGE -> downloadPhoto(code, wallModel)
        }

        statisticHashTags(wallModel)
        if (code != BaseFragment.PREVIEW_CODE) {
            logDownloadWallpaper(wallModel)
        }
    }

    private fun downloadFileParallax(item: WallpaperModel) {
        val count = item.count?.toIntOrNull() ?: return
        val listImageUrl = ArrayList<String>()
        val domain =  detailAdapter.getDomainParallax4D(item)
        val bgModel = domain + AppConstants.IMAGE_BACKGROUND_NAME
        listImageUrl.add(bgModel)
        (1..count).forEach {
            val url = domain + "${AppConstants.IMAGE_LAYER}$it.png"
            listImageUrl.add(url)
        }

        handleDownloadImageFromURL(listImageUrl,item)
    }

    private fun handleDownloadImageFromURL(listImages: ArrayList<String>, item: WallpaperModel) {
        showLoading()
        val folder = File(FileUtils.folderParallax.path + "/" + getNameFolder(item))
        CoroutineScope(Dispatchers.IO).launch {
            listImages.forEach {
                val fileSource = GlideSupport.download(it).get()
                FileUtils.copyFile(fileSource, File(folder.path + "/" + getNameImage(it)))

                folder.listFiles()?.let { listFiles ->
                    if (listFiles.size == item.count!!.toInt()) {
                        dismissLoading()

                        item.apply {
                            hasDownloaded = true
                            pathCacheFull = folder.path
                        }
                        saveWallpaperToDatabase(item)
                    }
                }
            }
        }
    }

    private fun saveWallpaperToDatabase(item: WallpaperModel) {
        compositeDisposable.add(
            Single.just(item)
                .flatMapCompletable {
                    val wallModel = database.wallpaper().selectFirstItem(item.id)
                    if (wallModel != null) {
                        database.wallpaper().updateDownload(item.id, item.hasDownloaded)
                    } else {
                        database.wallpaper().insert(item)
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    dismissLoading()
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    _parallaxLiveData.value = item
                }, {

                })
        )
    }

    private fun getNameImage(url: String): String{
        val listInfo = url.split("/")
        return listInfo[listInfo.size-1]
    }

    fun getNameFolder(item: WallpaperModel): String {
        val listInfo = item.url!!.split("/")
        return if (item.url == null) "" else listInfo[listInfo.size - 2]
    }

    private fun downloadPhoto(code: Int, item: WallpaperModel) {
        compositeDisposable.add(
            Single.just(item)
                .flatMapCompletable {
                    val url = it.toUrl(isMin = false, isThumb = false)
                    val file = GlideSupport.download(url).get()
                    FileUtils.addImageToGallery(file, url)
                    it.hasDownloaded = true
                    it.pathCacheFull = file.path
                    if (abs(code) == BaseFragment.DOWNLOAD_CODE) {
                        val model = database.wallpaper().selectFirstItem(it.id)
                        if(!it.hasDownloaded) FileUtils.addImageToGallery(file, url)
                        it.hasDownloaded = true
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadSuccessImage)
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadSuccessAll)
                        if (model != null) {
                            database.wallpaper().updateDownload(it.id, it.hasDownloaded)
                        } else {
                            database.wallpaper().insert(it)
                        }
                    } else Completable.complete()
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    showLoading()
                    timeStartedDownload = System.currentTimeMillis()
                }
                .doFinally {
                    dismissLoading()
                }
                .subscribe({
                    _photoLiveData.value = code to item
                    hasDownloadedWallpaper = true
                }, {
                    _photoLiveData.value = code to null
                    TrackingSupport.recordEventOnlyFirebase(FailDownload.FailDownLoadUserEvent.DownloadFail)
                })
        )
    }

    fun getPhotoLiveData(): LiveData<Pair<Int, WallpaperModel?>> {
        return _photoLiveData
    }

    fun getVideoLiveData(): LiveData<Pair<Int, WallpaperModel?>> {
        return _videoLiveData
    }

    fun getParallaxLiveData() = _parallaxLiveData

    private fun downloadVideo(code: Int, model: WallpaperModel) {
        if(model.isFromLocalStorage) {
            _videoLiveData.value = code to model
            return
        }
        cancelDownloadVideo()
        val url = model.toUrl(isMin = false, isThumb = false, isVideo = true, isDownloadingFullVideo = true)
        val file = FileUtils.createTempFile(FileUtils.getFileNameFromURL(url))
        val isFileExists = file.exists()
        val requestTask = if (isFileExists) {
            TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadSuccessVideo)
            TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadSuccessAll)
            Single.just(file)
        } else {
            val retryUrl = model.toUrl(isMin = false, isThumb = false, isVideo = true, isDownloadingFullVideo = true)
            Single.just(model).flatMap {
                apiClient.downloadFile(url)
                    .onErrorResumeNext(apiClient.downloadFile(retryUrl).doAfterSuccess {
                        Logger.d("retryDownloadVideo response", retryUrl)
                    })
                    .map {
                        val newFile = FileUtils.write(it, fileName = FileUtils.getFileNameFromURL(url))
                        if (newFile.exists()) {
                            hasDownloadedWallpaper = true
                            TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadSuccessVideo)
                            TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadSuccessAll)
                        }
                        newFile
                    }
            }
        }

        downloadVideoDisposable = requestTask
            .flatMap { file ->
                if (file.exists() && abs(code) == BaseFragment.DOWNLOAD_CODE) {
                    if(!model.hasDownloaded) FileUtils.addImageToGallery(file, url)
                    val wallModelDb = database.wallpaper().selectFirstItem(model.id)
                    model.hasDownloaded = true
                    if (wallModelDb != null) {
                        database.wallpaper().updateDownload(model.id, model.hasDownloaded).toSingleDefault(file)
                    } else {
                        database.wallpaper().insert(model).toSingleDefault(file)
                    }
                } else Single.just(file)
            }
            .doOnSubscribe {
                showLoading(touchOutside = false, canGoBack = true)
                timeStartedDownload = System.currentTimeMillis()
            }
            .doFinally {
                dismissLoading()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                model.pathCacheFullVideo = res?.path
                if (res?.path == null) {
                    _videoLiveData.value = code to null
                } else {
                    _videoLiveData.value = code to model
                }
            }, {
                _videoLiveData.value = code to null
                TrackingSupport.recordEventOnlyFirebase(FailDownload.FailDownLoadUserEvent.DownloadFail)
            })
    }

    private fun downloadParallaxModel(item: WallpaperModel) {
        compositeDisposable.add(
            Single.just(item)
                .flatMapCompletable {
                    val url = it.toUrl(isMin = false, isThumb = false)
                    val file = GlideSupport.download(url).get()
                    it.hasDownloaded = true
                    val model = database.wallpaper().selectFirstItem(it.id)
                    if (model != null) {
                        database.wallpaper().updateDownload(it.id, it.hasDownloaded)
                    } else {
                        database.wallpaper().insert(it)
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    showLoading()
                }
                .doFinally {
                    dismissLoading()
                }
                .subscribe({
//                    photoLiveData.value = code to item
//                    hasDownloadedWall = true
                }, {
//                    photoLiveData.value = code to null
                })
        )
    }

    fun favoriteWallpaper(position: Int, completion: ((WallpaperModel?) -> Unit)) {
        val wallModel = detailAdapter.getDataOrNull(position) ?: return
        wallModel.isFavorite = !wallModel.isFavorite
        favoriteDisposable?.dispose()
        val disposable = Single.just(wallModel).flatMapCompletable {
            val first = getWallpaperFromDatabase(it.id)
            if (first != null) {
                database.wallpaper().updateFavorite(it.id, it.isFavorite)
            } else {
                database.wallpaper().insert(it)
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { }
            .doFinally { }
            .subscribe({
                completion.invoke(wallModel)
            }, {
                completion.invoke(null)
            })
        favoriteDisposable = disposable
        if (wallModel.isFavorite) {
            statisticHashTags(wallModel)
            logFavoriteWallpaper(wallModel)
            TrackingSupport.recordEventOnlyFirebase(EventFavoriteWall.FavoriteWallpaper)
        }
    }

    fun favoriteWallpaper(wallpaperModel: WallpaperModel, completion: ((WallpaperModel?) -> Unit)) {
        val wallModel = wallpaperModel
        wallModel.isFavorite = !wallModel.isFavorite
        favoriteDisposable?.dispose()
        val disposable = Single.just(wallModel).flatMapCompletable {
            val firstModel = getWallpaperFromDatabase(it.id)
            if (firstModel != null) {
                database.wallpaper().updateFavorite(it.id, it.isFavorite)
            } else {
                database.wallpaper().insert(it)
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { }
            .doFinally { }
            .subscribe({
                completion.invoke(wallModel)
            }, {
                completion.invoke(null)
            })
        favoriteDisposable = disposable
        if (wallModel.isFavorite) {
            statisticHashTags(wallModel)
            logFavoriteWallpaper(wallModel)
            TrackingSupport.recordEventOnlyFirebase(EventFavoriteWall.FavoriteWallpaper)
        }
    }

    fun setWallpaper(position: Int, type: WallpaperHelper.WallpaperType) {
        val model = detailAdapter.getDataOrNull(position) ?: return
        setWallpaper(model, type)
    }

    @SuppressLint("CheckResult")
    fun setWallpaper(model: WallpaperModel, type: WallpaperHelper.WallpaperType) {
        addTrackingSetWallpaper(model, type)
        compositeDisposable.add(Single.just(model).map {
            if(it.isFromLocalStorage) {
                GlideSupport.getBitmap(it.url ?: "",  RequestOptions().apply {
                    override(AppConfiguration.widthScreenValue, AppConfiguration.heightScreenValue)
                    skipMemoryCache(true)
                    diskCacheStrategy(DiskCacheStrategy.NONE)
                })
            } else {
                val url = it.toUrl(isMin = false, isThumb = false)
                val file = GlideSupport.download(url).get()
                ImageHelper.decodeBitmap(file.path)
            }.let { bitmap ->
                WallpaperHelper.setWallpaper(bitmap, type, wallModel= model)
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading(canGoBack = true) }
            .subscribeOn(Schedulers.io())
            .doFinally { dismissLoading() }
            .subscribe({
                if (it) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        PeriodicWallpaperChangeWorker.cancel()
                    } else if (storage.shouldAutoChangeWallpaper) {
                        storage.getData(AppConstants.PreferencesKey.AUTO_CHANGE_WALLPAPER_SCREEN_TYPE, Int::class)?.let { index ->
                            val autoType = WallpaperHelper.WallpaperType.init(index)
                            if (autoType == WallpaperHelper.WallpaperType.BOTH || autoType == type)
                                PeriodicWallpaperChangeWorker.cancel()
                        }
                    }
                    hasDownloadedWallpaper = true
                    setWallLiveEvent.value = it
                } else {
                    showToast(R.string.setting_wallpaper_error)
                }
            }, {
                TrackingSupport.recordEvent(EventSetWall.SetWallpaperImageFail)
                showToast(R.string.setting_wallpaper_error)
            })
        )
        statisticHashTags(model)
    }

    private fun addTrackingSetWallpaper(model: WallpaperModel, type: WallpaperHelper.WallpaperType) {
        TrackingSupport.recordSetWallEvent(type, model)
    }

    fun cancelDownloadVideo() {
        downloadVideoDisposable?.also { it.dispose() }
        downloadVideoDisposable = null
    }

    fun getWallpaperFromDatabase(id: String) = database.wallpaper().selectFirstItem(id)

    fun deleteWallpaperFromDatabase(model: WallpaperModel, callback: ((WallpaperModel) -> Unit)) {
        compositeDisposable.add(Single.just(model).flatMapCompletable {
            val wallpaperDao = database.wallpaper()
            if (model.is4DImage) {
                deleteFolder4DImage(model)
            }
            it.hasDownloaded = false
            it.hasShownRewardAd = false
            wallpaperDao.selectFirstItem(it.id)?.let { res ->
                if (res.isFavorite || res.isPlaylist) {
                    wallpaperDao.updateDownload(it.id, false).andThen(wallpaperDao.updateRewardAd(it.id, false))
                } else {
                    wallpaperDao.delete(it.id)
                }
            } ?: wallpaperDao.delete(it.id)
        }.observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .subscribeOn(Schedulers.io())
            .doFinally { dismissLoading() }
            .subscribe({
                callback.invoke(model)
                showToast(R.string.remove_wallpaper_successfully)
            }, {
                showToast(R.string.remove_wallpaper_failed)
            })
        )
    }

    fun getListWallpaper() {
        if (loadingMore || !loadMoreData) return
        loadingMore = true
        WallpaperURLBuilder.shared.setConfigUrl(storage)
        if (!this::hashTagModel.isInitialized) {
            hashTagModel = detailAdapter.getDataOrNull(detailAdapter.currentPos) ?: WallpaperModel()
        }
        if (hashTagModel.id.isEmpty()) {
            return
        }
        val check = hashTagModel.hashTag?.run {
            val str = trim()
            (str.isEmpty() || str == ",")
        } ?: true
        val hashTag = (if (check) hashTagModel.name else hashTagModel.hashTag) ?: return
        val url = if (hashTagModel.isVideo) WallpaperURLBuilder.shared.getVideoSearchUrl(hashTag, storage.sex, pageNumber = pageNumber, isRelative = true)
        else WallpaperURLBuilder.shared.getHashTagsUrl(hashTag, storage.sex, pageNumber = pageNumber, isHashTag = !check)
        compositeDisposable.add(
                apiClient.getListWallpapersFromCategory(url, expiredTimeInMillis)
                        .map {
                            val listWall = it.data
                            for ((index, item) in listWall.withIndex()) {
                                if (item.url == hashTagModel.url || item.id == hashTagModel.id) {
                                    listWall.removeAt(index)
                                    break
                                }
                            }
                            loadMoreData = it.hasNext
                            listWall
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ res ->
                            detailAdapter.addData(res)
                            if (res.isNotEmpty()) {
                                pageNumber++
                            }
                            loadingMore = false
                            _loadMoreDataState.value = res.isNotEmpty()
                        }, {
                            loadingMore = false
                            _loadMoreDataState.value = false
                        })
        )
    }

    //on click image item on collection fragment
    fun loadMoreCollectionData(categoryId: String?) {
        if (loadingMore || categoryId.isNullOrBlank()
            || categoryId == Category.VIDEO_CATEGORY_ID) return
        loadingMore = true
        compositeDisposable.add(Single.just(categoryId)
            .flatMap { id ->
                Single.zip(imageResources(id, pageNumImage), videoResources(id, pageNumVideo)
                ) { image, video ->
                    removeImagesFromCategory(image.data)
                    if (image.data.isNotEmpty()) pageNumImage++
                    if (video.data.isNotEmpty()) pageNumVideo++
                    loadMorePhoto = image.hasNext
                    loadMoreVideo = video.hasNext
                    mixSearchResults(images = image.data, video.data)
                }
            }
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .doFinally {
                loadingMore = false
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                if (res.isNotEmpty()) {
                    detailAdapter.addData(res)
                }
                _loadMoreDataState.value = res.isNotEmpty()
            }, {
                _loadMoreDataState.value = false
            })
        )
    }

    private fun deleteFolder4DImage(model: WallpaperModel) {
        if (File(FileUtils.folderParallax.path + "/" + getNameFolder(model)).exists()) {
            File(FileUtils.folderParallax.path + "/" + getNameFolder(model)).deleteRecursively()
        }
    }

    private fun mixSearchResults(images : List<WallpaperModel>, videosServer : List<WallpaperModel>) : List<WallpaperModel> {
        val listVideos = listVideoWallpaper + videosServer
        listVideoWallpaper = listOf()
        return when {
            listVideos.isEmpty() -> {
                images
            }
            images.isEmpty() -> {
                listVideos
            }
            else -> {
                val listResult = mutableListOf<WallpaperModel>()
                val vidSize = listVideos.size
                val vidDistance = 5
                var vidPosition = 0
                listResult.add(listVideos[0])
                images.forEachIndexed { index, item ->
                    listResult.add(item)
                    val i = (index + 1) / vidDistance
                    if (i < vidSize && (index + 1) % vidDistance == 0) {
                        listResult.add(listVideos[i])
                        vidPosition = i
                    }
                }
                val startPos = vidPosition + 1
                if (startPos < vidSize) {
                    if (!loadMoreVideo && !loadMorePhoto) {
                        for (i in startPos until vidSize) {
                            listResult.add(listVideos[i])
                        }
                    } else {
                        listVideoWallpaper = listVideos.subList(startPos, vidSize)
                    }
                }
                listResult
            }
        }
    }

    private fun removeImagesFromCategory(data: MutableList<WallpaperModel>) {
        var index = 0
        while (listWallFromCategories.size > 0 && index < data.size) {
            val item = data[index]
            val it = listWallFromCategories.removeFirst { it == item }
            if (it != null) {
                data.removeAt(index)
            }
            index++
        }
    }

    fun loadMoreMainFragment() {
        compositeDisposable.add(Single.just(false).flatMap {
            getBestHashTags(it)
        }.flatMap {
            Single.zip(
                downloadVideoModels(it.second),
                downloadImageModels(it.first),
                downloadParallaxModels(),
            ) { t1, t2, t3 ->
                Pair(it.third, mixWallpaper(t1, t2, t3))
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe { result ->
                detailAdapter.addData(result.second)
            })
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
        var maxImageOfPage = if (isAll) size else maxNumOfVideo * distanceVideos
        maxImageOfPage = min(maxImageOfPage, size)
        val lastList = mutableListOf<WallpaperModel>()
        var isVideoSpot = true
        images.forEachIndexed { index, item ->
            if (index < maxImageOfPage) {
                result.add(item)
                size1 = videos.size
                size2 = parallaxModels.size
                if ((index + 1) % distanceVideos == 0) {
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
        return result
    }

    private fun getBestHashTags(isRefresh: Boolean): Single<Triple<String, String, Boolean>> {
        return Single.just(isRefresh).map {
            val latestHashTagsVideo = storage.getString(AppConstants.PreferencesKey.BEST_HASH_TAGS_VIDEO)
                ?: RemoteConfig.hashTagDefault
            val lastHashTagsImage = storage.getString(AppConstants.PreferencesKey.BEST_HASH_TAGS_IMAGE)
                ?: RemoteConfig.hashTagDefault
            if (it) {
                val hashTagsVideo = database.hashTagsDao().getBestHashTags(
                    isImage = false,
                    limit = RemoteConfig.commonData.bestHashTagNum
                )
                if (latestHashTagsVideo != hashTagsVideo) {
                    storage.pageNumberVideo = 2
                    storage.putString(AppConstants.PreferencesKey.BEST_HASH_TAGS_VIDEO, hashTagsVideo)
                }

                val bestHashTagsWall = database.hashTagsDao()
                    .getBestHashTags(isImage = true, limit = RemoteConfig.commonData.bestHashTagNum)
                if (bestHashTagsWall != lastHashTagsImage) {
                    storage.pageNumberWall = 2
                    storage.putString(AppConstants.PreferencesKey.BEST_HASH_TAGS_IMAGE, bestHashTagsWall)
                }
                Triple(bestHashTagsWall, hashTagsVideo, it)
            } else {
                Triple(lastHashTagsImage, latestHashTagsVideo, it)
            }
        }
    }

    private fun downloadVideoModels(bestHashTagsVideo: String): Single<MutableList<WallpaperModel>> {
        return if (WallpaperHelper.isSupportLiveWallpaper) {
            Single.just(Pair(bestHashTagsVideo, listVideoWallpaper.toMutableList()))
                .flatMap { res ->
                    apiClient.getListWallpapersFromCategory(
                        WallpaperURLBuilder.shared.getVideoUrlDefault(
                            pageNumber = storage.pageNumberVideo.toString(),
                            gender = storage.sex,
                            hashTags = res.first
                        )
                    )
                        .map {
                            storage.pageNumberVideo++
                            val result = mutableListOf<WallpaperModel>()
                            result.addAll(res.second)
                            result.addAll(it.data.toMutableList())
                            result
                        }
                }
        } else Single.just(mutableListOf())
    }

    private fun downloadImageModels(bestHashTags: String): Single<List<WallpaperModel>> {
        return Single.just(bestHashTags).map {
            Pair(it, storage.pageNumberWall.toString())
        }.flatMap { data ->
            val url = WallpaperURLBuilder.shared.getImageUrlDefault(
                data.second,
                storage.sex,
                hashTags = data.first
            )
            val taskGetListWall = apiClient.getListWallpapers(url)
            taskGetListWall.map { res ->
                val lang = RemoteConfig.countryName.toLowerCase(Locale.ENGLISH)
                val langDefault = RemoteConfig.DEFAULT_LANGUAGE.toLowerCase(Locale.ENGLISH)
                val list =
                    res.ServerInfo.filter { it.country == lang || it.country == langDefault }
                val pageNumber = list.firstOrNull()?.pageId
                storage.pageNumberWall = pageNumber?.toIntOrNull() ?: storage.pageNumberWall + 1
                val listWallpapers = list.flatMap { it.wallpapers }.toMutableList()
                val result = mutableListOf<WallpaperModel>()
                result.addAll(listWallpapers)
                result
            }
        }
    }

    private fun downloadParallaxModels(): Single<MutableList<WallpaperModel>> {
        return Single.just(listParallaxWallpaper.toMutableList())
            .flatMap { res ->
                apiClient.getListWallpapersFromCategory(
                    WallpaperURLBuilder.shared.getParallaxUrlDefault(storage.pageNumberParallaxTabHome.toString(), storage.sex)
                ).map {
                    storage.pageNumberParallaxTabHome = it.pageId!!.toInt()
                    val listResult = mutableListOf<WallpaperModel>()
                    listResult.addAll(res)
                    listResult.addAll(it.data.toMutableList())
                    listResult
                }
            }
    }

    private fun imageResources(idCategory: String, pagerNumber: Int) = if (loadMorePhoto) apiClient
        .getListWallpapersFromCategory(WallpaperURLBuilder
            .shared
            .getImageCategoryUrl(idCategory, pagerNumber, storage.sex), expiredTimeInMillis)
    else Single.just(ResponseModel())

    private fun videoResources(idCategory: String, pagerNumber: Int) = if (loadMoreVideo)
        apiClient.getListWallpapersFromCategory(WallpaperURLBuilder
            .shared
            .getImageCategoryVideoUrl(idCategory, pagerNumber, storage.sex), expiredTimeInMillis)
    else Single.just(ResponseModel())

    fun loadMoreCollectionOnFeaturedFragment(hashTag: HashTag?) {
        if (!loadMoreData || hashTag?.hashtag.isNullOrEmptyOrBlank()) return
        compositeDisposable.clear()
        pageNumber = 2
        val url = WallpaperURLBuilder.shared.getSearchUrl(hashTag?.hashtag!!, storage.sex, pageNumber)
        compositeDisposable.add(Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap { apiClient.getListWallpapersFromCategory(url, expired = expiredTimeInMillis) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                loadMoreData = res.hasNext
                if (loadMoreData) {
                    pageNumber++
                }
               detailAdapter.addData(res.data)
            }, {

            }))
    }

    fun statisticHashTags(model: WallpaperModel) {
        compositeDisposable.add(Single.just(model).map {
            database.hashTagsDao().update(it.hashTag, !it.isVideo)
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
        )
    }

    fun saveWallpaperVideo(model: WallpaperModel) {
        PeriodicWallpaperChangeWorker.cancel()
        compositeDisposable.add(Single.just(model).map {
            FileUtils.delete(storage.getString(AppConstants.PreferencesKey.KEY_SAVE_VIDEO_PATH))
            val file = if (it.isFromLocalStorage) {
                FileUtils.copyFileFromUri(it.url ?: "")
            } else FileUtils.copyWallpaperVideo(model.pathCacheFullVideo)
            storage.isSoundingVideo = SetSoundWallpaperDialog.isSoundVideo
            if (file?.exists() == true) {
                TrackingSupport.recordEvent(EventSetWall.SetWallpaperVideoSuccess)
            } else {
                TrackingSupport.recordEvent(EventSetWall.SetWallpaperVideoFail)
            }
            storage.putString(AppConstants.PreferencesKey.KEY_SAVE_VIDEO_PATH, file?.path)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
            }, {

            }))
    }

    private fun logDownloadWallpaper(model: WallpaperModel) {
        val modelId = model.realId
        if (modelId.isEmpty() || model.isFromLocalStorage) return
        val isVideo = model.isVideo
        if (downloadedIds.addIfAbsent(modelId.plus(isVideo))) {
            compositeDisposable.add(Single.just(WallpaperURLBuilder.shared.logDownloadWallpaperUrl(isVideo, modelId, storage.sex))
                .flatMap { apiClient.getString(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Logger.d("logDownloadWallpaper", it)
                }, {}))
        }
    }

    private fun logFavoriteWallpaper(model: WallpaperModel) {
        if (!model.isFavorite) return
        val modelId = model.realId
        if (modelId.isEmpty() || model.isFromLocalStorage) return
        val isVideo = model.isVideo
        if (favoriteIds.addIfAbsent(modelId.plus(isVideo))) {
            compositeDisposable.add(Single.just(WallpaperURLBuilder.shared.logDownloadWallpaperUrl(isVideo, modelId, storage.sex, true))
                .flatMap { apiClient.getString(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Logger.d("logFavoriteWallpaper", it)
                }, {}))
        }
    }

    fun updateRewardAd(position: Int) {
        compositeDisposable.add(Single.just(position).flatMapCompletable {
            val wallModel = detailAdapter.getDataOrNull(it)
            if (wallModel != null) {
                wallModel.hasShownRewardAd = true
                val db = database.wallpaper()
                if (db.selectFirstItem(wallModel.id) != null) {
                    db.updateRewardAd(wallModel.id, true)
                } else db.insert(wallModel)
            } else Completable.complete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.d("rewarded onUserEarnedReward")
            }, {}))
    }

    private fun downloadThumbVideo(position: Int, model: WallpaperModel): Disposable? {
        val url = model.toUrl(isMin = false, isThumb = false, isVideo = true)
        val file = FileUtils.createTempFile(FileUtils.getFileNameFromURL(url))
        val check = model.isFromLocalStorage
        if (file.exists() || check) {
            model.pathCacheFullVideo = if (check) url else file.path
            detailAdapter.playVideoCurrent()
            return null
        }

        if (!hasLoadFirstVideo && detailAdapter.currentPos != position) {
            if (lastPos == detailAdapter.currentPos) {
                // add to queue
                downloadingTask.add(position)
                return null
            } else {
                hasLoadFirstVideo = true
                downloadAllQueue()
            }
        }
        lastPos = detailAdapter.currentPos
        return downloadVideoTask(position)
    }

    private fun downloadParallax(model: WallpaperModel): Disposable? {
        var startTimeInMills = 0L
        return if (++count <= 1) {
            Single.just(getFileFromAsset()).map {
                startTimeInMills = System.currentTimeMillis()
                val file = FileUtils.unzip(it, "parallax", false)
                file
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    Logger.d("total time", System.currentTimeMillis() - startTimeInMills, res)
                    if (!res.isNullOrEmptyOrBlank()) {
                        model.pathCacheParallaxPath = res
                        detailAdapter.playCurrentParallax(model)
                    }
//                        val `is` = res.parentFile?.path
//                        val fis = FileInputStream(res)
//                        val file = FileUtils.unzip(fis, folderName)
                }, {

                })
        } else {
            null
        }
    }

    private fun downloadParallaxContent(model: WallpaperModel): Disposable? {
        return Single.just(getFileFromAsset()).map {
            val file = FileUtils.unzip(it, "parallax", false)
            file
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                if (!res.isNullOrEmptyOrBlank()) {
                    model.pathCacheParallaxPath = res
                    detailAdapter.playCurrentParallax(model)
                }
//                        val `is` = res.parentFile?.path
//                        val fis = FileInputStream(res)
//                        val file = FileUtils.unzip(fis, folderName)
            }, {

            })
    }

    private fun getFileFromAsset(): InputStream {
        return WallpaperApp.instance.assets.open("monkey.zip")
    }

    @Synchronized
    private fun downloadAllQueue() {
        downloadingTask.forEach { pos->
            detailAdapter.getViewHolderCard(pos)?.let {
                it.clear()
                it.addDisposable(downloadVideoTask(pos))
            }
        }
        downloadingTask.clear()
    }

    private fun downloadVideoTask(position: Int): Disposable {
        return Single.just(position).flatMap { pos ->
            val wallModel = detailAdapter.getData(pos)
            val urls = wallModel.toUrl(isMin = false, isThumb = false, isVideo = true)
            val retryUrl = storage.bestStoreVideoDownload?.let { best
                ->
                urls.replaceFirst(RemoteConfig.commonData.videoStorage, best)
            } ?: urls
            apiClient.downloadFile(urls)
                .onErrorResumeNext(apiClient.downloadFile(retryUrl))
                .map { res ->
                    val file = FileUtils.write(res, fileName = FileUtils.getFileNameFromURL(urls))
                    Pair(pos, file)
                }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                hasLoadFirstVideo = true
                downloadAllQueue()
            }
            .subscribe({ res ->
                if (res.second.exists()) {
                    detailAdapter.getData(res.first).pathCacheFullVideo = res.second.path
                }
                Logger.d("downloadVideoTask success")
                detailAdapter.playVideoCurrent()
            }, {

            })
    }

    companion object {
        private val downloadedIds = CopyOnWriteArrayList<String>()
        private val favoriteIds = CopyOnWriteArrayList<String>()
        private fun clear() {
            downloadedIds.clear()
            favoriteIds.clear()
        }
    }
}