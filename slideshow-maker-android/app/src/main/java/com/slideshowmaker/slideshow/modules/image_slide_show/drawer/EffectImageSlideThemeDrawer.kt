package com.slideshowmaker.slideshow.modules.image_slide_show.drawer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.widget.ImageView.ScaleType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class EffectImageSlideThemeDrawer(
    private val effectPathList: List<String>,
    private val effectScaleType: Int,
    private val outputVideoWidthValue: Int,
    private val outputVideoHeightValue: Int
) {
    private var index = 0

    private val bitmapList = effectPathList.map {
        BitmapFactory.decodeFile(
            it,
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
        )
    }

    private var triangleVertices =
        ByteBuffer.allocateDirect(mTriangleVerticesData.size * FLOAT_SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(mTriangleVerticesData)
                position(0)
            }
        }

    private val MVPMatrix = FloatArray(16)
    private val STMatrix = FloatArray(16)

    /** Canvas Bitmap。Use Canvas contents as OpenGL texture */
    private val canvasBitmap by lazy {
        Bitmap.createBitmap(
            outputVideoWidthValue,
            outputVideoHeightValue,
            Bitmap.Config.ARGB_8888
        )
    }

    /** Canvas. This goes to the encoder */
    private val canvas by lazy { Canvas(canvasBitmap) }

    // Handle
    private var mProgram = 0
    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0
    private var maPositionHandle = 0
    private var maTextureHandle = 0
    private var uCanvasTextureHandle = 0

    /** OpenGL texture id passing the canvas image */
    private var canvasTextureID = -1

    init {
        Matrix.setIdentityM(STMatrix, 0)
        //Reset because we don't need to adjust the aspect ratio (because the canvas is made according to the encoder's output size)
        Matrix.setIdentityM(MVPMatrix, 0)
        surfaceCreated()
    }

    /**
     * Draw to Canvas and draw to OpenGL
     *
     * @param onCanvasDrawRequest Canvas is passed, so please draw and return
     */
    @OptIn(ExperimentalContracts::class)
    private inline fun drawCanvas(onCanvasDrawRequest: (Canvas) -> Unit) {
        contract {
            callsInPlace(onCanvasDrawRequest, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
        }
        prepareDraw()
        drawCanvasInternal(onCanvasDrawRequest)
        invokeGlFinish()
    }


    /** call before drawing */
    private fun prepareDraw() {
        // can cause glError 1282
        GLES20.glUseProgram(mProgram)
        checkGlError("glUseProgram")
        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            triangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(maPositionHandle)
        checkGlError("glEnableVertexAttribArray maPositionHandle")
        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            maTextureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            triangleVertices
        )
        checkGlError("glVertexAttribPointer maTextureHandle")
        GLES20.glEnableVertexAttribArray(maTextureHandle)
        checkGlError("glEnableVertexAttribArray maTextureHandle")

// With Snapdragon, the image is distorted without glClear
// GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
    }

    /**
     * Write to Canvas and draw with OpenGL.
     * Must be called after [drawFrame].
     *
     * @param onCanvasDrawRequest Canvas is passed, so please draw and return
     */
    private inline fun drawCanvasInternal(onCanvasDrawRequest: (Canvas) -> Unit) {
        checkGlError("drawCanvas start")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, canvasTextureID)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        onCanvasDrawRequest(canvas)
        // glActiveTexture
        //  texSubImage2D
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, canvasBitmap)
        checkGlError("GLUtils.texSubImage2D canvasTextureID")
        // Uniform
        // GLES20.GL_TEXTURE0
        GLES20.glUniform1i(uCanvasTextureHandle, 0)
        checkGlError("glUniform1i uCanvasTextureHandle")
        //Reset because we don't need to adjust the aspect ratio (because the canvas is made according to the encoder's output size)
