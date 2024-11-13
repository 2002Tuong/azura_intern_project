package com.slideshowmaker.slideshow.modules.slide_show_package_2

import android.opengl.GLES20
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.modules.slide_show_package_2.data.FrameData
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.ShaderHelper
import com.slideshowmaker.slideshow.utils.TextureHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SlideShowDrawer {
    private var frameData: FrameData? = null

    private val bytesPerFloat = 4

    private var updateTexture = true

    private val vertexData = floatArrayOf(
        -1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, 1f, 0.0f,
        1f, -1f, 0.0f
    )
    private var vertexBuffer: FloatBuffer

    private val positionDataSize = 3
    private val colorDataSize = 4
    private val normalDataSize = 3
    private val textureCoordinateDataSize = 2


    private var programHandle = 0

    private var textureFromDataHandle = 0
    private var textureToDataHandle = 0

    private var gsTransition =
        com.slideshowmaker.slideshow.modules.transition.transition.GSTransition()

    private var fragmentShaderHandle = 0
    private var vertexShaderHandle = 0

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * bytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertexData).position(0)
    }

    fun prepare() {
        vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        fragmentShaderHandle = ShaderHelper.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(gsTransition.transitionCodeId)
        )

        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle, fragmentShaderHandle,
            arrayOf("_p")
        )
    }

    fun prepare(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition) {
        this.gsTransition = gsTransition
        vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        fragmentShaderHandle = ShaderHelper.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(this.gsTransition.transitionCodeId)
        )

        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle, fragmentShaderHandle,
            arrayOf("_p")
        )
    }

    fun drawFrame() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        frameData?.let {
            if (updateTexture) {
                GLES20.glDeleteTextures(
                    2,
                    intArrayOf(textureFromDataHandle, textureToDataHandle),
                    0
                )
                textureFromDataHandle = TextureHelper.loadTexture(it.fromBitmap)
                textureToDataHandle = TextureHelper.loadTexture(it.toBitmap)
                updateTexture = false
            }
            drawSlide(it)
        }
    }

    private fun drawSlide(frameData: FrameData) {
        GLES20.glUseProgram(programHandle)

        val locationAttr = GLES20.glGetAttribLocation(programHandle, "_p")
        GLES20.glEnableVertexAttribArray(locationAttr)
        GLES20.glVertexAttribPointer(locationAttr, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        val textureFromLocation = GLES20.glGetUniformLocation(programHandle, "from")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureFromDataHandle)
        GLES20.glUniform1i(textureFromLocation, 0)

        val textureToLocation = GLES20.glGetUniformLocation(programHandle, "to")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureToDataHandle)
        GLES20.glUniform1i(textureToLocation, 1)

        val zoomProgressLocation = GLES20.glGetUniformLocation(programHandle, "_zoomProgress")
        GLES20.glUniform1f(zoomProgressLocation, frameData.zoomProgress)

        val progressLocation = GLES20.glGetUniformLocation(programHandle, "progress")
        GLES20.glUniform1f(progressLocation, frameData.progress)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)

    }

    fun changeFrameData(frameData: FrameData?) {
        if (this.frameData?.slideId == frameData?.slideId) {
            updateTexture = false
            this.frameData = frameData
        } else {
            this.frameData = frameData
            updateTexture = true
        }
    }

    fun setUpdateTexture(b: Boolean) {
        updateTexture = b
    }

    fun changeTransition(gsTransition: com.slideshowmaker.slideshow.modules.transition.transition.GSTransition, fragmentShaderHandle: Int) {
        if (this.gsTransition.transitionCodeId == gsTransition.transitionCodeId) return
        this.gsTransition = gsTransition
        this.fragmentShaderHandle = fragmentShaderHandle
        GLES20.glDeleteProgram(programHandle)
        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle, this.fragmentShaderHandle,
            arrayOf("_p")
        )
    }

    private fun getVertexShader(): String {
        return "attribute vec2 _p;\n" +
                "varying vec2 _uv;\n" +
                "void main() {\n" +
                "gl_Position = vec4(_p,0.0,1.0);\n" +
                "_uv = vec2(0.5, 0.5) * (_p+vec2(1.0, 1.0));\n" +
                "}"
    }

    private fun getFragmentShader(transitionCodeId: Int): String {

        val transitionCode = RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            transitionCodeId
        )

        return "precision mediump float;\n" +
                "varying vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform float progress, ratio, _fromR, _toR;\n" +
                "uniform highp float _zoomProgress;" +
                "\n" +
                "vec4 getFromColor(vec2 uv){\n" +
                "    return texture2D(from, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}\n" +
                "vec4 getToColor(vec2 uv){\n" +
                "    return texture2D(to, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}" +
                transitionCode +
                "void main(){gl_FragColor=transition(_uv);}"
    }
}