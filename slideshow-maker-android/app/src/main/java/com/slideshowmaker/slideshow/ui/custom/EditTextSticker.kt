package com.slideshowmaker.slideshow.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.TextStickerAttrInfo

import com.slideshowmaker.slideshow.utils.DimenUtils
import com.slideshowmaker.slideshow.utils.Logger
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

class EditTextSticker(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    private val density = DimenUtils.density(context)
    private val textSize = 100 * density


    private val dotHeight = density

    private var bitmap1: Bitmap? = null
    private val matrix = Matrix()
    private val floatArray = FloatArray(9)
    private val iconSizeInDp = resources.getDimensionPixelSize(R.dimen.icon_size_small)
    private val resizedBitmap: Bitmap =
        ContextCompat.getDrawable(context, R.drawable.icon_scale_sticker)!!.toBitmap(
            iconSizeInDp,
            iconSizeInDp
        )
    private val deleteBitmap =
        ContextCompat.getDrawable(context, R.drawable.icon_cancel_transform)!!
            .toBitmap(iconSizeInDp, iconSizeInDp)
    private val editTextBitmap =
        ContextCompat.getDrawable(context, R.drawable.icon_common_edit)!!
            .toBitmap(iconSizeInDp, iconSizeInDp)
    private var btnRadius = 12f * density
    private val textLines = ArrayList<String>()
    private var inpuText = ""

    private var hintText = context.getString(R.string.text_editor_hint)

    private var isInEdit = true
    val inEdit get() = isInEdit

    private var isAdd = false
    private val rotateBtnRegion = Region()
    private val deleteBtnRegion = Region()
    private val editBtnRegion = Region()
    private val imgRegion = Region()

    private var alignMode = AlignMode.CENTER

    private var touchPointX = 0f
    private var touchPointY = 0f
    private var isRotateMode = false
    private var isMotion = false
    private var diagonalLength = 0f
    private var middlePointX = 0f
    private var middlePointY = 0f
    private var lastDegrees = 0f

    private var fontResId = R.font.roboto_regular
    private var textStyle1 = Typeface.NORMAL
    private var paintFlag = Paint.ANTI_ALIAS_FLAG
    private var textColor = Color.WHITE

    var deleteListener: (() -> Unit)? = null
    var editListener: ((EditTextSticker) -> Unit)? = null

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = textSize
        color = textColor
        typeface = Typeface.create(ResourcesCompat.getFont(context, fontResId), textStyle1)
    }

    private val dotBoundPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(5f * density, 5f * density), 0f)
        strokeWidth = dotHeight
    }

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        matrix.postScale(0.25f, 0.25f, middlePointX, middlePointY)
        val textHeight = getTextHeight(textPaint) / 4
        val textWidth = getTextWidth(hintText, textPaint) / 4
        val translateXVal = (DimenUtils.screenWidth(context) - textWidth) / 2f
        val translateYVal = 2 * 56 * DimenUtils.density()
        matrix.postTranslate(translateXVal, translateYVal)
        background = null
        calculatorLineText()
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inpuText = s.toString()
                Logger.e("text length = ${s.toString().length}")
                calculatorLineText()
            }

        })
    }

    private fun calculatorLineText() {
        textLines.clear()
        if (inpuText.isEmpty()) {
            drawHint()
            return
        }

        var curLine = ""
        for (char in inpuText) {
            curLine = if (char == '\n') {
                textLines.add(curLine)
                ""
            } else {
                "$curLine$char"
            }
        }
        textLines.add(curLine)
        var maxW = 0
        for (item in textLines) {
            val lineWidth = getTextWidth(item, textPaint)
            if (lineWidth > maxW) maxW = lineWidth.roundToInt()
        }

        val maxHeight = getTextHeight(textPaint) * textLines.size
        val bitmap = Bitmap.createBitmap(maxW + 1, maxHeight.roundToInt() + 10, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        when (alignMode) {
            AlignMode.LEFT -> {
                for (index in 0 until textLines.size) {
                    val currentLine = textLines[index]
                    canvas.drawText(
                        currentLine,
                        0f,
                        getTextHeight(textPaint) * (index + 1) - getTextHeight(textPaint) / 4,
                        textPaint
                    )
                }
            }
            AlignMode.CENTER -> {
                for (index in 0 until textLines.size) {
                    val line = textLines[index]
                    canvas.drawText(
                        line,
                        maxW / 2f - getTextWidth(line, textPaint) / 2,
                        getTextHeight(textPaint) * (index + 1) - getTextHeight(textPaint) / 4,
                        textPaint
                    )
                }
            }
            AlignMode.RIGHT -> {
                for (index in 0 until textLines.size) {
                    val line = textLines[index]
                    canvas.drawText(
                        line,
                        maxW - getTextWidth(line, textPaint),
                        getTextHeight(textPaint) * (index + 1) - getTextHeight(textPaint) / 4,
                        textPaint
                    )
                }
            }
        }
        bitmap1 = bitmap
        invalidate()

    }

    private fun drawHint() {
        val height = getTextHeight(textPaint)
        val width = getTextWidth(hintText, textPaint)
        val bitmap =
            Bitmap.createBitmap(width.roundToInt(), height.roundToInt() + 10, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(
            hintText,
            0f,
            getTextHeight(textPaint) - getTextHeight(textPaint) / 4,
            textPaint
        )
        bitmap1 = bitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        bitmap1?.let {
            val leftTopX = getMatrixTranslateX()
            val leftTopY = getMatrixTranslateY()

            val rightTopX = getMatrixScaleX() * this.bitmap1!!.width + getMatrixTranslateX()
            val rightTopY = getMatrixTranslateY() + getMatrixSkewY() * bitmap1!!.width

            val leftBottomX = getMatrixTranslateX() + getMatrixSkewX() * this.bitmap1!!.height
            val leftBottomY = getMatrixScaleY() * this.bitmap1!!.height + getMatrixTranslateY()

            val rightBottomX =
                getMatrixScaleX() * this.bitmap1!!.width + getMatrixTranslateX() + getMatrixSkewX() * this.bitmap1!!.height
            val rightBottomY =
                getMatrixScaleY() * this.bitmap1!!.height + getMatrixTranslateY() + getMatrixSkewY() * this.bitmap1!!.width

            canvas.save()
            canvas.drawBitmap(bitmap1!!, matrix, null)
            if (isInEdit) {
                val drawPath = Path().apply {
                    moveTo(leftTopX, leftTopY)
                    lineTo(rightTopX, rightTopY)
                    lineTo(rightBottomX, rightBottomY)
                    lineTo(leftBottomX, leftBottomY)
                    lineTo(leftTopX, leftTopY)
                    close()
                }
                getBoundRegion(imgRegion, drawPath)
                canvas.drawPath(drawPath, dotBoundPaint)
                canvas.restore()

                drawRotateButton(canvas, rightBottomX, rightBottomY)
                if (isAdd) {
                    drawDeleteButton(canvas, leftTopX, leftTopY)
                    drawEditStickerButton(canvas, rightTopX, rightTopY)
                }

            }

        }
    }

    fun getOutBitmap(canvas: Canvas) {
        canvas.drawBitmap(bitmap1!!, matrix, null)
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
        canvas?.drawBitmap(resizedBitmap, null, rectF, null)
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
    }

    private fun drawEditStickerButton(canvas: Canvas?, offsetX: Float, offsetY: Float) {
        Path().apply {
            addRect(0f, 0f, btnRadius * 2, btnRadius * 2, Path.Direction.CCW)
            offset(offsetX - btnRadius, offsetY - btnRadius)
            getBoundRegion(editBtnRegion, this)
        }
        val rectF = RectF(
            offsetX - btnRadius,
            offsetY - btnRadius,
            offsetX + btnRadius,
            offsetY + btnRadius
        )
        canvas?.drawBitmap(editTextBitmap, null, rectF, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isInEdit) return false
        requestFocus()
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                touchPointX = event.rawX
                touchPointY = event.rawY
                when {
                    rotateBtnRegion.contains(event.x.toInt(), event.y.toInt()) -> {
                        isRotateMode = true

                        val floatArr = FloatArray(9)
                        matrix.getValues(floatArr)

                        val leftTopX = getMatrixTranslateX()
                        val leftTopY = getMatrixTranslateY()
                        val rightBottomX = event.x
                        val rightBottomY = event.y
                        middlePointX = (leftTopX + rightBottomX) / 2
                        middlePointY = (leftTopY + rightBottomY) / 2
                        diagonalLength =
                            hypot(rightBottomX - middlePointX, rightBottomY - middlePointY)
                        lastDegrees = Math.toDegrees(
                            atan2(
                                (rightBottomY - middlePointY),
                                (rightBottomX - middlePointX)
                            ).toDouble()
                        ).toFloat()
                        return true
                    }
                    deleteBtnRegion.contains(event.x.toInt(), event.y.toInt()) -> {
                        if (!isAdd) return false
                        deleteListener?.invoke()
                    }
                    editBtnRegion.contains(event.x.toInt(), event.y.toInt()) -> {
                        if (!isAdd) return false
                        editListener?.invoke(this)
                    }
                    imgRegion.contains(event.x.toInt(), event.y.toInt()) -> {
                        isMotion = true
                        return true
                    }
                    else -> return false
                }

            }
            MotionEvent.ACTION_MOVE -> {
                if (isRotateMode) {
                    rotate(event.x, event.y)
                } else if (isMotion) {
                    motionView(event.rawX, event.rawY)
                }
                return true

            }
            MotionEvent.ACTION_UP -> {
                isRotateMode = false
                isMotion = false
                return true
            }
        }

        return false
    }

    private fun showKeyboard() {
        requestFocus()
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.toggleSoftInputFromWindow(applicationWindowToken, InputMethodManager.SHOW_FORCED, 0)
    }

    private fun motionView(rawX: Float, rawY: Float) {
        matrix.postTranslate(rawX - touchPointX, rawY - touchPointY)
        touchPointX = rawX
        touchPointY = rawY
        invalidate()
    }

    private fun rotate(rawX: Float, rawY: Float) {
        val toDiagonalLength = hypot((rawX) - middlePointX, (rawY) - middlePointY)
        val scale = toDiagonalLength / diagonalLength
        matrix.postScale(scale, scale, middlePointX, middlePointY)
        diagonalLength = toDiagonalLength

        val toDegrees =
            Math.toDegrees(atan2((rawY - middlePointY), (rawX - middlePointX)).toDouble()).toFloat()

        val degrees = toDegrees - lastDegrees
        matrix.postRotate(degrees, middlePointX, middlePointY)
        lastDegrees = toDegrees
        matrix.getValues(floatArray)

        invalidate()
    }

    fun setInEdit(inEdit: Boolean) {
        isInEdit = inEdit
        invalidate()
    }

    private fun getTextWidth(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat() + 80
    }

    private fun getTextHeight(paint: Paint): Float {
        val text =
            "qwertyuiop[]asdfghjkl;'\\zxcvbnm,./QWERTYUIOP{}ASDFGHJKL:\"|ZXCVBNM<>?1234567890-=!@#$%^&*()_+`~"
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
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

    private fun getBoundRegion(region: Region, path: Path) {
        val rectF = RectF()
        path.computeBounds(rectF, true)
        region.setPath(
            path,
            Region(
                rectF.left.toInt(),
                rectF.top.toInt(),
                rectF.right.toInt(),
                rectF.bottom.toInt()
            )
        )
    }

    fun changeFonts(fontId: Int) {
        if (fontResId == fontId) return
        fontResId = fontId
        textPaint.typeface = Typeface.create(ResourcesCompat.getFont(context, fontResId), textStyle1)
        calculatorLineText()
    }

    fun changeAlign(align: AlignMode) {
        if (alignMode == align) return
        alignMode = align
        calculatorLineText()
    }

    fun changeTextStyle(textStyle: Int) {
        if (textStyle1 == textStyle) return
        textStyle1 = textStyle
        textPaint.typeface = Typeface.create(ResourcesCompat.getFont(context, fontResId), textStyle1)
        calculatorLineText()
    }

    fun changeTextFlag(flag: Int) {
        paintFlag = if (paintFlag == flag) {
            Paint.ANTI_ALIAS_FLAG
        } else {
            flag
        }

        textPaint.apply {
            isAntiAlias = true
            flags = paintFlag
        }
        calculatorLineText()
    }

    fun changeColor(color: Int) {
        if (textColor != color) {
            textColor = color
            textPaint.color = textColor
            calculatorLineText()
        }
    }

    fun changeIsAdded(isAdded: Boolean) {
        isAdd = isAdded
        invalidate()
    }

    fun getMainText(): String {
        return inpuText
    }

    enum class AlignMode {
        LEFT, CENTER, RIGHT
    }

    fun getTextAttrData(): TextStickerAttrInfo {
        return TextStickerAttrInfo(inpuText, textColor, fontResId, alignMode, textStyle1, paintFlag)
    }

    fun setAttr(textStickerAttrData: TextStickerAttrInfo) {
        textStickerAttrData.apply {
            setText("")
            append(text)
            inpuText = text
            changeColor(textColor)
            changeAlign(alignMode)
            changeTextStyle(textStyle)
            changeFonts(fontId)
            changeTextFlag(flag)
        }

    }

}