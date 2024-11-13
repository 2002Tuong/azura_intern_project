package com.slideshowmaker.slideshow.modules.slide_show_package_2.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.slideshowmaker.slideshow.modules.slide_show_package_2.SlideShowDrawer
import com.slideshowmaker.slideshow.modules.slide_show_package_2.data.FrameData
import com.slideshowmaker.slideshow.modules.theme.SlideThemeDrawer
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SlideRenderer(val themeData: ThemeData) : GLSurfaceView.Renderer {

    private var slideShowDrawer: SlideShowDrawer? = null
    private var slideThemeDrawer: SlideThemeDrawer? = null

    init {
        slideShowDrawer = SlideShowDrawer()
        slideThemeDrawer = SlideThemeDrawer(themeData)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        slideThemeDrawer?.prepare()
        slideShowDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        slideShowDrawer?.drawFrame()
        slideThemeDrawer?.drawFrame()
    }

    fun changeTheme(themeData: ThemeData) {
        slideThemeDrawer?.changeTheme(themeData)
        slideShowDrawer?.setUpdateTexture(true)
    }

    fun changeFrameData(frameData: FrameData) {
        slideShowDrawer?.changeFrameData(frameData)
    }

    fun changeTransition(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition, fragmentShaderHandle: Int) {
        slideShowDrawer?.changeTransition(gsTransition, fragmentShaderHandle)
    }
}