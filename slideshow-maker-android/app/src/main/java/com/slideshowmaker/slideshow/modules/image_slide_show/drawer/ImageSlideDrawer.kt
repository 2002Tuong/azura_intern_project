package com.slideshowmaker.slideshow.modules.image_slide_show.drawer

import android.opengl.GLES20
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.ShaderHelper
import com.slideshowmaker.slideshow.utils.TextureHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImageSlideDrawer {

    @Volatile
    private var frameData: ImageSlideFrame? = null

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
    private var vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexData.size * bytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    private val positionDataSize = 3
    private val colorDataSize = 4
    private val normalDataSize = 3
    private val textureCoordinateDataSize = 2


    private var programHandle = 0

    private var textureFromDataHandle = 0
    private var textureToDataHandle = 0

    private var lookupTextureFromDataHandle = 0
    private var lookupTextureToDataHandle = 0
    private var transition =
        GSTransition()

    private var fragmentShaderHandle = 0
    private var vertexShaderHandle = 0

    var ratio = 1f;

    init {
        vertexBuffer.put(vertexData).position(0)
    }

    fun prepare() {
        vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        fragmentShaderHandle = ShaderHelper.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(transition.transitionCodeId)
        )

        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle,
            fragmentShaderHandle,
            arrayOf("_p")
        )
        synchronized(this) { updateTexture = true }
    }

    fun prepare(gsTransition: GSTransition) {
        transition = gsTransition
        vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        fragmentShaderHandle = ShaderHelper.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(transition.transitionCodeId)
        )

        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle,
            fragmentShaderHandle,
            arrayOf("_p")
        )
        synchronized(this) { updateTexture = true }
    }

    fun drawFrame() {
        frameData?.let {
            if (updateTexture) {
                GLES20.glDeleteTextures(
                    4,
                    intArrayOf(
                        textureFromDataHandle,
                        textureToDataHandle,
                        lookupTextureFromDataHandle,
                        lookupTextureToDataHandle
                    ),
                    0
                )
                textureFromDataHandle = TextureHelper.loadTexture(it.fromBitmap)
                textureToDataHandle = TextureHelper.loadTexture(it.toBitmap)
                lookupTextureFromDataHandle = TextureHelper.loadTexture(it.fromLookupBitmap)
                lookupTextureToDataHandle = TextureHelper.loadTexture(it.toLookupBitmap)
                synchronized(this) { updateTexture = false }
                drawSlide(it)
            } else {
                drawSlide(it)
            }

        }
    }

    private fun drawSlide(frameData: ImageSlideFrame) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
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

        val lookupTextureFromLocation =
            GLES20.glGetUniformLocation(programHandle, "fromLookupTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lookupTextureFromDataHandle)
        GLES20.glUniform1i(lookupTextureFromLocation, 2)

        val lookupTextureToLocation = GLES20.glGetUniformLocation(programHandle, "toLookupTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lookupTextureToDataHandle)
        GLES20.glUniform1i(lookupTextureToLocation, 3)

        val zoomProgressLocation = GLES20.glGetUniformLocation(programHandle, "_zoomProgress")
        GLES20.glUniform1f(zoomProgressLocation, frameData.zoomProgress)

        val zoom1ProgressLocation = GLES20.glGetUniformLocation(programHandle, "_zoomProgress1")
        GLES20.glUniform1f(zoom1ProgressLocation, frameData.zoomProgress1)

        val progressLocation = GLES20.glGetUniformLocation(programHandle, "progress")
        GLES20.glUniform1f(progressLocation, frameData.progress)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }


    fun setUpdateTexture(b: Boolean) {
        updateTexture = b
        Logger.e("setUpdateTexture = $b")
        if (b) {
            frameData = null
            synchronized(this) {
                updateTexture = true
            }
        }
    }

    fun reset() {
        frameData = null
        synchronized(this) {
            updateTexture = true
        }
    }

    fun changeFrameData(frameData: ImageSlideFrame?, isRender: Boolean = false) {
        /*        if(mFrameData == null) {
                    mFrameData = frameData
                    mUpdateTexture = true
                } else {*/

        if (!isRender) {
            this.frameData = frameData
            return
        }
        // mFrameData = frameData


        if (this.frameData == null) {
            this.frameData = frameData
            // mUpdateTexture = true
            synchronized(this) {
                updateTexture = true
            }

        } else {
            this.frameData?.zoomProgress = frameData?.zoomProgress ?: 1f
            this.frameData?.zoomProgress1 = frameData?.zoomProgress1 ?: 1f
            if (this.frameData?.slideId == frameData?.slideId) {
                this.frameData?.progress = frameData?.progress ?: 0f
                //  mUpdateTexture = false
                synchronized(this) {
                    updateTexture = false
                }
            } else {
                this.frameData = frameData
                // mUpdateTexture = true
                synchronized(this) {
                    updateTexture = true
                }

            }
        }


        // }

    }

    fun changeTransition(gsTransition: GSTransition, fragmentShaderHandle: Int) {
        if (transition.transitionCodeId == gsTransition.transitionCodeId) return
        transition = gsTransition
        this.fragmentShaderHandle = fragmentShaderHandle
        val newProgramHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle, this.fragmentShaderHandle,
            arrayOf("_p")
        )
        if (newProgramHandle != 0) {
            GLES20.glDeleteProgram(programHandle)
            programHandle = newProgramHandle
        } else {

        }
    }

    private fun getVertexShader(): String {
        return "attribute vec2 _p;\n" +
                "varying vec2 _uv;\n" +
                "void main() {\n" +
                "gl_Position = vec4(_p,0.0,1.0);\n" +
                "_uv = vec2(0.5, 0.5) * (_p+vec2(1.0, 1.0));\n" +
                "}"
    }

    /*    private fun getFragmentShader(transitionCodeId: Int): String {

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
        }*/

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
                "uniform highp float _zoomProgress,_zoomProgress1;\n" +
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
                "" +
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