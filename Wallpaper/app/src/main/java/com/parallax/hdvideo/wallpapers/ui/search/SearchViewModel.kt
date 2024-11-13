package com.parallax.hdvideo.wallpapers.ui.search

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.animateFallDown
import com.parallax.hdvideo.wallpapers.extension.removeFirst
import com.parallax.hdvideo.wallpapers.remote.DefaultManager
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.EventSearch
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragmentAdapter
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class SearchViewModel @Inject constructor(private val apiClient: ApiClient,
                                                   private val storage: LocalStorage,
                                                   private val database: AppDatabase)
    : BaseViewModel() {

    private var pageNumber = 1
    private var started = false
    private var keyWord: String = ""
    private var disposableSuggestion: Disposable? = null
    private var disposableSearch: Disposable? = null
    private val observerOnTextChanged = Observer<String> { onTextChanged(it) }
    private var isTopDown: Boolean = false
    private var isCoupleWallpaper: Boolean = false
    private var isTopDevices = false
    val textChanged = MutableLiveData<String>()
    var currentHashTag: HashTag? = null
    var isShowNoResultView = View.GONE

    val wallpaperAdapter = SearchWallpaperAdapter(screenType = AppScreen.SEARCH, object : MainFragmentAdapter.OnScrollListener {
        override fun loadMoreData() {
            search()
        }
    })
    val searchSuggestionAdapter = SearchSuggestionAdapter()
    var trendHashTagAdapter = TopTrendHashTagAdapter(WallpaperApp.instance.resources.getIntArray(R.array.search_tag_color))

    init {
        textChanged.observeForever(observerOnTextChanged)
        searchSuggestionAdapter.onDeleteItemCallback = { item, pos ->
            item.hashTag?.let { removeFromSearchHistory(it) }
            searchSuggestionAdapter.remove(pos)
        }
    }

    override fun onCleared() {
        super.onCleared()
        textChanged.removeObserver(observerOnTextChanged)
        disposableSuggestion?.dispose()
        disposableSearch?.dispose()
        wallpaperAdapter.release()
    }

    fun getHashTags() {
        compositeDisposable.add(Single.zip(apiClient.getHashTags(WallpaperURLBuilder.shared.getHashTagsTrendUrl(storage.sex)),
            apiClient.getHashTags(WallpaperURLBuilder.shared.getColorUrl(storage.sex))
        ) { t1, t2 ->
            Pair(t1.data, t2.data)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({(trends, colors) ->
                colors.shuffle()
                trendHashTagAdapter.setDataColorAdapter(colors.subList(0, min(colors.size, 10)))
                val model = trends.removeFirst { hashTags ->
                    hashTags.topDown
                }?.apply {
                    name = WallpaperApp.instance.getString(R.string.top_download)
                }
                if (model != null) {
                    trends.add(0, model)
                }
                trendHashTagAdapter.setData(trends)
                trendHashTagAdapter.loadAds()
            }, {
                trendHashTagAdapter.setData(listOf())
            }))
    }

    fun search(query: String? = null, isRefresh: Boolean = false, isTopDown: Boolean = false,
               isCouple: Boolean = false, isTopDevices : Boolean = false): Boolean  {
        val word = query ?: this.keyWord
        if (isRefresh) {
            if (this.keyWord == word) {
                wallpaperAdapter.onNextItemsLoaded()
                return false
            }
            this.isTopDown = isTopDown
            this.isCoupleWallpaper = isCouple
            this.isTopDevices = isTopDevices
            pageNumber = 1
            this.keyWord = word
            started = false
            wallpaperAdapter.removeAll()
            wallpaperAdapter.canLoadMoreData(true)
        }
        disposableSuggestion?.dispose()
        disposableSearch?.dispose()
        disposableSearch = Single.zip(
            getWallpaper(word),
            getVideo(word),
        ) { wallpapers, videos ->
            wallpaperAdapter.mixSearchResults(wallpapers, videos)
        }.map {
            updateStatusFavoriteWallpaper(it)
            it
        }.doOnSubscribe { if(!started) showLoading(true) }
            .doFinally {
                dismissLoading()
            }.doAfterSuccess { list ->
                if (this.isTopDevices) list.forEach { it.isTopDevices = true }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ res ->
                val wallpaperModels: List<WallpaperModel> = if(this.isCoupleWallpaper) {
                    val data = mutableListOf<WallpaperModel>()
                    res.forEach {
                        data.add(it)
                        data.add(it.invert())
                    }
                    data
                } else {
                    res
                }
                if (wallpaperModels.isNotEmpty()) pageNumber++
                else TrackingSupport.recordSearchKeywords(EventSearch.SearchNoResult, word)
                if (isRefresh) {
                    wallpaperAdapter.setData(wallpaperModels)
                    wallpaperAdapter.getRecyclerView()?.animateFallDown()
                    loadMoreLiveData.onNext(wallpaperAdapter.listData)
                } else {
                    val res = wallpaperAdapter.addFilterData(wallpaperModels)
                    loadMoreLiveData.onNext(res)
                }
                started = true
                if (wallpaperAdapter.emptyData && this.isTopDown) {
                    loadTopDownDefaultWallpaper()
                }
            }, {
                wallpaperAdapter.onNextItemsLoaded()
                started = true
                if (wallpaperAdapter.emptyData && this.isTopDown) {
                    loadTopDownDefaultWallpaper()
                }
            })
        if (isRefresh) {
            wallpaperAdapter.loadAds()
        }
        return true
    }

    private fun updateStatusFavoriteWallpaper(listWall : List<WallpaperModel>) {
        compositeDisposable.add(database.wallpaper().selectFavorite(true).subscribe {
            listWall.forEach { wallpaperModel ->
                val found = it.any { it.id == wallpaperModel.id }
                if(found) wallpaperModel.isFavorite = true
            }
        })
    }

    private fun getWallpaper(text: String) : Single<List<WallpaperModel>> {
        return if (!wallpaperAdapter.couldLoadMorePhoto) Single.just(listOf())
        else Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap {
                val url = if (this.isTopDown) WallpaperURLBuilder.shared.getTopDownUrl(storage.sex)
                else WallpaperURLBuilder.shared.getSearchUrl(text, storage.sex, pageNumber)
                apiClient.getListWallpapersFromCategory(url, expired = if (this.isTopDown) 3600 * 1000 else expiredTimeInMillis)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                wallpaperAdapter.couldLoadMorePhoto = it.hasNext
                it.data
            }
    }

    private fun getVideo(query : String) : Single<List<WallpaperModel>> {
        return if (!WallpaperHelper.isSupportLiveWallpaper
            || !wallpaperAdapter.couldLoadMoreVideo) Single.just(listOf())
        else Single.timer(100, TimeUnit.MILLISECONDS)
            .flatMap {
                if (!isCoupleWallpaper) {
                    val url = WallpaperURLBuilder.shared.getVideoSearchUrl(
                        query,
                        storage.sex,
                        pageNumber
                    )
                    apiClient.getListWallpapersFromCategory(url)
                } else Single.just(ResponseModel())
            }.map {
                wallpaperAdapter.couldLoadMoreVideo = it.hasNext
                it.data
            }
    }

    private fun onTextChanged(textSearch: String) {
        disposableSuggestion?.dispose()
        val length = textSearch.length
        if (length >= 1) {
            disposableSuggestion = Single.timer(200, TimeUnit.MILLISECONDS)
                .flatMap { apiClient.getHashTags(WallpaperURLBuilder.shared.getSearchUrl(textSearch, storage.sex, pageNumber, isSuggestion = true))}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
//                    _searchHashTags.postValue(res.data)
                    searchSuggestionAdapter.setData(res.data)
                }, {
                    searchSuggestionAdapter.setData(listOf())
                })
        } else {
            disposableSuggestion = database.searchHistoryDao().getAll()
                .map { list ->
                    list.map {
                        val model = HashTag()
                        model.id = it.id
                        model.name = it.name
                        model.isHistory = true
                        model.hashTag = it.query
                        model
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    searchSuggestionAdapter.setData(res)
                }, {
                    searchSuggestionAdapter.setData(listOf())
                })
        }
    }


    private fun loadTopDownDefaultWallpaper() {
        compositeDisposable.add(Single.fromCallable { DefaultManager.loadTopDownload() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                wallpaperAdapter.setData(it)
            }, {}))
    }

    fun addToSearchHistory(query : String?, name: String?, id: Long = 0) {
        if(query == null) return
        compositeDisposable.add(database.searchHistoryDao().updateOrInsert(query, name, id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
            }, {}))
    }

    private fun removeFromSearchHistory(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.searchHistoryDao().deleteQuery(query)
        }
    }

    override fun loadMoreData() {
        wallpaperAdapter.loadNextItems()
    }
    
    fun reset() {
        keyWord = ""
    }

    fun cancelAll() {
        compositeDisposable.clear()
    }
}