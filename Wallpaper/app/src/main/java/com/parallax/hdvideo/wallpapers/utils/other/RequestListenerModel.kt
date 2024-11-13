package com.parallax.hdvideo.wallpapers.utils.other

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel

open class RequestListenerModel(var wallpaper: WallpaperModel? = null): RequestListener<Drawable> {
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
        onError()
        onFinal()
        return false
    }

    override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        onSuccess()
        onFinal()
        return false
    }

    open fun onSuccess() {

    }

    open fun onError() {

    }

    open fun onFinal() {

    }

}