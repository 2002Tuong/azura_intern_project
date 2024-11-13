package com.parallax.hdvideo.wallpapers.ui.base.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.ui.base.dialog.ProgressDialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

open class BaseViewModel: ViewModel() {

    open val compositeDisposable = CompositeDisposable()
    private val _progressDialogLiveData = MutableLiveData<ProgressDialogFragment.Configure>()
    private val _toastLiveData = MutableLiveData<Any>()
    val progressDialog get() = _progressDialogLiveData
    val toastLiveData get() = _toastLiveData
    var loading = false
    val expiredTimeInMillis = 1000 * 3600 * 24L // 1 day
    val loadMoreLiveData = PublishSubject.create<List<WallpaperModel>>()

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    open fun showLoading(touchOutside: Boolean = false, canGoBack: Boolean = false) {
        loading = true
        _progressDialogLiveData.postValue(
            ProgressDialogFragment.Configure(
            status = true,
            touchOutside = touchOutside,
            canGoBack = canGoBack
        ))
    }

    open fun dismissLoading() {
        loading = false
        _progressDialogLiveData.postValue(
            ProgressDialogFragment.Configure(
            status = false,
            touchOutside = false,
            canGoBack = false
        ))
    }

    fun showToast(any: Any) {
        _toastLiveData.postValue(any)
    }

    open fun loadMoreData() {

    }

    fun add(disposable: Disposable?) {
        disposable?.let { compositeDisposable.add(it) }
    }
}