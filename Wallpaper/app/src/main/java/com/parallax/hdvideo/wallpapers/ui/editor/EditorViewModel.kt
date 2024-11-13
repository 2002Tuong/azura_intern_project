package com.parallax.hdvideo.wallpapers.ui.editor

import android.annotation.SuppressLint
import android.net.Uri
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import com.parallax.hdvideo.wallpapers.utils.other.SingleLiveEvent
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(private val storage: LocalStorage): BaseViewModel() {

    init {
        WallpaperURLBuilder.shared.setConfigUrl(storage)
    }

    val hasSetWallpaperLiveData = SingleLiveEvent<Boolean>()

    @SuppressLint("CheckResult")
    fun setWallpaper(uri: Uri?, type: WallpaperHelper.WallpaperType) {
        if (uri == null) return
        compositeDisposable.add(Single.just(uri).map {
            GlideSupport.getBitmap(it.toString(),  RequestOptions().apply {
                override(AppConfiguration.widthScreenValue, AppConfiguration.heightScreenValue)
                skipMemoryCache(true)
                diskCacheStrategy(DiskCacheStrategy.NONE)
            }).let { bitmap ->
                WallpaperHelper.setWallpaper(bitmap, type)
            }
        }.delay(200, TimeUnit.MILLISECONDS)
            .doOnSubscribe { showLoading() }
            .doFinally { dismissLoading() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ check ->
                hasSetWallpaperLiveData.postValue(check)
            }, {
                showToast(R.string.setting_wallpaper_error)
            })
        )
    }
}