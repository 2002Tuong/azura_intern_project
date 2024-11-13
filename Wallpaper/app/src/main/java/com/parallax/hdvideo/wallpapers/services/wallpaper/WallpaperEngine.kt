package com.parallax.hdvideo.wallpapers.services.wallpaper

import android.content.Context
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import java.lang.ref.WeakReference

abstract class WallpaperEngine(val context: Context, val storage: LocalStorage) {

    private var _engine: WeakReference<WallpaperService.Engine>? = null

    private var mSurfaceHolder: WeakReference<SurfaceHolder>? = null

    protected val visibility get() = _engine?.get()?.isVisible ?: false
    protected var widthValue = 0
    protected var heightValue = 0

    val onPreview : Boolean
        get() = _engine?.get()?.isPreview ?: false


    fun setup(engine: WallpaperService.Engine, surfaceHolder: SurfaceHolder) {
        this._engine = WeakReference(engine)
        this.mSurfaceHolder = WeakReference(surfaceHolder)
    }

    abstract fun onSurfaceCreated(holder: SurfaceHolder)

    fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        this.widthValue = width
        this.heightValue = height
    }

    open fun onVisibilityChanged(visible: Boolean)  {

    }

    open fun onTouchEvent(event: MotionEvent) {

    }

    open fun onSurfaceDestroyed(holder: SurfaceHolder) {
        _engine = null
        mSurfaceHolder = null
    }


    val surfaceHolder: SurfaceHolder?
        get() = mSurfaceHolder?.get()
    val engine: WallpaperService.Engine? get() = _engine?.get()
}