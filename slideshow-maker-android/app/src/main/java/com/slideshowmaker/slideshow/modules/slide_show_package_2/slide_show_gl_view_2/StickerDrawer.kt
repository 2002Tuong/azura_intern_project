package com.slideshowmaker.slideshow.modules.slide_show_package_2.slide_show_gl_view_2

import android.graphics.Bitmap
import android.opengl.GLES20
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.ShaderHelper
import com.slideshowmaker.slideshow.utils.TextureHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class StickerDrawer() {
/*    private var mVertices = floatArrayOf(
        -0.5f, 0.5f, 0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f, 0.5f, 0.0f
    )*/

    private var vertices = floatArrayOf(
        -1f, 1f, 0f,
        -1f, -1f, 0.0f,
        1f, 1f, 0.0f,
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        1f, 1f, 0.0f
    )

    private var textureCoordinateData = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    private val vertexBuffer: FloatBuffer
    private val textureCoordinateBuffer: FloatBuffer

    private var programHandle = 0

    private var tex_1_Handle = 0
    private var textureUniformHandle = 0
    private var positionHandle = 0
    private var textureCoordinateHandle = 0

    init {

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        textureCoordinateBuffer = ByteBuffer.allocateDirect(textureCoordinateData.size * 4).order(
            ByteOrder.nativeOrder()).asFloatBuffer()
        textureCoordinateBuffer.put(textureCoordinateData).position(0)
        // texture = Texture(context)

    }

    fun prepare(bitmap: Bitmap) {
        //texture
        val vertexShader = getVertexShader()
        val fragmentShader = getFragmentShader()

        val vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle, fragmentShaderHandle,
            arrayOf("a_Position", "a_TexCoordinate")
        )
        tex_1_Handle = TextureHelper.loadTexture(bitmap)
    }

    fun drawFrame() {

        GLES20.glUseProgram(programHandle)

        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate")
        textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture")


        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)


        textureCoordinateBuffer.position(0)
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer)
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex_1_Handle)
        GLES20.glUniform1i(textureUniformHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size/3)
        GLES20.glFinish()
    }
    fun drawFrame(viewPortW:Int, viewPortH:Int, viewPortX:Int, viewPortY:Int) {

        GLES20.glUseProgram(programHandle)
        GLES20.glViewport(viewPortX, viewPortY, viewPortW, viewPortH)
        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate")
        textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture")


        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)


        textureCoordinateBuffer.position(0)
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer)
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex_1_Handle)
        GLES20.glUniform1i(textureUniformHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size/3)
        GLES20.glFinish()
    }
    protected fun getVertexShader(): String {
        return RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            R.raw.filter_frame_vertex_shader
        )
    }

    protected fun getFragmentShader(): String {
        return RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            R.raw.fillter_frame_fragment_shader
        )
    }
}