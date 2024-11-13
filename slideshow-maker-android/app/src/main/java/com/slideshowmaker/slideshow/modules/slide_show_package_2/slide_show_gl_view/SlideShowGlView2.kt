package com.slideshowmaker.slideshow.modules.slide_show_package_2.slide_show_gl_view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData

class SlideShowGlView2 : GLSurfaceView {

    var slideShowRenderer: SlideShowRenderer? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        init()
    }

    private fun init() {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(slideShowRenderer: SlideShowRenderer) {
        this.slideShowRenderer = slideShowRenderer
        setRenderer(slideShowRenderer)
    }

    fun drawSlide(timeMilSec:Int) {
        slideShowRenderer?.drawSlideByTime(timeMilSec)
    }

    fun changeTransition(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition) {
        queueEvent(Runnable {
            slideShowRenderer?.changeTransition(gsTransition)
        })
    }

    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
           slideShowRenderer?.changeTheme(themeData)
        })
    }

}