//    Matrix.setIdentityM(mMVPMatrix, 0)
        // Apart from that, OpenGL images have the bottom left origin (usually the top left), so flip the matrix
        // sorry i don't understand
        Matrix.setIdentityM(STMatrix, 0)
        Matrix.scaleM(STMatrix, 0, 1f, -1f, 1f)

        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, STMatrix, 0)
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays Canvas")
    }

    /** call glFinish */
    private fun invokeGlFinish() {
        GLES20.glFinish()
    }

    private fun surfaceCreated() {
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (mProgram == 0) {
            throw RuntimeException("failed creating program")
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (maTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (muMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix")
        checkGlError("glGetUniformLocation uSTMatrix")
        if (muSTMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uSTMatrix")
        }
        uCanvasTextureHandle = GLES20.glGetUniformLocation(mProgram, "uCanvasTexture")

        // Get Canvas texture ID issued
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)

        canvasTextureID = textures[0]
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, canvasTextureID)
        checkGlError("glBindTexture canvasTextureID")

        // Interpolation settings when scaling
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // initialize texture
        // Use texSubImage2D after switching context when updating
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, canvasBitmap, 0)
        checkGlError("glTexParameter canvasTextureID")
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        checkGlError("glCreateShader type=$shaderType")
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            return 0
        }
        GLES20.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES20.glAttachShader(program, pixelShader)
        checkGlError("glAttachShader")
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkGlError(op: String) {
//    val error = GLES20.glGetError()
//    if (error != GLES20.GL_NO_ERROR) {
//      throw RuntimeException("$op: glError $error")
//    }
    }

    fun drawFrame(time: Int) {
        val currentBitmap = bitmapList[index]
        index = (index + 1) % bitmapList.size

        drawCanvas {
            val (src, dest) = getDesRect(currentBitmap, effectScaleType, it)

            it.drawBitmap(
                /* bitmap = */ currentBitmap,
                /* src = */ src,
                /* dst = */ dest,
                /* paint = */ null,
            )
        }
    }

    private fun getDesRect(
        currentBitmap: Bitmap,
        scaleType: Int,
        canvas: Canvas
    ): Pair<Rect, Rect> {
        return when (scaleType) {
            ScaleType.FIT_XY.ordinal, ScaleType.CENTER_INSIDE.ordinal -> {
                val src = Rect(0, 0, currentBitmap.width - 1, currentBitmap.height - 1)
                val ratio = src.width().toFloat() / src.height()
                val dest = when (scaleType) {
                    ScaleType.FIT_XY.ordinal -> Rect(0, 0, canvas.width - 1, canvas.height - 1)
                    else -> getRect(canvas.width - 1, canvas.height - 1, ratio)
                }
                Pair(src, dest)
            }

            else -> {
                val dest = Rect(0, 0, canvas.width - 1, canvas.height - 1)
                val ratio = dest.width().toFloat() / dest.height()
                val src = getRect(currentBitmap.width - 1, currentBitmap.height - 1, ratio)
                Pair(src, dest)
            }
        }
    }

    private fun getRect(width: Int, height: Int, ratio: Float): Rect {
        return if (width.toFloat() / height > ratio) {
            val desWidth = (height * ratio).toInt()
            val left = (width - desWidth) / 2
            val right = left + desWidth
            Rect(left, 0, right, height)
        } else {
            val desHeight = (width / ratio).toInt()
            val top = (height - desHeight) / 2
            val bottom = top + desHeight
            Rect(0, top, width, bottom)
        }
    }

    fun release() = bitmapList.forEach { kotlin.runCatching { it.recycle() } }

    companion object {

        private val mTriangleVerticesData = floatArrayOf(
            -1.0f, -1.0f, 0f, 0f, 0f,
            1.0f, -1.0f, 0f, 1f, 0f,
            -1.0f, 1.0f, 0f, 0f, 1f,
            1.0f, 1.0f, 0f, 1f, 1f
        )

        private const val FLOAT_SIZE_BYTES = 4
        private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
        private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
        private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

        private const val VERTEX_SHADER = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uSTMatrix;
            attribute vec4 aPosition;
            attribute vec4 aTextureCoord;
            varying vec2 vTextureCoord;
            
            void main() {
              gl_Position = uMVPMatrix * aPosition;
              vTextureCoord = (uSTMatrix * aTextureCoord).xy;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require

            precision mediump float;
            varying vec2 vTextureCoord;
            uniform sampler2D uCanvasTexture;
        
            void main() {
                gl_FragColor = texture2D(uCanvasTexture, vTextureCoord);
            }
        """
    }
}

fun Bitmap.resized(newWidth: Int, newHeight: Int): Bitmap {
    val width = this.width
    val height = this.height

    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height

    // CREATE A MATRIX FOR THE MANIPULATION
    val matrix = android.graphics.Matrix()

    // RESIZE THE BIT MAP
    matrix.postScale(scaleWidth, scaleHeight)

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false).also {
        recycle()
    }
}