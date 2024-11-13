package com.slideshowmaker.slideshow.ui.custom.player

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES20
import android.opengl.Matrix
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VideoPlayDrawer2(var videoPath:String)  : SurfaceTexture.OnFrameAvailableListener {



    private val FLOAT_SIZE_BYTES = 4
    private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_SIZE_BYTES
    private val TEXTURE_VERTICES_DATA_STRIDE_BYTES = 2 * FLOAT_SIZE_BYTES
    private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 0

    private val triangleVerticesArray = floatArrayOf(
        -1.0f, -1.0f, 0f, 1.0f,
        -1.0f, 0f, -1.0f, 1.0f, 0f, 1.0f, 1.0f, 0f
    )

    private val textureVerticesDataArray = floatArrayOf(
        0f, 0.0f, 1.0f, 0f,
        0.0f, 1f, 1.0f, 1.0f
    )
    private var triangleVerticesBuff: FloatBuffer
    private var textureVerticesBuff: FloatBuffer
    private val GL_TEXTURE_EXTERNAL_OES = 0x8D65

    private val MVPMatrix = FloatArray(16)
    private val STMatrix = FloatArray(16)

    private var programHandle = 0
    private var textureIDHandle = 0
    val textureId get() = textureIDHandle

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

        triangleVerticesBuff = ByteBuffer
            .allocateDirect(
                triangleVerticesArray.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleVerticesBuff.put(triangleVerticesArray).position(0)

        textureVerticesBuff = ByteBuffer
            .allocateDirect(
                textureVerticesDataArray.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVerticesBuff.put(textureVerticesDataArray).position(0)

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

        triangleVerticesBuff.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            triangleVerticesBuff
        )
        GLES20.glEnableVertexAttribArray(maPositionHandle)

        textureVerticesBuff.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            maTextureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            TEXTURE_VERTICES_DATA_STRIDE_BYTES,
            textureVerticesBuff
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

    fun playVideo() {

        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()

            }
        }
    }

    fun pauseVideo() {
        Logger.e("player = $mediaPlayer")
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