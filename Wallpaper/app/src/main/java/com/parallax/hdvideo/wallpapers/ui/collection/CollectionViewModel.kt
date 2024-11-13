package com.parallax.hdvideo.wallpapers.ui.collection

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.Category.Companion.CATEGORY_ID_ADVERTISE
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.DefaultManager
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val api: ApiClient,
    private val storage: LocalStorage
) : BaseViewModel() {

    val collectionAdapter: CollectionV2Adapter = CollectionV2Adapter()

    init {
        collectionAdapter.canLoadMoreData(false)
    }

    fun setup(fragment: Fragment) {
        collectionAdapter.requestManagerInstance = Glide.with(fragment)
    }

    fun getData() {
        if (collectionAdapter.listData.isEmpty()) {
            compositeDisposable.add(
                Single.timer(100, TimeUnit.MILLISECONDS)
                    .flatMap {
                        api.getListCategoryModels(WallpaperURLBuilder.shared.getCategoryUrl(storage.sex))
                            .onErrorReturn { ResponseModel(DefaultManager.loadCategories()) }
                            .map { if (it.data.isEmpty()) DefaultManager.loadCategories() else it.data }
                    }.map {
                        addAdvertiseCategory(it)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { dismissLoading() }
                    .subscribe({ res ->
                        collectionAdapter.setData(res)
                    }, {

                    })
            )
        }
    }

    private fun addAdvertiseCategory(list: List<Category>): List<Category> {
        val results = ArrayList<Category>()
        var numberOfCategory = 0
        list.forEach {
            // if numberCategory == 2 add category , advertise else add category
            if (numberOfCategory == 2) {
                results.add(it)
                results.add(Category(CATEGORY_ID_ADVERTISE, CATEGORY_ID_ADVERTISE))
                numberOfCategory = -1 // numberCategory -1 because add advertise not category
            } else {
                results.add(it)
            }
            numberOfCategory++
        }
        return results
    }

    private fun addVideoCategoryIfNeeded(list: MutableList<Category>): List<Category> {
        if (WallpaperHelper.isSupportLiveWallpaper && RemoteConfig.commonData.isActiveServer) {
            val videoCategory = Category(
                id = Category.VIDEO_CATEGORY_ID,
                name = WallpaperApp.instance.getString(R.string.live_wallpapers)
            )
            val response = api.getWallpapersFromCategory(
                WallpaperURLBuilder.shared.getVideoUrlDefault(
                    pageNumber = "1",
                    type = "cate",
                    gender = storage.sex
                ), expiredTimeInMillis
            ).execute()
            response.body()?.data?.let {
                videoCategory.walls = it
                if (it.isNotEmpty()) list.add(0, videoCategory)
            }
        }
        return list
    }
}