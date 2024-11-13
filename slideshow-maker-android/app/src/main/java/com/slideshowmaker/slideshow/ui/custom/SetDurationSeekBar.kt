package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlin.math.roundToInt

class SetDurationSeekBar : View {

    private var sizeOfText = 12f
    private var sizeOfSelectedText = 14f
    private var sizeOfLine = 5f
    private var sizeOfMiniBall = 8f
    private var sizeOfBigBall = 24f
    private var disableColor = Color.parseColor("#4E5563")
    private var highlightColor = Color.parseColor("#FB7609")

    private val disablePaintObj = Paint()
    private val highlightPaintObj = Paint()
    private val selectedTextPaintObj = Paint()

    private var currentPos = 2

    private var onDurationChangeListener: DurationChangeListener? = null

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }


    private fun initAttrs(attributes: AttributeSet?) {
        sizeOfText = (DimenUtils.density(context) * sizeOfText)
        sizeOfSelectedText = (DimenUtils.density(context) * sizeOfSelectedText)
        sizeOfLine = (DimenUtils.density(context) * sizeOfLine)
        sizeOfMiniBall = (DimenUtils.density(context) * sizeOfMiniBall / 2)
        sizeOfBigBall = (DimenUtils.density(context) * sizeOfBigBall / 2)

        disablePaintObj.apply {
            color = disableColor
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = textSize
            typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        }

        highlightPaintObj.apply {
            color = highlightColor
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = textSize
            typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        }

        selectedTextPaintObj.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = sizeOfSelectedText
            typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), getMeasuredDimensionHeight(heightMode, heightMeasureSpec))
    }

    private fun getMeasuredDimensionHeight(mode: Int, height: Int): Int {
        return when (mode) {
            MeasureSpec.AT_MOST -> {
                (sizeOfText + sizeOfMiniBall + sizeOfBigBall+12+4).roundToInt()
            }
            else -> {
                height
            }
        }
    }
    private val rect = Rect()

    override fun onDraw(canvas: Canvas) {
        drawDisableLine(canvas)
        drawHighlightLine(canvas)
        for(i in 1 until currentPos) {
            drawHighlightMiniBall(canvas, i)
            drawText(canvas, i, highlightPaintObj)
        }
        drawBigBall(canvas, currentPos)
        for(i in currentPos+1..10) {
            drawDisableMiniBall(canvas, i)
            drawText(canvas, i, disablePaintObj)
        }
        drawSelectedText(canvas, currentPos, selectedTextPaintObj)
    }



    private fun drawDisableLine(canvas: Canvas?) {
        canvas?.drawRect(0f, height / 2f - sizeOfLine / 2, width.toFloat(), height / 2f + sizeOfLine / 2, disablePaintObj)
    }

    private fun drawHighlightLine(canvas: Canvas?) {
        var rightSide = (width / 20f) + (width / 10f) * (currentPos - 1)
        if(currentPos == 10) rightSide = width.toFloat()
        canvas?.drawRect(0f, height / 2f - sizeOfLine / 2, rightSide, height / 2f + sizeOfLine / 2, highlightPaintObj)
    }

    private fun drawDisableMiniBall(canvas: Canvas?, number: Int) {
        val rightSide = (width / 20f) + (width / 10f) * (number - 1)
        canvas?.drawCircle(rightSide, height / 2f, sizeOfMiniBall, disablePaintObj)
    }

    private fun drawHighlightMiniBall(canvas: Canvas?, number: Int) {
        val rightSide = (width / 20f) + (width / 10f) * (number - 1)
        canvas?.drawCircle(rightSide, height / 2f, sizeOfMiniBall, highlightPaintObj)
    }

    private fun drawBigBall(canvas: Canvas?, number: Int) {
        val rightSide = (width / 20f) + (width / 10f) * (number - 1)
        canvas?.drawCircle(rightSide, height / 2f, sizeOfBigBall, highlightPaintObj)
    }

    private fun drawText(canvas: Canvas?, number: Int, paint: Paint){
        val rightSide = (width / 20f) + (width / 10f) * (number - 1)-2-(getTextWidth(number.toString(), paint)/2f)
        canvas?.drawText(number.toString(), rightSide, getTextHeight(number.toString(), paint) +4, paint)
    }

    private fun drawSelectedText(canvas: Canvas?, number: Int, paint: Paint){
        val rightSide = (width / 20f) + (width / 10f) * (number - 1)-2-(getTextWidth(number.toString(), paint)/2f)
        canvas?.drawText(number.toString(), rightSide, height/2f+getTextHeight(number.toString(), paint)/2, paint)
    }

    private fun getTextWidth(text:String, paint: Paint):Float {
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }
    private fun getTextHeight(text:String, paint: Paint):Float {
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN) {
            changePosition(event.rawX)
        } else if(event?.action == MotionEvent.ACTION_MOVE) {
            changePosition(event.rawX)
        } else if(event?.action == MotionEvent.ACTION_UP) {
            onDurationChangeListener?.onTouchUp(currentPos)
        }

        return true
    }

    private fun changePosition(rawX:Float) {

        //val marginS = this.marginStart
        val deltaX = rawX- 0 -width/20f
        var position = (deltaX/(width/10f)).roundToInt()+1
        if(position<1) position = 1
        else if(position>10) position = 10

        if(position != currentPos) {
            onDurationChangeListener?.onChange(position)
        }

        currentPos = position

        invalidate()
    }

    fun setCurrentDuration(duration: Int) {
        if(currentPos != duration) {
            currentPos = duration
            invalidate()
        }
    }

    fun setDurationChangeListener(onChange:(duration:Int)->Unit, onTouchUp:(duration:Int)->Unit) {
        onDurationChangeListener = object : DurationChangeListener {
            override fun onChange(duration: Int) {
                onChange.invoke(duration)
            }

            override fun onTouchUp(duration: Int) {
                onTouchUp.invoke(duration)
            }
        }
    }

    interface DurationChangeListener {
        fun onChange(duration:Int)
        fun onTouchUp(duration: Int)
    }

}