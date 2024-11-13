package com.parallax.hdvideo.wallpapers.ui.playlist

import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.services.worker.PeriodicWallpaperChangeWorker
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.dialog.SelectTimePeriodDialog
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragmentAdapter
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.livewall.girl.wallpapers.extension.checkPermissionStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val localStorage: LocalStorage
) : BaseViewModel() {

    val serviceStateLiveData : LiveData<List<WorkInfo>> =
        WorkManager.getInstance(WallpaperApp.instance)
            .getWorkInfosForUniqueWorkLiveData(PeriodicWallpaperChangeWorker.TASK_NAME)

    val mainAdapter = MainFragmentAdapter(screenType = AppScreen.PLAYLIST)

    private val _queueStatusLiveData = MutableLiveData<Boolean>()
    val queueStatus : LiveData<Boolean> = _queueStatusLiveData

    init {
        mainAdapter.shouldHiddenAd = true
        mainAdapter.canLoadMoreData(false)
    }

    override fun onCleared() {
        super.onCleared()
        mainAdapter.release()
    }
    fun setup(fragment: PlaylistFragment) {
        mainAdapter.requestManagerInstance = Glide.with(fragment)
    }

    fun deleteWallpaperFromPlaylist(wallpaperModel: WallpaperModel) {
        compositeDisposable.add(Single.just(wallpaperModel).flatMapCompletable {
            val itemDatabase = appDatabase.wallpaper().selectFirstItem(wallpaperModel.id)
            if (itemDatabase != null) {
                itemDatabase.isPlaylist = false
                FileUtils.getPlayListFile(itemDatabase.toUrl(isMin = false, isThumb = false)).delete()
                appDatabase.wallpaper().update(itemDatabase)
            } else Completable.complete()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                showToast(R.string.remove_wallpaper_successfully)
            }, {}))
    }

    fun retrieveWallpaperQueue() {
        compositeDisposable.clear()
        compositeDisposable.add(appDatabase.wallpaper().retrievePlaylistQueue()
            .map {ls ->
                if (WallpaperApp.instance.checkPermissionStorage) ls
                else ls.filter { !it.isFromLocalStorage }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mainAdapter.setData(it)
                _queueStatusLiveData.value = it.isNotEmpty()
                if (it.isEmpty()) {
                    cancelServicesPlayList()
                }
            }, {})
        )
    }

    fun updateCurrentSettings(textView: TextView, duration: Int? = null) : Int {
        val text = arrayOf(R.string.fifteen_minutes, R.string.thirty_minutes,
            R.string.one_hour, R.string.two_hours,
            R.string.six_hours, R.string.twelve_hours,
            R.string.one_day, R.string.three_days)
        val d = duration?.also {
            localStorage.wallpaperChangeDelayTimeInMin = it } ?: localStorage.wallpaperChangeDelayTimeInMin
        var index = SelectTimePeriodDialog.listDurations.indexOf(d)
        if (index < 0) index = 0
        textView.text = textView.context.getString(text[index])
        return d
    }


    fun cancelServicesPlayList() {
        localStorage.playlistCurIndex = 0
        localStorage.shouldAutoChangeWallpaper = false
        PeriodicWallpaperChangeWorker.cancel()
    }

    fun schedule(duration: Int, typeScreen: Int) {
        localStorage.wallpaperChangeDelayTimeInMin = duration
        localStorage.shouldAutoChangeWallpaper = true
        localStorage.putData(AppConstants.PreferencesKey.AUTO_CHANGE_WALLPAPER_SCREEN_TYPE, typeScreen)
        PeriodicWallpaperChangeWorker.cancel()
        PeriodicWallpaperChangeWorker.schedule(0)
    }

}