package com.slideshowmaker.slideshow.ui.custom.player

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoPlayRenderer (val videoPath: String, val glView:GLSurfaceView, var autoPlay:Boolean = true)  : GLSurfaceView.Renderer {

    var vidPlayDrawer: VideoPlayDrawer? = null
    init {
       vidPlayDrawer = VideoPlayDrawer(videoPath, autoPlay)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        setViewPort()
        vidPlayDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        vidPlayDrawer?.drawFrame()
    }

    private fun setViewPort() {
        val viewSize = glView.width

        val videoSize = MediaHelper.getVideoSize(videoPath)
        val viewPortX:Int
        val viewPortY:Int
        val viewPortWidth:Int
        val viewPortHeight:Int
        if(videoSize.width > videoSize.height) {
            viewPortWidth = viewSize
            viewPortHeight = viewSize*videoSize.height/videoSize.width
            viewPortY = (viewSize-viewPortHeight)/2
            viewPortX = 0
        } else {
            viewPortHeight = viewSize
            viewPortWidth = viewSize*videoSize.width/videoSize.height
            viewPortY = 0
            viewPortX = (viewSize-viewPortWidth)/2
        }
        GLES20.glViewport(viewPortX, viewPortY, viewPortWidth,viewPortHeight)
    }

    fun getCurrentPosition():Int? {
        return vidPlayDrawer?.getCurrentPosition()
    }

    fun onDestroy() {
        Logger.e("video drawer = $vidPlayDrawer")
        vidPlayDrawer?.onDestroy()
    }

    fun onPause() {
        vidPlayDrawer?.onPause()
    }

    fun onPlayVideo() {
        vidPlayDrawer?.playVideo()
    }

    fun seekTo(timeMilSec:Int) {
        vidPlayDrawer?.seekTo(timeMilSec)
    }

}