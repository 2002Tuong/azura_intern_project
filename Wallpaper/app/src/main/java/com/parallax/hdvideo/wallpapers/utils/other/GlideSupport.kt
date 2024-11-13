package com.parallax.hdvideo.wallpapers.utils.other

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.Transition.ViewAdapter
import com.bumptech.glide.request.transition.TransitionFactory
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.extension.toGlideUrl
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import java.io.File

object GlideSupport {

    fun load(image: ImageView, url: String?) {

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
//            .override(Target.SIZE_ORIGINAL)
//            .transition(DrawableTransitionOptions.with(TRANSITION_FACTORY))
            .dontTransform()

        Glide.with(image).load(url)
            .apply(requestOptions)
            .into(image)
    }

    fun load(image: ImageView, resourceId: Int) {

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
//            .override(Target.SIZE_ORIGINAL)
//            .transition(DrawableTransitionOptions.with(TRANSITION_FACTORY))
            .dontTransform()

        Glide.with(image).load(resourceId)
            .apply(requestOptions)
            .into(image)
    }

    fun loadBlurImage(image: ImageView, url: Uri) {
        Glide.with(image).load(url)
            .apply(bitmapTransform(BlurBitmapTransformation(image.context)))
            .into(image)
    }

    fun download(url: String, options: RequestOptions? = null): FutureTarget<File> {
        var requestOptions = options ?: RequestOptions()
            .override(Target.SIZE_ORIGINAL)
        requestOptions = requestOptions.priority(Priority.HIGH)
        val failRequestBuilder = Glide.with(WallpaperApp.instance).downloadOnly()
            .load(WallpaperURLBuilder.shared.getUrlFail(url, false))
        return Glide.with(WallpaperApp.instance)
            .downloadOnly()
            .load(url.toGlideUrl)
            .error(failRequestBuilder)
            .apply(requestOptions)
            .submit()
    }

    //option != null -> case set picture from local storage
    fun getBitmap(url: String, options: RequestOptions? = null) : Bitmap {
        var requestOptions = options ?: RequestOptions()
            .override(Target.SIZE_ORIGINAL)
        requestOptions = requestOptions.priority(Priority.HIGH)
        val fail = Glide.with(WallpaperApp.instance).asBitmap().load(WallpaperURLBuilder.shared.getUrlFail(url, false))
        return Glide.with(WallpaperApp.instance).asBitmap()
            .load(if (options != null) url else url.toGlideUrl)
            .error(fail).apply(requestOptions).submit().get()
    }

    fun clear(view: ImageView) {
        try {
            Glide.with(view).clear(view)
            view.setImageDrawable(null)
        } catch (e : IllegalArgumentException) {}
    }

    // run in background
    fun clearDiskCache() {
        Glide.get(WallpaperApp.instance).clearDiskCache()
    }

    fun clearMemory() {
        Glide.get(WallpaperApp.instance).clearMemory()
    }

    val TRANSITION = Transition { current: Drawable?, adapter: ViewAdapter ->
            if (adapter.view is ImageView) {
                val image = adapter.view as ImageView
                if (image.drawable == null) {
                    image.alpha = 0f
                    image.animate().alpha(1f)
                }
            }
            false
        }

    val TRANSITION_FACTORY = TransitionFactory { dataSource: DataSource, isFirstResource -> if (dataSource == DataSource.REMOTE) TRANSITION else NoTransition.get() }

}