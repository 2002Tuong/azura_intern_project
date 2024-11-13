package com.slideshowmaker.slideshow.modules.slide_video_package

import android.content.Context
import android.graphics.Point
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Size
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData

class VideoSlideGLView (context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {

    private lateinit var videoSlideRenderer: VideoSlideRenderer


    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(videoSlideRenderer: VideoSlideRenderer) {
        this.videoSlideRenderer = videoSlideRenderer
        setRenderer(this.videoSlideRenderer)
    }


    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
            videoSlideRenderer.changeTheme(themeData)
        })
    }

    fun changeVideo(videoDataForSlide: VideoDataForSlide) {
        queueEvent(Runnable {
            val size = videoDataForSlide.size
            val viewPortXPos:Int
            val viewPortYPos:Int
            val viewPortWidth:Int
            val viewPortHeight:Int
            if(size.width > size.height) {
                viewPortWidth = width
                viewPortHeight = width*size.height/size.width
                viewPortYPos = (height-viewPortHeight)/2
                viewPortXPos = 0
            } else {
                viewPortHeight = height
                viewPortWidth = height*size.width/size.height
                viewPortYPos = 0
                viewPortXPos = (width-viewPortWidth)/2
            }
            videoSlideRenderer.changeVideo(videoDataForSlide.path, Size(viewPortWidth, viewPortHeight), Point(viewPortXPos, viewPortYPos))
        })

    }




    fun seekTo(timeMilSec:Int) {

    }

}