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

class FrameAndFilterSlideShowDrawer {
    private val vertices = floatArrayOf(
        -1f, 1f, 0f,
        -1f, -1f, 0.0f,
        1f, 1f, 0.0f,
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        1f, 1f, 0.0f
    )

    private val textureCoordinateData = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    private val textureCoordinateBuffer: FloatBuffer

    private var programHandle = 0
    private var texture1Handle = 0
    private var textureUniformHandle = 0
    private var positionHandle = 0
    private var textureCoordinateHandle = 0

    init {
        vertexBuffer.put(vertices).position(0)

        textureCoordinateBuffer = ByteBuffer
            .allocateDirect(textureCoordinateData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureCoordinateBuffer.put(textureCoordinateData).position(0)
    }

    /**
     * Prepare the program in the OpenGL thread.
     */
    fun prepare(bitmap: Bitmap) {
        val vertexShader = getVertexShader()
        val fragmentShader = getFragmentShader()

        val vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        programHandle = ShaderHelper.createAndLinkProgram(
            vertexShaderHandle, fragmentShaderHandle,
            arrayOf("a_Position", "a_TexCoordinate")
        )
        texture1Handle = TextureHelper.loadTexture(bitmap)
    }

    /**
     * Draw the frame
     */
    fun drawFrame() {
        // glUseProgram: Installs a program object as part of current rendering state
        GLES20.glUseProgram(programHandle)

        // Get the attribute and uniform locations
        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        // Get the attribute and uniform locations
        textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate")
        // Get the attribute and uniform locations
        textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture")

        // mVertexBuffer: (x, y, z) coordinates for the vertices
        vertexBuffer.position(0)
        // glVertexAttribPointer: Define an array of generic vertex attribute data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        // glEnableVertexAttribArray: Enable or disable a generic vertex attribute array
        GLES20.glEnableVertexAttribArray(positionHandle)

        // mTextureCoordinateBuffer: (u, v) coordinates for the texture
        textureCoordinateBuffer.position(0)
        // glVertexAttribPointer: Define an array of generic vertex attribute data
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer)
        // glEnableVertexAttribArray: Enable or disable a generic vertex attribute array
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)

        // glActiveTexture: Select active texture unit
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // glBindTexture: Bind a named texture to a texturing target
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1Handle)
        // glUniform1i: Set the value of a uniform variable for the current program object
        GLES20.glUniform1i(textureUniformHandle, 0)

        // glDrawArrays: Render primitives from array data
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)
        // glFinish: Block until all GL execution is complete
        GLES20.glFinish()
    }

    private fun getVertexShader(): String {
        return RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            R.raw.filter_frame_vertex_shader
        )
    }

    private fun getFragmentShader(): String {
        return RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            R.raw.fillter_frame_fragment_shader
        )
    }
}
