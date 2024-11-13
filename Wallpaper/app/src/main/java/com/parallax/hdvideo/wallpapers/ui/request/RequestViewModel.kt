package com.parallax.hdvideo.wallpapers.ui.request

import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.di.network.ApiClient
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isOnline
import com.parallax.hdvideo.wallpapers.extension.urlEncoder
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
        private val apiClient: ApiClient,
        private val storage: LocalStorage
) : BaseViewModel() {

    fun submit(email: String, subject: String, detailed: String) {
        compositeDisposable.add(
            Single.just(
                WallpaperURLBuilder.shared
                    .getWallpaperRequestUrl(email.urlEncoder(), subject.urlEncoder(), detailed.urlEncoder()))
                .delay(1L, TimeUnit.SECONDS)
                .flatMap { apiClient.getString(it) }
                .doOnSubscribe { showLoading(canGoBack = true) }
                .doFinally { dismissLoading() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emailRequest = email
                    showToast(R.string.requested_thank_you)
                }, {
                    if (it.isOnline()) {
                        emailRequest = email
                        showToast(R.string.requested_thank_you)
                    }
                })
        )
    }

    var emailRequest: String
        get() = storage.getString("email_request") ?: ""
        set(value) = storage.putString("email_request", value)
}
