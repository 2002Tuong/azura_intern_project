package com.slideshowmaker.slideshow.modules.image_slide_show.drawer

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES20
import android.opengl.Matrix
import android.view.Surface
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import com.slideshowmaker.slideshow.utils.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImageSlideThemeDrawer(var themeData: ThemeData) : SurfaceTexture.OnFrameAvailableListener {

    private var isPlay = true

    private val FLOAT_SIZE_BYTES = 4
    private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_SIZE_BYTES
    private val TEXTURE_VERTICES_DATA_STRIDE_BYTES = 2 * FLOAT_SIZE_BYTES
    private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 0

    private val triangleVerticesData = floatArrayOf(
        -1.0f, -1.0f, 0f, 1.0f,
        -1.0f, 0f, -1.0f, 1.0f, 0f, 1.0f, 1.0f, 0f
    )

    private val textureVerticesData = floatArrayOf(
        0f, 0.0f, 1.0f, 0f,
        0.0f, 1f, 1.0f, 1.0f
    )
    private var triangleVertices: FloatBuffer
    private var textureVertices: FloatBuffer
    private val GL_TEXTURE_EXTERNAL_OES = 0x8D65

    private val MVPMatrix = FloatArray(16)
    private val STMatrix = FloatArray(16)

    private var mProgram = 0
    private var mTextureID = 0

    private var maPositionHandle = 0
    private var maTextureHandle = 0

    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0

    private lateinit var mSurface: SurfaceTexture

    private var updateSurface = false

    private var mediaPlayer: MediaPlayer? = null
    private val vertexShader = ("uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n" + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n" + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" + "}\n")

    private val fragmentShader = ("#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "vec4 p = texture2D(sTexture, vTextureCoord); "
            + "if(p.g<0.1 && p.r < 0.1 && p.b<0.1) {discard;p.a=0.0;}"
            + "gl_FragColor = p;\n"
            + "}\n")

    init {

        triangleVertices = ByteBuffer
            .allocateDirect(
                triangleVerticesData.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleVertices.put(triangleVerticesData).position(0)

        textureVertices = ByteBuffer
            .allocateDirect(
                textureVerticesData.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVertices.put(textureVerticesData).position(0)

        Matrix.setIdentityM(STMatrix, 0)
    }

    fun prepare() {
        val vertexThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        mProgram = ShaderHelper.createAndLinkProgram(
            vertexThemeShaderHandle, fragmentThemeShaderHandle,
            arrayOf("uSTMatrix", "uMVPMatrix", "aTextureCoord")
        )

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]

        mSurface = SurfaceTexture(mTextureID)
        mSurface.setOnFrameAvailableListener(this)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        //GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        ) // for this tutorial: use GL_CLAMP_TO_EDGE to prevent semi-transparent borders. Due to interpolation it takes texels from next repeat

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        /*GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20. GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)*/

        synchronized(this) { updateSurface = false }

        if (themeData.themeVideoFilePath != "none")
            doPlayVideo()
    }

    fun prepare(themeData: ThemeData) {
        this.themeData = themeData
        val vertexThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        mProgram = ShaderHelper.createAndLinkProgram(
            vertexThemeShaderHandle, fragmentThemeShaderHandle,
            arrayOf("uSTMatrix", "uMVPMatrix", "aTextureCoord")
        )

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]

        mSurface = SurfaceTexture(mTextureID)
        mSurface.setOnFrameAvailableListener(this)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        //GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        ) // for this tutorial: use GL_CLAMP_TO_EDGE to prevent semi-transparent borders. Due to interpolation it takes texels from next repeat

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        /*GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20. GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)*/

        synchronized(this) { updateSurface = false }

        if (themeData.themeVideoFilePath != "none") {
            isPlay = true
            doPlayVideo()
        }

    }

    fun drawFrame() {
        synchronized(this) {
            if (updateSurface) {
                mSurface.updateTexImage()
                mSurface.getTransformMatrix(STMatrix)
                updateSurface = false
            }
        }

        GLES20.glUseProgram(mProgram)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)

        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            triangleVertices
        )
        GLES20.glEnableVertexAttribArray(maPositionHandle)

        textureVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            maTextureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            TEXTURE_VERTICES_DATA_STRIDE_BYTES,
            textureVertices
        )
        GLES20.glEnableVertexAttribArray(maTextureHandle)

        Matrix.setIdentityM(MVPMatrix, 0)

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MVPMatrix, 0)
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, STMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glFinish()


    }

    fun seekTo(timeMilSec: Int) {
        mediaPlayer?.seekTo(timeMilSec)
    }

    private fun doPlayVideo() {
        val surface = Surface(mSurface)
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(themeData.themeVideoFilePath)
        mediaPlayer?.setSurface(surface)
        mediaPlayer?.setOnCompletionListener {

        }
        mediaPlayer?.isLooping = true
        surface.release()
        try {
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.isLooping = true
                if (isPlay) {
                    playTheme()
                } else {
                    pauseTheme()
                }

            }
            mediaPlayer?.prepare()
        } catch (e: Exception) {
        }
    }

    fun playTheme() {
        if (themeData.themeVideoFilePath == "none") {
            isPlay = true
            return
        }
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    isPlay = true
                }
            }
        } catch (e: java.lang.Exception) {

        }

    }

    fun pauseTheme() {
        try {
            if (themeData.themeVideoFilePath == "none") {
                isPlay = false
                return
            }
            mediaPlayer?.let {
                if (it.isPlaying) {
                    mediaPlayer?.pause()
                    isPlay = false
                }
            }
        } catch (e: java.lang.Exception) {
            Logger.e("pauseTheme ------> ${e.toString()}")
        }

    }

    fun destroyTheme() {
        mediaPlayer?.let {
            it.release()
        }
    }

    private var videoDuration = 0
    fun changeTheme(themeData: ThemeData) {
        Logger.e("theme data path = ${themeData.themeVideoFilePath}")
        if (this.themeData.themeVideoFilePath != themeData.themeVideoFilePath) {
            if (themeData.themeVideoFilePath != "none") {
                videoDuration = MediaHelper.getVideoDuration(themeData.themeVideoFilePath)
            } else {
                videoDuration = 0
            }
            GLES20.glDeleteProgram(mProgram)
            this.themeData = themeData
            mediaPlayer?.release()
            if (this.themeData.themeVideoFilePath != "none")
                prepare()
        }

    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this) {
            updateSurface = true
        }
    }

    fun doSeekTo(currentVideoTime: Int) {
        try {
            if (currentVideoTime == 0) {
                mediaPlayer?.seekTo(0)
            } else {
                mediaPlayer?.seekTo(videoDuration % currentVideoTime)
            }
        } catch (e: Exception) {

        }
    }

}