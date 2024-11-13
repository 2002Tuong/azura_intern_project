package com.parallax.hdvideo.wallpapers.ui.list

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.animateFallDown
import com.parallax.hdvideo.wallpapers.extension.fromJson
import com.parallax.hdvideo.wallpapers.extension.getStringFromAssets
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragmentAdapter
import com.parallax.hdvideo.wallpapers.ui.search.SearchWallpaperAdapter
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import com.livewall.girl.wallpapers.extension.checkPermissionStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ListWallpaperViewModel @Inject constructor(
    private val api: ApiClient,
    private val storage: LocalStorage,
    private val database: AppDatabase
) : BaseViewModel() {

    val isEmptyListLiveData = MutableLiveData<Boolean>()
    var searchAdapter = SearchWallpaperAdapter(screenType = AppScreen.CATEGORY,
        scrollListener = object :
            MainFragmentAdapter.OnScrollListener {
            override fun loadMoreData() {
                if (isCouple) {
                    searchCoupleWallpaper(false)
                }
                if(isImage4D){
                    loadImage4D(false)
                }
                if (isSearchByHashTag) {
                    getListWallpaperByHashTag(vmHashTag, false)
                } else if (idCate.isNotEmpty()) getData(false)
            }
        })

    var idCate = ""
    private lateinit var vmHashTag: HashTag
    lateinit var category: Category
    private var pageNumber = 1
    private var pageNumberVideo = 0
    private var pagerNumber4D = 1
    private var isStart = false
    private var isCouple = false
    private var isImage4D = false
    private var isSearchByHashTag = false
    var couldRefreshData = true
    override fun onCleared() {
        super.onCleared()
        searchAdapter.release()
    }

    fun getData(isRefresh: Boolean) {
        compositeDisposable.clear()
        if (isRefresh) pageNumber = 1
        if (idCate == Category.VIDEO_CATEGORY_ID) {
            getVideo(isRefresh)
        } else {
            getWallpaper(isRefresh)
        }
    }

    private val responseImageResources get() = if (searchAdapter.couldLoadMorePhoto) api
        .getListWallpapersFromCategory(WallpaperURLBuilder
            .shared
            .getImageCategoryUrl(idCate, pageNumber, storage.sex), expiredTimeInMillis)
    else Single.just(ResponseModel())

    private val responseVideoResources get() = if (searchAdapter.couldLoadMoreVideo && WallpaperHelper.isSupportLiveWallpaper)
        api.getListWallpapersFromCategory(WallpaperURLBuilder
            .shared
            .getImageCategoryVideoUrl(idCate, pageNumberVideo, storage.sex), expiredTimeInMillis)
    else Single.just(ResponseModel())

    private fun getWallpaper(isRefresh: Boolean) {
        compositeDisposable.add(Single.just(isRefresh)
            .flatMap {
                Single.zip(responseImageResources, responseVideoResources
                ) { image, video ->
                    if (image.data.isNotEmpty()) pageNumber++
                    if (video.data.isNotEmpty()) pageNumberVideo++
                    searchAdapter.couldLoadMorePhoto = image.hasNext
                    searchAdapter.couldLoadMoreVideo = video.hasNext
                    searchAdapter.mixSearchResults(images = image.data, video.data)
                }
            }.map {
                updateStatusFavoriteWallpaper(it)
                it
            }
            .delay(200, TimeUnit.MILLISECONDS)
            .doOnSubscribe { if (!isStart) showLoading(canGoBack = true) }
            .doFinally {
                dismissLoading()
                loading = false
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                if (isRefresh) {
                    searchAdapter.setData(res)
                    searchAdapter.getRecyclerView()?.animateFallDown()
                } else searchAdapter.addFilterData(res)
                loadMoreLiveData.onNext(res)
                isStart = true
            }, {
                searchAdapter.onNextItemsLoaded()
                isStart = true
            })
        )
    }

    private fun getVideo(isRefresh: Boolean) {
        compositeDisposable.add(
            Single.timer(200, TimeUnit.MILLISECONDS).flatMap {
                api.getListWallpapersFromCategory(
                    WallpaperURLBuilder.shared.getVideoUrlDefault(
                        pageNumber = pageNumber.toString(),
                        type = "cate",
                        gender = storage.sex
                    ), expiredTimeInMillis
                )
            }.delay(200, TimeUnit.MILLISECONDS)
                .doFinally {
                    dismissLoading()
                }
                .map {
                    val wallpapers = it.data
                    updateStatusFavoriteWallpaper(wallpapers)
                    wallpapers
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    val isEmpty = res.isEmpty()
                    if (!isEmpty) pageNumber++
                    if (searchAdapter.emptyData && isEmpty) {
                        res.addAll(defaultVideos)
                    }
                    if (isRefresh) {
                        searchAdapter.setData(res)
                    } else searchAdapter.addData(res)
                    loadMoreLiveData.onNext(res)
                    isStart = true
                }, {
                    if (searchAdapter.emptyData) {
                        searchAdapter.addData(defaultVideos)
                    }
                    searchAdapter.onNextItemsLoaded()
                })
        )
    }

    fun getHistoryDownload(isRefresh: Boolean) {
        searchAdapter.canLoadMoreData(false)
        compositeDisposable.clear()
        compositeDisposable.add(Flowable.timer(400, TimeUnit.MILLISECONDS)
            .flatMap { database.wallpaper().selectHistoryDownload() }
            .doOnSubscribe { if (!isStart) showLoading(canGoBack = true) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                updateStatusFavoriteWallpaper(it)
                it
            }
            .subscribe({ res ->
                searchAdapter.setData(res)
                updateEmptyViews(res)
                dismissLoading()
                isStart = true
            }, { dismissLoading() }))
    }

    private fun updateEmptyViews(res: List<WallpaperModel>?) {
        isEmptyListLiveData.value = res.isNullOrEmpty()
    }

    fun getFavoriteData(isRefresh: Boolean) {
        compositeDisposable.clear()
        searchAdapter.canLoadMoreData(false)
        compositeDisposable.add(Flowable.timer(400, TimeUnit.MILLISECONDS)
            .flatMap { database.wallpaper().selectFavorite() }
            .map {ls ->
                if (WallpaperApp.instance.checkPermissionStorage) ls
                else ls.filter { !it.isFromLocalStorage }
            }
            .doOnSubscribe { if (!isStart) showLoading(canGoBack = true) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                if(couldRefreshData){
                    searchAdapter.setData(res)
                    updateEmptyViews(res)
                    dismissLoading()
                    isStart = true
                    couldRefreshData= false
                }
            }, { dismissLoading() }))
    }

    private val defaultVideos: List<WallpaperModel> get()  {
        return try {
            val json = getStringFromAssets(AppConstants.FILE_NAME_VIDEO_WALLPAPER) ?: ""
            fromJson(json)
        }catch (e: Exception) { mutableListOf() }
    }

    fun loadLocalStorage() {
        compositeDisposable.clear()
        searchAdapter.canLoadMoreData(false)
        compositeDisposable.add(
            FileUtils.localStorageQuery().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (!isStart) showLoading(canGoBack = true) }
                .map {
                    updateStatusFavoriteWallpaper(it)
                    it
                }
                .subscribe({
                    searchAdapter.setData(it)
                    searchAdapter.getRecyclerView()?.animateFallDown()
                    dismissLoading()
                    Logger.d("LocalStorage", "Success! Item count: ${it.size}")
                }, {
                    dismissLoading()
                    Logger.d("LocalStorage", "Failed! Error: ${it.message}")
                })
        )
    }

    fun searchCoupleWallpaper(isRefresh: Boolean)  {
        isCouple = true
        if (isRefresh) {
            pageNumber = 1
        }
        compositeDisposable.clear()
        val url = WallpaperURLBuilder.shared.getSearchUrl(RemoteConfig.commonData.hashTagCouple, storage.sex, pageNumber)
        compositeDisposable.add(Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap { api.getListWallpapersFromCategory(url, expired = expiredTimeInMillis) }
            .doOnSubscribe { if (!isStart) showLoading(canGoBack = true) }
            .doFinally {
                dismissLoading()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                val coupleWall = mutableListOf<WallpaperModel>()
                res.data.forEach {
                    coupleWall.add(it)
                    coupleWall.add(it.invert())
                }
                if (coupleWall.isNotEmpty()) pageNumber++
                if (isRefresh) {
                    searchAdapter.setData(coupleWall)
                } else searchAdapter.addData(coupleWall)
                loadMoreLiveData.onNext(coupleWall)
                isStart = true
            }, {
                searchAdapter.onNextItemsLoaded()
                isStart = true
            }))
    }

    fun getTrendingWallpaper(isLivePhoto: Boolean = false) {
        compositeDisposable.add(Single.zip(imagesTrending, videosTrending) { walls, videos ->
            val listResult = mutableListOf<WallpaperModel>()
            val listVideos = videos.toMutableList()
            var size: Int
            walls.forEachIndexed { index, item ->
                listResult.add(item)
                size = listVideos.size
                if ((index + 1) % AppConstants.DISTANCE_VIDEOS_IN_HOME_SCREEN == 0 && size > 0) {
                    listResult.add(listVideos.removeAt(0))
                }
            }
            listResult
        }.map {
            updateStatusFavoriteWallpaper(it)
            it
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
               // showLoading()
            }.doFinally { dismissLoading() }
            .subscribe({
                if (isLivePhoto) {
                    searchAdapter.setData(it)
                    searchAdapter.setFilter(SearchWallpaperAdapter.FiltersType.VIDEO)
                } else {
                    searchAdapter.setData(it)
                }
            }, {})
        )
    }

    fun loadImage4D(isRefresh: Boolean) {
        isImage4D = true
        //val url = WallpaperURLBuilder.shared.getImage4D(storage.gender, pagerNumber4D)
        val url = WallpaperURLBuilder.shared.getParallaxUrlDefault(storage.pageNumberParallaxTab4D.toString(), storage.sex)
        compositeDisposable.add(api.getListWallpapersFromCategory(url)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                storage.pageNumberParallaxTab4D = if(it.pageId.isNullOrEmpty()) 2 else it.pageId!!.toInt()

                if (isRefresh) {
                    searchAdapter.setData(it.data)
                } else {
                    searchAdapter.addData(it.data)
                }
                loadMoreLiveData.onNext(it.data)
            }, {})
        )
    }

    private val imagesTrending = Single.fromCallable {
        WallpaperURLBuilder.shared.getTrendingWallUrl(storage.sex)
    }.flatMap { api.getListWallpapersFromCategory( it,3600 * 1000L) }.map { it.data }


    private val videosTrending = if (RemoteConfig.commonData.isActiveServer && WallpaperHelper.isSupportLiveWallpaper ) Single.fromCallable {
        WallpaperURLBuilder.shared.getTrendingVideoUrl(storage.sex)
    }.flatMap { api.getListWallpapersFromCategory( it,3600 * 1000L) }.map { it.data }
     else Single.just(listOf<WallpaperModel>())


    fun getTopDownLoadData() {
        searchAdapter.canLoadMoreData(false)
        compositeDisposable.clear()
        val word = "topdownload"
        compositeDisposable.add(Single.zip(
            getWallpaperTopDownLoad(word),
            getVideoTopDownload(word),
        ) { wallpapers, videos ->
            searchAdapter.mixSearchResults(wallpapers, videos)
        }.map {
            updateStatusFavoriteWallpaper(it)
            it
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ res ->
                searchAdapter.setData(res)
            }, {

            })
        )
    }

    @SuppressLint("CheckResult")
    private fun getWallpaperTopDownLoad(text: String): Single<List<WallpaperModel>> {
        return Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap {
                val url = WallpaperURLBuilder.shared.getTopDownUrl(storage.sex)
                api.getListWallpapersFromCategory(url, expired = 3600 * 1000)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                it.data
            }
    }

    private fun getVideoTopDownload(query: String): Single<List<WallpaperModel>> {
        return if (!WallpaperHelper.isSupportLiveWallpaper) Single.just(listOf())
        else Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap {
                val url = WallpaperURLBuilder.shared.getVideoSearchUrl(
                    query,
                    storage.sex
                )
                api.getListWallpapersFromCategory(url)
            }.map {
                it.data
            }
    }

    fun getBestHashTagData() {
        searchAdapter.canLoadMoreData(false)
        compositeDisposable.clear()
        compositeDisposable.add(
            Single.fromCallable {
                database.hashTagsDao()
                    .getBestHashTags(isImage = true, limit = RemoteConfig.commonData.bestHashTagNum)
            }.flatMap {
                //in case user haven't clicked on any item -> no hash tag collected -> use top down api (check DWT-537)
                val url = if (it == RemoteConfig.hashTagDefault) {
                    WallpaperURLBuilder.shared.getTopDownUrl(storage.sex)
                } else {
                    WallpaperURLBuilder.shared.getHashTagsUrl(it, storage.sex,
                        pageNumber = 1, isHashTag = true, isNotify = true)
                }
                api.getListWallpapersFromCategory(url, 3600 * 1000L)
            }.doOnSubscribe { showLoading(canGoBack = true) }
            .doFinally {
                dismissLoading()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                searchAdapter.setData(it.data)
            }, {

            }))
    }

    override fun loadMoreData() {
        searchAdapter.loadNextItems()
    }

    fun getListWallpaperByHashTag(hashTag: HashTag, isRefresh: Boolean) {
        compositeDisposable.clear()
        isSearchByHashTag = true
        vmHashTag = hashTag
        if (isRefresh) {
            pageNumber = 1
        }
        val url = WallpaperURLBuilder.shared.getSearchUrl(vmHashTag.hashtag!!, storage.sex, pageNumber)
        compositeDisposable.add(Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap { api.getListWallpapersFromCategory(url, expired = expiredTimeInMillis) }
            .doOnSubscribe { if (!isStart && pageNumber == 1) showLoading(canGoBack = true) }
            .doFinally {
                dismissLoading()
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                pageNumber++
                if (isRefresh) {
                    searchAdapter.setData(filterList(res.data, true))
                } else {
                    searchAdapter.addData(filterList(res.data, false))
                }
                if (!res.hasNext) searchAdapter.canLoadMoreData(false)
            }, {

            }))
    }

    //Add images from hash tag, then remove item in downloaded list which has the same url with these images.
    private fun filterList(data: List<WallpaperModel>, isRefresh: Boolean): List<WallpaperModel> {
        return if (vmHashTag.walls?.isEmpty() != true) {
            var list = mutableListOf<WallpaperModel>()
            if (isRefresh) list.addAll(0, vmHashTag.walls!!)
            list.addAll(data)
            list = list.distinctBy { it.url }.toMutableList()
            list
        } else {
            data
        }
    }

    fun updateWallpaper(model: WallpaperModel) {
        compositeDisposable.add(Single.just(model).flatMapCompletable {
            database.wallpaper().updateDownload(it.id, false)
                .andThen(
                    database.wallpaper().updateRewardAd(it.id, false)
                )
        }.doOnSubscribe {
            if (!isStart) showLoading(canGoBack = true)
        }.doFinally {
            dismissLoading()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                searchAdapter.remove(searchAdapter.listData.indexOf(model))
            }, {

            })
        )
    }

    private fun updateStatusFavoriteWallpaper(listWall : List<WallpaperModel>) {
        compositeDisposable.add(database.wallpaper().selectFavorite(true).subscribe {
            listWall.forEach { wallpaperModel ->
                val hasfound = it.any { it.id == wallpaperModel.id }
                if(hasfound) wallpaperModel.isFavorite = true
            }
        })
    }
}