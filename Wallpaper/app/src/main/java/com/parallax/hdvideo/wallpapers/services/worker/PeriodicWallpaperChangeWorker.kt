package com.parallax.hdvideo.wallpapers.services.worker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.storage.database.AppDatabase
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import com.parallax.hdvideo.wallpapers.utils.other.ImageHelper
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlin.math.max

@HiltWorker
class PeriodicWallpaperChangeWorker @AssistedInject constructor(
    private val localStorage: LocalStorage,
    private val database: AppDatabase,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    private var model = WallpaperModel()
    override fun doWork(): Result {
        val playList = database.wallpaper().getPlaylist()
        if (playList.isNotEmpty()) {
            var curIndex = max(localStorage.playlistCurIndex, 0)
            if (curIndex >= playList.size) curIndex = 0
            model = playList[curIndex]
            val duration = localStorage.wallpaperChangeDelayTimeInMin
            if (model.isFromLocalStorage) {
                try {
                    ImageHelper.decodeBitmap(Uri.parse(model.url))?.let { setWallpaper(it) }
                } catch (e: Exception) {
                }
                localStorage.playlistCurIndex = curIndex + 1
            } else {
                val file = FileUtils.getPlayListFile(model.toUrl(isMin = false, isThumb = false))
                if (!file.exists()) {
                    if (downloadImage(model)) {
                        localStorage.playlistCurIndex = curIndex + 1
                    } else {
                        Logger.d("PeriodicWallpaperChangeWorker requiredNetwork")
                        schedule(delay = 1, requiredNetwork = true)
                        return Result.success()
                    }
                } else {
                    setWallpaper(file.path)
                    localStorage.playlistCurIndex = curIndex + 1
                }
                schedule(duration)
            }
        }
        return Result.success()
    }

    private fun downloadImage(model: WallpaperModel) : Boolean {
        try {
            if (NetworkUtils.isNetworkConnected()) {
                WallpaperURLBuilder.shared.setConfigUrl(localStorage)
                val url = model.toUrl(isMin = false, isThumb = false)
                var file = GlideSupport.download(url).get()
                if (file != null && file.exists()) {
                    file = FileUtils.copyFileToPlaylistFolder(file, url)
                    val dir = file.path
                    setWallpaper(dir)
                    return true
                }
            }
        } catch (e: Exception) {
            Logger.d("PeriodicWallpaperChangeWorker Error downloadPhoto", e.message)
        }
        return false
    }

    private fun setWallpaper(path: String) {
        val bitmap = ImageHelper.decodeBitmap(path)
        setWallpaper(bitmap)
    }

    private fun setWallpaper(bitmap: Bitmap) {
        val position = localStorage.getData(AppConstants.PreferencesKey.AUTO_CHANGE_WALLPAPER_SCREEN_TYPE, Int::class) ?: 0
        WallpaperHelper.setWallpaper(bitmap, WallpaperHelper.WallpaperType.init(position), !model.isTopDevices, model)
        Logger.d("PeriodicWallpaperChangeWorker set wallpaper")
    }

    companion object {
        const val TASK_NAME = "WallpaperChangerTask"

        // delay Minute
        fun schedule(delay: Int, requiredNetwork: Boolean = false) {

            val workBuilder = OneTimeWorkRequestBuilder<PeriodicWallpaperChangeWorker>()
                .setInitialDelay(delay.toLong(), TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            if (requiredNetwork) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED).build()
                workBuilder.setConstraints(constraints)
            }

            WorkManager.getInstance(WallpaperApp.instance)
                .enqueueUniqueWork(TASK_NAME, ExistingWorkPolicy.REPLACE, workBuilder.build())
            Logger.d("started schedule wallpaper")
        }

        fun cancel() {
            WorkManager.getInstance(WallpaperApp.instance)
                .cancelUniqueWork(TASK_NAME)
        }
    }
}