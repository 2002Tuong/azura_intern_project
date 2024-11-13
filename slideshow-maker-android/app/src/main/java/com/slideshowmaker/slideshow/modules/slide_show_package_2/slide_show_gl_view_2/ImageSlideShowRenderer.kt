package com.slideshowmaker.slideshow.modules.slide_show_package_2.slide_show_gl_view_2

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.modules.slide_show_package_2.SlideShowDrawer
import com.slideshowmaker.slideshow.modules.slide_show_package_2.data.SlideShow
import com.slideshowmaker.slideshow.modules.theme.SlideThemeDrawer
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.ShaderHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageSlideShowRenderer() : GLSurfaceView.Renderer {

    private var slideShowDrawer: SlideShowDrawer? = null

    private var themeDrawer: SlideThemeDrawer? = null

    private var _themeData = ThemeData()
    val themeData get() = _themeData

    private lateinit var _slideShow: SlideShow
    val slideShow get() = _slideShow

    private var _gsTransition =
        com.slideshowmaker.slideshow.modules.transition.transition.GSTransition()
    val gsTransition get() = _gsTransition

    init {
        themeDrawer = SlideThemeDrawer(_themeData)
        slideShowDrawer = SlideShowDrawer()
    }

    fun initData(imageList: ArrayList<String>) {
        _slideShow = SlideShow(imageList)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        slideShowDrawer?.prepare()
        themeDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        slideShowDrawer?.drawFrame()
        themeDrawer?.drawFrame()
    }

    fun drawSlideByTime(timeMilSec:Int) {
        val frameData = _slideShow.getFrameByVideoTime(timeMilSec)
        slideShowDrawer?.changeFrameData(frameData)
    }

     fun changeTheme(themeData: ThemeData) {
         _themeData = themeData
         themeDrawer?.changeTheme(themeData)
         slideShowDrawer?.setUpdateTexture(true)
    }

     fun changeTransition(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition) {
         _gsTransition = gsTransition
        val shaderHandle =  ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(gsTransition.transitionCodeId))
        slideShowDrawer?.changeTransition(gsTransition, shaderHandle)
    }

    fun getMaxDuration():Int = _slideShow.getTotalDuration()

    fun getDelayTimeSec():Int = _slideShow.delayTimeInSec

    fun changeDelayTimeSec(delayTime:Int) {
        _slideShow.updateTime(delayTime)
    }

    fun repeat() {
        _slideShow.repeat()
    }

    fun onPlay() {
        themeDrawer?.playTheme()
    }

    fun onPause() {
        themeDrawer?.pauseTheme()
    }

    fun seekTo(timeMilSec:Int, onComplete:()->Unit) {
        _slideShow.seekTo(timeMilSec, onComplete)
    }

    private fun getFragmentShader(transitionCodeId: Int): String {
        val transitionCode = RawResourceReader.readTextFileFromRawResource(VideoMakerApplication.getContext(), transitionCodeId)
        return "precision mediump float;\n" +
                "varying vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform float progress, ratio, _fromR, _toR, _zoomProgress;\n" +
                "\n" +
                "vec4 getFromColor(vec2 uv){\n" +
                "    return texture2D(from, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}\n" +
                "vec4 getToColor(vec2 uv){\n" +
                "    return texture2D(to, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}"+
                transitionCode +
                "void main(){gl_FragColor=transition(_uv);}"
    }

}