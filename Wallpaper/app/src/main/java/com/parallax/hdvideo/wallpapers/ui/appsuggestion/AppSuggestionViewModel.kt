package com.parallax.hdvideo.wallpapers.ui.appsuggestion

import com.bumptech.glide.RequestManager
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.MoreAppModel
import com.parallax.hdvideo.wallpapers.databinding.ItemSuggestionAppBinding
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AppSuggestionViewModel @Inject constructor(private val apiClient: ApiClient,
                                                          private val localStorage: LocalStorage)
    : BaseViewModel() {

    private val maximumItem = 4
    var requestManagerInstance: RequestManager? = null
    var moreAppModels = listOf<MoreAppModel>()


    val moreAppAdapter = object : BaseAdapterList<MoreAppModel, ItemSuggestionAppBinding>({ R.layout.item_suggestion_app },
        onBind = { binding, model, _ -> binding.item = model }) {
        override var enabled: Boolean = false
        override fun onBindViewHolder(holder: BaseViewHolder<MoreAppModel, ItemSuggestionAppBinding>, position: Int) {
            super.onBindViewHolder(holder, position)
            requestManagerInstance?.let {
                it.load(getData(position).toUrl()).into(holder.dataBinding.appThumbnail)
            }
            holder.dataBinding.downloadButton.setOnClickListener {
                onClickedItemcallBack?.invoke(position, getData(position))
            }
        }
    }

    fun getMoreApp() {
        if (!moreAppAdapter.dataEmpty) return
        compositeDisposable.clear()
        val url =
            if (BuildConfig.DEBUG)
                "https://sg.dieuphoiwallpaper.xyz/wallforgirlgz/rest/apps?lang=en_VN&os=android&mobileid=ee3970e0ada2c447_sdk29900596658&token=female&appid=videogirlwallpapertkv2secv10"
            else
                WallpaperURLBuilder.shared.getMoreAppUrl(localStorage.sex)
        compositeDisposable.add(
            Single.timer(500, TimeUnit.MILLISECONDS)
            .flatMap { apiClient.getMoreApp(url) }
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { dismissLoading() }
            .subscribeOn(Schedulers.io())
            .doFinally { dismissLoading() }
            .subscribe({

                val listData = it.data
                listData.shuffle()
                moreAppModels = listData
                val size = listData.size
                if (size <= maximumItem) moreAppAdapter.setData(it.data)
                else {
                    moreAppAdapter.setData(listData.subList(0,4))
                }
                Logger.d( "getMoreApp")
            }, {
                Logger.d( "getMoreApp error")
            })
        )
    }

}