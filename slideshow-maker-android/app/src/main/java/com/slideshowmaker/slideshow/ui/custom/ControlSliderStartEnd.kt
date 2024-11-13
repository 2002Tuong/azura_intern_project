package com.slideshowmaker.slideshow.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.Utils
import kotlin.math.roundToInt

class ControlSliderStartEnd : View {

    private var density = 0f

    private var progressLineHeight = 2f

    private val disablePaintObj = Paint()
    private val highlightPaintObj = Paint()
    private val textPaintObj = Paint()

    private var managerHeight = 20f
    private var managerWidth = 14f
    private var lineInManagerWidth = 7.5f
    private var lineInManagerHeight = 1.25f

    private var managerStartDx = 100f
    private var managerEndDx = 100f

    private var startProgressValue = 0f
    private var endProgressValue = 100f

    private var progressLineOffset = 0f
    private var progressLineDistance = 0f

    private var changeLeft = false
    private var changeRight = false

    private val controllerStartRegion = Region()
    private val controllerEndRegion = Region()

    private var maximum = 0

    private var textSize = 12f

    private var onChangeListener: OnChangeListener? = null

    private var tenSecWidth = 0f

    private val mMinimumTimeMilSec = 10000
    private val deltaRegion = 150
    private var startOffsetInX = 0f
    private var mEndOffsetInX = 0f
    private var textDurationHeight = 0f
    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs: AttributeSet?) {

        density = DimenUtils.density(context)
        progressLineHeight *= density
        managerHeight *= density
        managerWidth *= density
        lineInManagerWidth *= density
        lineInManagerHeight *= density
        textSize *= density

        progressLineOffset = managerWidth / 2f

        disablePaintObj.apply {
            color = Color.parseColor("#bebebe")
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        highlightPaintObj.apply {
            color = Color.parseColor("#FB7609")
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        textPaintObj.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = textSize
            typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        }
        textDurationHeight = Utils.getTextHeight("00:00",textPaintObj)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            getMeasuredDimensionHeight(heightMode, heightMeasureSpec)
        )

        managerStartDx = 0f
        managerEndDx = MeasureSpec.getSize(widthMeasureSpec).toFloat() - managerWidth
        progressLineDistance = MeasureSpec.getSize(widthMeasureSpec).toFloat() - managerWidth
    }

    private fun getMeasuredDimensionHeight(mode: Int, height: Int): Int {
        return when (mode) {
            MeasureSpec.AT_MOST -> {
                (managerHeight * 2 + 6).roundToInt()
            }
            else -> {
                height
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawDisableLine(canvas)
        drawHighlightLine(canvas)
        drawControllerStart(canvas)
        drawControllerEnd(canvas)
        drawTextDuration(canvas)
    }

    private fun drawDisableLine(canvas: Canvas?) {
        progressLineDistance = width - managerWidth
        val rightOffset = progressLineOffset
        canvas?.drawRect(
            0 + rightOffset,
            height / 2f,
            0 + rightOffset + progressLineDistance,
            height / 2f + progressLineHeight,
            disablePaintObj
        )
    }

    private fun drawHighlightLine(canvas: Canvas?) {
        val left = startProgressValue * progressLineDistance / 100 + progressLineOffset
        val right = endProgressValue * progressLineDistance / 100 + progressLineOffset
        canvas?.drawRect(
            0 + left,
            height / 2f,
            right,
            height / 2f + progressLineHeight,
            highlightPaintObj
        )
    }


    private fun drawControllerStart(canvas: Canvas?) {
        val controlPath = getControllerPath()
        controlPath.apply {
            val offsetX = (startProgressValue * progressLineDistance / 100)
            startOffsetInX = offsetX+x
            offset(offsetX, (height / 2f).roundToInt().toFloat())
            val boundRecF = RectF()
            computeBounds(boundRecF, true)
            controllerStartRegion.setPath(
                this,
                Region(
                    boundRecF.left.toInt(),
                    boundRecF.top.toInt(),
                    boundRecF.right.toInt(),
                    boundRecF.bottom.toInt()
                )
            )
        }
        canvas?.drawPath(controlPath, highlightPaintObj)
    }


    private fun drawControllerEnd(canvas: Canvas?) {
        val controlPath = getControllerPath()
        controlPath.apply {
            val offsetX = (endProgressValue * progressLineDistance / 100)
            mEndOffsetInX = offsetX+x
            offset(offsetX, (height / 2f).roundToInt().toFloat())
            val boundRecF = RectF()
            computeBounds(boundRecF, true)
            controllerEndRegion.setPath(
                this,
                Region(
                    boundRecF.left.toInt(),
                    boundRecF.top.toInt(),
                    boundRecF.right.toInt(),
                    boundRecF.bottom.toInt()
                )
            )
        }

        canvas?.drawPath(controlPath, highlightPaintObj)
    }

    private fun getControllerPath(): Path {
        val controlPath = Path()
        val centerWid = managerWidth / 2f
        val triangleHei = 7f * DimenUtils.density(context)
        val cornerRad = 2.5f * density
        val rectF = RectF()
        controlPath.apply {
            moveTo(centerWid, 0f)
            lineTo(managerWidth, triangleHei)
            lineTo(managerWidth, managerHeight - cornerRad)
            lineTo(0f, managerHeight - cornerRad)
            lineTo(0f, triangleHei)
            lineTo(centerWid, 0f)
            moveTo(0f, managerHeight - cornerRad)

            rectF.set(0f, managerHeight - 2 * cornerRad, 2 * cornerRad, managerHeight)
            arcTo(rectF, 180f, -90f, false)

            lineTo(managerWidth - cornerRad, managerHeight)

            rectF.set(
                managerWidth - 2 * cornerRad,
                managerHeight - 2 * cornerRad,
                managerWidth,
                managerHeight
            )
            arcTo(rectF, 90f, -90f, false)

            close()
        }
        val deltaX = DimenUtils.density(context) * 6.5f / 2f
        val deltaY = DimenUtils.density(context) * (14f + 5.75f) / 2f
        controlPath.addPath(getThreeLinePath(), deltaX, deltaY)
        controlPath.fillType = Path.FillType.EVEN_ODD
        return controlPath
    }

    private fun getThreeLinePath(): Path {
        val space = DimenUtils.density(context) * 3.5f / 2f

        return Path().apply {
            fillType = Path.FillType.INVERSE_EVEN_ODD
            addPath(getLinePath(), 0f, 0f)
            addPath(getLinePath(), 0f, space + lineInManagerHeight)
            addPath(getLinePath(), 0f, 2 * (space + lineInManagerHeight))
            close()
        }
    }

    private fun getLinePath(): Path {
        val rectF = RectF()
        return Path().apply {
            fillType = Path.FillType.INVERSE_EVEN_ODD
            val cornerRad = lineInManagerHeight / 2f
            moveTo(cornerRad, 0f)
            lineTo(lineInManagerWidth - cornerRad, 0f)
            lineTo(lineInManagerWidth - cornerRad, lineInManagerHeight)
            lineTo(cornerRad, lineInManagerHeight)
            lineTo(cornerRad, 0f)
            moveTo(cornerRad, 0f)

            rectF.set(0f, 0f, lineInManagerHeight, lineInManagerHeight)
            arcTo(rectF, 90f, 180f, false)

            moveTo(lineInManagerWidth - cornerRad, 0f)

            rectF.set(
                lineInManagerWidth - lineInManagerHeight,
                0f,
                lineInManagerWidth,
                lineInManagerHeight
            )
            arcTo(rectF, 90f, -180f, false)

            close()
        }
    }

    private fun drawTextDuration(canvas: Canvas?) {
        val textStart = Utils.convertSecToTimeString((startProgressValue/100/1000*maximum).roundToInt())
        val textEnd = Utils.convertSecToTimeString((endProgressValue/100/1000*maximum).roundToInt())
        canvas?.drawText(textStart, 0f, textDurationHeight*1.5f, textPaintObj)
        canvas?.drawText(
            textEnd,
            width - 10 - (getTextWidth(textEnd, textPaintObj)),
            textDurationHeight*1.5f,
            textPaintObj
        )

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

            tenSecWidth = (10000 * progressLineDistance / maximum)

        if (event?.action == MotionEvent.ACTION_DOWN) {



            if (controllerStartRegion.contains(event.x.toInt(), event.y.toInt()) || event.rawX in startOffsetInX-deltaRegion..startOffsetInX+deltaRegion) {
                changeLeft = true
                changeRight = false
            } else if (controllerEndRegion.contains(event.x.toInt(), event.y.toInt())  || event.rawX in mEndOffsetInX-deltaRegion..mEndOffsetInX+deltaRegion) {
                changeLeft = false
                changeRight = true
            } else {
                changeLeft = false
                changeRight = false
            }
        } else if (event?.action == MotionEvent.ACTION_UP) {
            if (changeLeft) {
                onChangeListener?.onLeftUp(managerStartDx / progressLineDistance)
            } else if (changeRight) {
                onChangeListener?.onRightUp(managerEndDx / progressLineDistance)
            }
            changeLeft = false
            changeRight = false
        }

        if (changeLeft) {
            if (event?.action == MotionEvent.ACTION_MOVE) {
                swipeStartController(event.rawX)
            }
        } else if (changeRight) {
            if (event?.action == MotionEvent.ACTION_MOVE) {
                swipeEndController(event.rawX)
            }
        }
        parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    private fun swipeStartController(rawX: Float) {
        tenSecWidth = (10000*progressLineDistance/maximum)
        managerStartDx = startProgressValue*progressLineDistance/100
        managerEndDx = endProgressValue*progressLineDistance/100
        var deltaX = rawX - x
        if (deltaX <= 0) deltaX = 0f
        //if(dx>=(mControllerEndDx-mControllerWidth)) dx = mControllerEndDx-mControllerWidth
        if (deltaX >= (managerEndDx - tenSecWidth)) deltaX = managerEndDx - tenSecWidth

        managerStartDx = deltaX
        startProgressValue = managerStartDx * 100 / progressLineDistance
        onChangeListener?.onSwipeLeft(managerStartDx / progressLineDistance)
        invalidate()
    }

    private fun swipeEndController(rawX: Float) {
        tenSecWidth = (10000*progressLineDistance/maximum)
        managerStartDx = startProgressValue*progressLineDistance/100
        managerEndDx = endProgressValue*progressLineDistance/100
        var deltax = rawX - x
        // if(dx<=(mControllerStartDx+mControllerWidth)) dx = mControllerStartDx+mControllerWidth
        if (deltax <= (managerStartDx + tenSecWidth)) deltax = managerStartDx + tenSecWidth
        if (deltax >= width - managerWidth) deltax = width - managerWidth
        managerEndDx = deltax
        endProgressValue = managerEndDx * 100 / progressLineDistance
        onChangeListener?.onSwipeRight(managerEndDx / progressLineDistance)
        invalidate()
    }

    fun getStartOffset(): Int {
        //val startProgress = ((mControllerStartDx) / mProgressLineDistance)
        return (startProgressValue * maximum/100).roundToInt()
    }

    fun getLength(): Int {
        return (endProgressValue * maximum/100).roundToInt() - getStartOffset()
    }

    private fun getTextWidth(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }

    private fun getTextHeight(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    fun setMaxValue(timeMiniSec: Long) {
        maximum = (timeMiniSec).toInt()
        invalidate()
    }


    fun setOnChangeListener(onChangeListener: OnChangeListener) {
        this.onChangeListener = onChangeListener
    }


    fun setStartAndEndProgress(startProgress: Float, endProgress: Float) {
        startProgressValue = startProgress
        endProgressValue = endProgress
        managerStartDx = startProgressValue*progressLineDistance/100
        managerEndDx = endProgressValue*progressLineDistance/100
        Logger.e("mProgressLineDistance = $progressLineDistance")
        invalidate()
    }


    interface OnChangeListener {
        fun onSwipeLeft(progress: Float)
        fun onLeftUp(progress: Float)
        fun onSwipeRight(progress: Float)
        fun onRightUp(progress: Float)
    }
}