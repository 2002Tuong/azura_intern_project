package com.slideshowmaker.slideshow.modules.slide_show_package_2.renderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.modules.slide_show_package_2.data.FrameData
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.ShaderHelper

class SlideShowGlView : GLSurfaceView {

    private var slideRenderer: SlideRenderer?= null

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

    fun setSlideRenderer(render: SlideRenderer) {
        slideRenderer = render
        setRenderer(render)
    }

    fun drawSlide(frameData: FrameData) {
        slideRenderer?.changeFrameData(frameData)
    }

    fun changeTransition(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition) {
        queueEvent(Runnable {
            val shaderHandle =  ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(gsTransition.transitionCodeId))
            slideRenderer?.changeTransition(gsTransition, shaderHandle)
        })
    }

    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
            slideRenderer?.changeTheme(themeData)

        })
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