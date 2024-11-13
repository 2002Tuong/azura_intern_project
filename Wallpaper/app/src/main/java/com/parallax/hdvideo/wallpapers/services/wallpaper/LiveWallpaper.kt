package com.parallax.hdvideo.wallpapers.services.wallpaper

import android.app.WallpaperColors
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.parallax.hdvideo.wallpapers.ads.AppOpenAdManager
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class LiveWallpaper : WallpaperService() {

    @Inject
    lateinit var storage: LocalStorage

    override fun onCreateEngine(): Engine {
        return LiveWallpaperEngine(
            VideoEngine(
                applicationContext,
                storage
            )
        )
    }

    inner class LiveWallpaperEngine(val engineImpl: WallpaperEngine) : WallpaperService.Engine() {

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Logger.d("LiveWallpaper onSurfaceChanged")
            engineImpl.onSurfaceChanged(holder, format, width, height)
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            AppOpenAdManager.switchOnOff(false)
            engineImpl.setup(this, surfaceHolder)
            if (engineImpl is VideoEngine) {
                setTouchEventsEnabled(true)
            }
        }

        override fun onTouchEvent(event: MotionEvent) {
            engineImpl.onTouchEvent(event)
            super.onTouchEvent(event)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            Logger.d("LiveWallpaper onSurfaceCreated")
            engineImpl.onSurfaceCreated(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            Logger.d("LiveWallpaper onVisibilityChanged $visible")
            engineImpl.onVisibilityChanged(visible)
            super.onVisibilityChanged(visible)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            Logger.d("LiveWallpaper onSurfaceDestroyed")
            engineImpl.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            super.onDestroy()
            AppOpenAdManager.switchOnOff(true)
            Logger.d("LiveWallpaper onDestroy")
        }

        override fun notifyColorsChanged() {
            try {
                super.notifyColorsChanged()
            } catch (e: NullPointerException) {
                Logger.d("notifyColorsChanged", e)
            }
        }

        override fun onComputeColors(): WallpaperColors? {
            return super.onComputeColors()
        }
    }


    enum class WallpaperMode {
        IMAGE,  // Current wallpaper is single image
        VIDEO,  // Current wallpaper is single video
        PLAYLIST
        // PlayList is activating
    }
}