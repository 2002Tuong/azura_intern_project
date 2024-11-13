package com.slideshowmaker.slideshow.ui.custom.player

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.slideshowmaker.slideshow.utils.Logger

class VideoPlayGLView(context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {


    private lateinit var playRenderer: VideoPlayRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(videoPlayRenderer: VideoPlayRenderer) {
        playRenderer = videoPlayRenderer
        setRenderer(playRenderer)
        Logger.e("set renderer")
    }

}