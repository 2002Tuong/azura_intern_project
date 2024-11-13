package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlin.math.atan2
import kotlin.math.hypot

class StickerView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {

    private val matrix = Matrix()
    private var bitmap: Bitmap? = null
    private val paint = Paint()
    private val bluePaint = Paint()
    private val floatArray = FloatArray(9)

    private val dotBoundPaint = Paint()
    private var dotHeight = 1f

    private var density = 1f
    private var btnRadius = 12f

    private val btnPaint = Paint()

    private val rotateBtnRegion = Region()
    private val deleteBtnRegion = Region()
    private val imageRegion = Region()

    private var onEditting = false
    val inEdit get() = onEditting

    private val scaleBitmap: Bitmap =
        AppCompatResources.getDrawable(context, R.drawable.icon_scale_sticker)!!.toBitmap()
    private val deleteBitmap =
        AppCompatResources.getDrawable(context, R.drawable.icon_cancel_transform)!!.toBitmap()

    var deleteHandleCallback: (() -> Unit)? = null

    init {
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        density = DimenUtils.density(context)
        dotHeight *= density
        btnRadius *= density
        paint.apply {
            isAntiAlias = true
            color = Color.BLACK

        }

        bluePaint.apply {
            isAntiAlias = true
            color = Color.BLUE
        }

        dotBoundPaint.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(5f * density, 5f * density), 0f)
            strokeWidth = dotHeight
        }

        btnPaint.apply {
            color = Color.GREEN
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }


    override fun onDraw(canvas: Canvas) {
        bitmap?.let {
            val arrayOfFloat = FloatArray(9)
            matrix.getValues(arrayOfFloat)

            val leftTopX = getMatrixTranslateX()
            val leftTopY = getMatrixTranslateY()

            val rightTopX = getMatrixScaleX() * this.bitmap!!.width + getMatrixTranslateX()
            val rightTopY = getMatrixTranslateY() + getMatrixSkewY() * bitmap!!.width

            val leftBottomX = getMatrixTranslateX() + getMatrixSkewX() * this.bitmap!!.height
            val leftBottomY = getMatrixScaleY() * this.bitmap!!.height + getMatrixTranslateY()

            val rightBottomX =
                getMatrixScaleX() * this.bitmap!!.width + getMatrixTranslateX() + getMatrixSkewX() * this.bitmap!!.height
            val rightBottomY =
                getMatrixScaleY() * this.bitmap!!.height + getMatrixTranslateY() + getMatrixSkewY() * this.bitmap!!.width

            canvas.save()
            canvas.drawBitmap(bitmap!!, matrix, null)
            if (onEditting) {
                val path = Path().apply {
                    moveTo(leftTopX, leftTopY)
                    lineTo(rightTopX, rightTopY)
                    lineTo(rightBottomX, rightBottomY)
                    lineTo(leftBottomX, leftBottomY)
                    lineTo(leftTopX, leftTopY)
                    close()
                }
                getBoundRegion(imageRegion, path)
                canvas.drawPath(path, dotBoundPaint)
                canvas.restore()

                drawRotateButton(canvas, rightBottomX, rightBottomY)
                drawDeleteButton(canvas, leftTopX, leftTopY)
            }

        }

    }

    fun getOutBitmap(canvas: Canvas) {
        canvas.drawBitmap(bitmap!!, matrix, null)
    }

    private fun drawBoundPath(canvas: Canvas, path: Path) {

    }

    private fun drawRotateButton(canvas: Canvas?, offsetX: Float, offsetY: Float) {
        Path().apply {
            addRect(0f, 0f, btnRadius * 2, btnRadius * 2, Path.Direction.CCW)
            offset(offsetX - btnRadius, offsetY - btnRadius)
            getBoundRegion(rotateBtnRegion, this)
        }
        val rectF = RectF(
            offsetX - btnRadius,
            offsetY - btnRadius,
            offsetX + btnRadius,
            offsetY + btnRadius
        )
        canvas?.drawBitmap(scaleBitmap, null, rectF, null)
        // canvas?.drawPath(path, mButtonPaint)
    }

    private fun drawDeleteButton(canvas: Canvas?, offsetX: Float, offsetY: Float) {
        Path().apply {
            addRect(0f, 0f, btnRadius * 2, btnRadius * 2, Path.Direction.CCW)
            offset(offsetX - btnRadius, offsetY - btnRadius)
            getBoundRegion(deleteBtnRegion, this)
        }
        val rectF = RectF(
            offsetX - btnRadius,
            offsetY - btnRadius,
            offsetX + btnRadius,
            offsetY + btnRadius
        )
        canvas?.drawBitmap(deleteBitmap, null, rectF, null)
        // canvas?.drawPath(path, mButtonPaint)
    }

    fun setBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            matrix.postTranslate(100f, 100f)
            this.bitmap = it
            requestLayout()
        }

    }

    fun setBitmap(bitmap: Bitmap?, inEdit: Boolean) {
        bitmap?.let {
            matrix.postTranslate(100f, 100f)
            this.bitmap = it
            requestLayout()
            setInEdit(inEdit)
        }

    }

    fun setBitmap(bitmap: Bitmap?, inEdit: Boolean, parentWidth: Int, parentHeight: Int) {
        bitmap?.let {
            var limitedX = parentWidth - bitmap.width
            if (limitedX <= 0) limitedX = 1
            var limitedY = parentWidth - bitmap.height
            if (limitedY <= 0) limitedY = 1
            matrix.postTranslate((0..limitedX).random().toFloat(), (0..limitedY).random().toFloat())
            this.bitmap = it
            requestLayout()
            setInEdit(inEdit)
        }

    }

    private fun getBoundRegion(region: Region, path: Path) {
        val boundRecF = RectF()
        path.computeBounds(boundRecF, true)
        region.setPath(
            path,
            Region(
                boundRecF.left.toInt(),
                boundRecF.top.toInt(),
                boundRecF.right.toInt(),
                boundRecF.bottom.toInt()
            )
        )
    }


    private var touchedPointX = 0f
    private var touchedPointY = 0f
    private var isInRotateMode = false
    private var isOnMotion = false
    var diagonalLength = 0f
    var middlePointX = 0f
    var middleointY = 0f
    var lastDegrees = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!onEditting) return false
        if (event?.action == MotionEvent.ACTION_DOWN) {
            touchedPointX = event.rawX
            touchedPointY = event.rawY
            if (rotateBtnRegion.contains(event.x.toInt(), event.y.toInt())) {
                isInRotateMode = true

                val arrayOfFloat = FloatArray(9)
                matrix.getValues(arrayOfFloat)

                val leftTopX = getMatrixTranslateX()
                val leftTopY = getMatrixTranslateY()
                val rightBottomX = event.x
                val rightBottomY = event.y
                middlePointX = (leftTopX + rightBottomX) / 2
                middleointY = (leftTopY + rightBottomY) / 2
                diagonalLength = hypot(rightBottomX - middlePointX, rightBottomY - middleointY)
                lastDegrees = Math.toDegrees(
                    atan2(
                        (rightBottomY - middleointY),
                        (rightBottomX - middlePointX)
                    ).toDouble()
                ).toFloat()
                return true
            } else if (deleteBtnRegion.contains(event.x.toInt(), event.y.toInt())) {
                deleteHandleCallback?.invoke()
            } else if (imageRegion.contains(event.x.toInt(), event.y.toInt())) {
                isOnMotion = true
                return true
            } else return false

        } else if (event?.action == MotionEvent.ACTION_MOVE) {
            if (isInRotateMode) {
                rotate(event.x, event.y)
            } else if (isOnMotion) {
                motionView(event.rawX, event.rawY)
            }
            return true

        } else if (event?.action == MotionEvent.ACTION_UP) {
            isInRotateMode = false
            isOnMotion = false
            return true
        }

        return false
    }

    private fun motionView(rawX: Float, rawY: Float) {
        matrix.postTranslate(rawX - touchedPointX, rawY - touchedPointY)
        touchedPointX = rawX
        touchedPointY = rawY
        invalidate()
    }

    private fun rotate(rawX: Float, rawY: Float) {
        val toDiagonalLength = hypot((rawX) - middlePointX, (rawY) - middleointY)
        val scale = toDiagonalLength / diagonalLength
        matrix.postScale(scale, scale, middlePointX, middleointY)
        diagonalLength = toDiagonalLength

        val toDegrees =
            Math.toDegrees(atan2((rawY - middleointY), (rawX - middlePointX)).toDouble()).toFloat()

        val degrees = toDegrees - lastDegrees
        matrix.postRotate(degrees, middlePointX, middleointY)
        lastDegrees = toDegrees
        matrix.getValues(floatArray)

        invalidate()
    }

    fun setInEdit(inEdit: Boolean) {
        onEditting = inEdit
        invalidate()
    }

    private fun getMatrixTranslateX(): Float {
        matrix.getValues(floatArray)
        return floatArray[Matrix.MTRANS_X]
    }

    private fun getMatrixTranslateY(): Float {
        matrix.getValues(floatArray)
        return floatArray[Matrix.MTRANS_Y]
    }

    private fun getMatrixScaleX(): Float {
        matrix.getValues(floatArray)
        return floatArray[Matrix.MSCALE_X]
    }

    private fun getMatrixScaleY(): Float {
        matrix.getValues(floatArray)
        return floatArray[Matrix.MSCALE_Y]
    }

    private fun getMatrixSkewX(): Float {
        matrix.getValues(floatArray)
        return floatArray[Matrix.MSKEW_X]
    }

    private fun getMatrixSkewY(): Float {
        matrix.getValues(floatArray)
        return floatArray[Matrix.MSKEW_Y]
    }

    fun getBitmap(): Bitmap? = bitmap
}