package com.slideshowmaker.slideshow.modules.slide_show_package_2.slide_show_gl_view_2

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData

class ImageSlideShowGLView(context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {

    private lateinit var imageSlideShowRenderer: ImageSlideShowRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(imageSlideShowRenderer: ImageSlideShowRenderer) {
        this.imageSlideShowRenderer = imageSlideShowRenderer
        setRenderer(imageSlideShowRenderer)
    }

    fun drawSlideByTime(timeMilSec:Int) {
        imageSlideShowRenderer.drawSlideByTime(timeMilSec)
    }

    fun changeTransition(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition) {
        queueEvent(Runnable {
            imageSlideShowRenderer.changeTransition(gsTransition)
        })
    }

    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
            imageSlideShowRenderer.changeTheme(themeData)
        })
    }

    fun seekTo(timeMilSec:Int) {

    }

}