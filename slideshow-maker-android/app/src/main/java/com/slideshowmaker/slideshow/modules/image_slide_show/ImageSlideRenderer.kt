package com.slideshowmaker.slideshow.modules.image_slide_show

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideDrawer
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideFrame
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideThemeDrawer
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.ShaderHelper
import timber.log.Timber
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageSlideRenderer(gsTransition: GSTransition) : GLSurfaceView.Renderer {

    private var imageSlideDrawer: ImageSlideDrawer? = null
    private var imageSlideThemeDrawer: ImageSlideThemeDrawer? = null
    private var gsTransition = gsTransition
    private var themeData = ThemeData()

    init {
        imageSlideDrawer = ImageSlideDrawer()
        imageSlideThemeDrawer = ImageSlideThemeDrawer(themeData)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        imageSlideDrawer?.prepare(gsTransition)
        if (themeData.themeVideoFilePath != "none") {
            imageSlideThemeDrawer?.prepare()
        }

    }

    var mProjectionMatrix = FloatArray(16)

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Timber.d("onSurfaceChanged w:%d h:%d", width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Apply the projection and view transformation

        // Apply the projection and view transformation
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        imageSlideDrawer?.ratio = width.toFloat() / height

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        imageSlideDrawer?.drawFrame()
        if (themeData.themeVideoFilePath != "none")
            imageSlideThemeDrawer?.drawFrame()
    }

    fun changeFrameData(frameData: ImageSlideFrame) {
        imageSlideDrawer?.changeFrameData(frameData)
    }


    fun resetData() {
        imageSlideDrawer?.reset()
    }

    private val fragmentShaders = HashMap<Int, Int>()
    private val shaderCompilerThread = HandlerThread("ShaderCompilerThread").apply { start() }
    private val shaderCompilerHandler = Handler(shaderCompilerThread.looper)

    fun changeTransition(gsTransition: GSTransition) {
        this.gsTransition = gsTransition

        // Logger.e(getFragmentShader(gsTransition.transitionCodeId))
        val transitionCodeId = gsTransition.transitionCodeId
        Timber.d("ImageSlideRender Thread is ${Thread.currentThread().name}")
        val handle = ShaderHelper.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(transitionCodeId)
        )
//        val handle = ShaderHelper.compileShader(
//            GLES20.GL_FRAGMENT_SHADER,
//            getFragmentShader(gsTransition.transitionCodeId)
//        )
        //  val handle =  ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(code))
        imageSlideDrawer?.changeTransition(gsTransition, handle)
    }

    fun changeTheme(themeData: ThemeData) {
        if (this.themeData.themeVideoFilePath != themeData.themeVideoFilePath) {
            this.themeData = themeData
            imageSlideThemeDrawer?.changeTheme(themeData)
            imageSlideDrawer?.setUpdateTexture(true)
        }

    }

    fun onPause() {
        imageSlideThemeDrawer?.pauseTheme()
    }

    fun onPlay() {
        imageSlideThemeDrawer?.playTheme()
    }

    fun onDestroy() {
        imageSlideThemeDrawer?.destroyTheme()
    }

    fun setUpdateTexture(needUpadate: Boolean) {
        imageSlideDrawer?.setUpdateTexture(needUpadate)
    }

    fun seekTheme(videoTimeMs: Int) {
        imageSlideThemeDrawer?.doSeekTo(videoTimeMs)
    }

    private fun getFragmentShader(transitionCodeId: Int): String {
        val transitionCode = RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            transitionCodeId
        )
        return "precision highp float;" +
                "varying highp vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform sampler2D fromLookupTexture, toLookupTexture;\n" +
                "uniform float progress, ratio, _fromR, _toR;\n" +
                "uniform highp float _zoomProgress, _zoomProgress1;\n" +
                "\n" +
                "vec4 lookup(vec4 textureColor, sampler2D lookupBitmap, vec2 uv) {\n" +
                "    //highp vec4 textureColor = texture2D(inputTexture, uv);\n" +
                "\n" +
                "    highp float blueColor = textureColor.b * 63.0;\n" +
                "\n" +
                "    highp vec2 quad1;\n" +
                "    quad1.y = floor(floor(blueColor) / 8.0);\n" +
                "    quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
                "\n" +
                "    highp vec2 quad2;\n" +
                "    quad2.y = floor(ceil(blueColor) / 8.0);\n" +
                "    quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
                "\n" +
                "    highp vec2 texPos1;\n" +
                "    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "\n" +
                "    highp vec2 texPos2;\n" +
                "    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "\n" +
                "    lowp vec4 newColor1 = texture2D(lookupBitmap, texPos1);\n" +
                "    lowp vec4 newColor2 = texture2D(lookupBitmap, texPos2);\n" +
                "\n" +
                "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
                "\n" +
                "    return mix(textureColor, vec4(newColor.rgb, textureColor.w), 1.);\n" +
                "}\n" +
                "\n" +
                "vec4 getFromColor(vec2 uv){\n" +
                "    return lookup(texture2D(from, vec2(1.0, -1.0)*uv*_zoomProgress), fromLookupTexture, _uv);\n" +
                "}\n" +
                "vec4 getToColor(vec2 uv){\n" +
                "    return lookup(texture2D(to, vec2(1.0, -1.0)*uv*_zoomProgress1), toLookupTexture, _uv) ;\n" +
                "}\n" +
                "\n" +
                transitionCode +
                "void main()\n" +
                "{\n" +
                "    gl_FragColor=transition(_uv);\n" +
                "}"
    }

}