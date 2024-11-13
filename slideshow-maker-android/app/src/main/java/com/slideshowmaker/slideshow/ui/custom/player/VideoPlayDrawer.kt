package com.slideshowmaker.slideshow.ui.custom.player

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES20
import android.opengl.Matrix
import android.view.Surface
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VideoPlayDrawer(var videoPath:String, var autoPlay:Boolean = true)  : SurfaceTexture.OnFrameAvailableListener {



    private val FLOAT_SIZE_BYTES = 4
    private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_SIZE_BYTES
    private val TEXTURE_VERTICES_DATA_STRIDE_BYTES = 2 * FLOAT_SIZE_BYTES
    private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 0

    private val triangleVerticesArray = floatArrayOf(
        -1.0f, -1.0f, 0f, 1.0f,
        -1.0f, 0f, -1.0f, 1.0f, 0f, 1.0f, 1.0f, 0f
    )

    private val textureVerticesArray = floatArrayOf(
        0f, 0.0f, 1.0f, 0f,
        0.0f, 1f, 1.0f, 1.0f
    )
    private var triangleVertices: FloatBuffer
    private var textureVertices: FloatBuffer
    private val GL_TEXTURE_EXTERNAL_OES = 0x8D65

    private val MVPMatrix = FloatArray(16)
    private val STMatrix = FloatArray(16)

    private var programHandle = 0
    private var textureIDHandle = 0

    private var maPositionHandle = 0
    private var maTextureHandle = 0

    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0

    private lateinit var surfaceTexture: SurfaceTexture

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
            + "gl_FragColor = p;\n"
            + "}\n")

    init {

        triangleVertices = ByteBuffer
            .allocateDirect(
                triangleVerticesArray.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleVertices.put(triangleVerticesArray).position(0)

        textureVertices = ByteBuffer
            .allocateDirect(
                textureVerticesArray.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVertices.put(textureVerticesArray).position(0)

        Matrix.setIdentityM(STMatrix, 0)

    }

    fun prepare() {
        val vertexThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        programHandle = ShaderHelper.createAndLinkProgram(
            vertexThemeShaderHandle, fragmentThemeShaderHandle,
            arrayOf("uSTMatrix", "uMVPMatrix", "aTextureCoord")
        )

        maPositionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(programHandle, "aTextureCoord")
        muMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uSTMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureIDHandle = textures[0]

        surfaceTexture = SurfaceTexture(textureIDHandle)
        surfaceTexture.setOnFrameAvailableListener(this)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureIDHandle)


        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        synchronized(this) { updateSurface = false }

        doPlayVideo()
    }
    private var volume1 = 1f
    fun prepare(volume:Float) {
        volume1 = volume
        val vertexThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        programHandle = ShaderHelper.createAndLinkProgram(
            vertexThemeShaderHandle, fragmentThemeShaderHandle,
            arrayOf("uSTMatrix", "uMVPMatrix", "aTextureCoord")
        )

        maPositionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(programHandle, "aTextureCoord")
        muMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uSTMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureIDHandle = textures[0]

        surfaceTexture = SurfaceTexture(textureIDHandle)
        surfaceTexture.setOnFrameAvailableListener(this)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureIDHandle)


        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        synchronized(this) { updateSurface = false }

        doPlayVideo()
    }

    fun drawFrame() {
        synchronized(this) {
            if (updateSurface) {
                surfaceTexture.updateTexImage()
                surfaceTexture.getTransformMatrix(STMatrix)
                updateSurface = false
            }
        }

        GLES20.glUseProgram(programHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureIDHandle)

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
        val surface = Surface(surfaceTexture)
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(videoPath)
        mediaPlayer?.setSurface(surface)
        mediaPlayer?.isLooping = true
        surface.release()
        try {
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.isLooping = volume1 > 0f
                mediaPlayer?.setVolume(volume1, volume1)
                if(autoPlay)
               playVideo()
                else
                    pauseVideo()

            }
            mediaPlayer?.prepare()
        } catch (e: Exception) {

        }
    }

    fun playVideo() {

        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    fun pauseVideo() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                mediaPlayer?.pause()
            }
        }
    }

    fun getCurrentPosition():Int? = mediaPlayer?.currentPosition

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this) {
            updateSurface = true
        }
    }

   fun onDestroy() {
       mediaPlayer?.release()
   }

    fun onPause() {
        Logger.e("player = $mediaPlayer")
        pauseVideo()
    }

}