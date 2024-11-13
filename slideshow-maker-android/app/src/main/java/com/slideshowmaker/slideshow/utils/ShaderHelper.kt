package com.slideshowmaker.slideshow.utils
import android.opengl.GLES20
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.slideshowmaker.slideshow.data.exceptions.ShaderCompileException
import com.slideshowmaker.slideshow.data.exceptions.ShaderException
import timber.log.Timber

object ShaderHelper {

    private val TAG = "ShaderHelper"

    fun compileShader(shaderType: Int, shaderSource: String): Int {
        var shaderHandle = GLES20.glCreateShader(shaderType)

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource)

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle))
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }

        if (shaderHandle == 0) {
            FirebaseCrashlytics.getInstance().recordException(ShaderException("Error creating shader $shaderType $shaderSource"))
        }

        return shaderHandle
    }

    fun createAndLinkProgram(
        vertexShaderHandle: Int,
        fragmentShaderHandle: Int,
        attributes: Array<String>?
    ): Int {
        var programHandle = GLES20.glCreateProgram()

        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShaderHandle)

            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            if (attributes != null) {
                val size = attributes.size
                for (i in 0 until size) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i])
                }
            }

            GLES20.glLinkProgram(programHandle)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            if (linkStatus[0] == 0) {
                val infoLog = GLES20.glGetProgramInfoLog(programHandle)
                Timber.e("ShaderLinking Program link error: $infoLog")
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }

        if (programHandle == 0) {
            FirebaseCrashlytics.getInstance().recordException(ShaderCompileException("Error compiling program ${GLES20.glGetProgramInfoLog(programHandle)}"))
            ShaderCompileException("Error compiling program ${GLES20.glGetProgramInfoLog(programHandle)}").printStackTrace()
//            throw RuntimeException("Error creating program.")
        }

        return programHandle
    }

}