package com.slideshowmaker.slideshow.modules.image_slide_show

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition

class ImageSlideGLView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private lateinit var imageSlideRenderer: ImageSlideRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = false
    }

    fun doSetRenderer(imageSlideRenderer: ImageSlideRenderer) {
        this.imageSlideRenderer = imageSlideRenderer

        setRenderer(imageSlideRenderer)
    }


    fun changeTransition(gsTransition: GSTransition) {

        queueEvent(Runnable {
            imageSlideRenderer.changeTransition(gsTransition)
        })
    }




    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
            imageSlideRenderer.changeTheme(themeData)
        })
    }

}