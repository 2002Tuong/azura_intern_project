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

class SeekBarWithText : View {
    private val rect = Rect()
    private var sizeOfText = 12f
    private var sizeOfLine = 2f
    private var sizeOfBall = 12f

    private var disableColor = Color.parseColor("#ffffff")
    private var highlightColor = Color.parseColor("#FB7609")

    private val disablePaint = Paint()
    private val highlightPaint = Paint()
    private val textProgressPaint = Paint()

    private var progressDistance = 0f

    private var progress = 100f

    private var onProgressChangeListener: ProgressChangeListener? = null

    constructor(context: Context) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs: AttributeSet?) {

        sizeOfText = (DimenUtils.density(context) * sizeOfText)
        sizeOfLine = (DimenUtils.density(context) * sizeOfLine)
        sizeOfBall = (DimenUtils.density(context) * sizeOfBall)

        if(attrs == null) return
        val typedAttrArray = context.obtainStyledAttributes(attrs, R.styleable.SeekBarWithText)
        highlightColor = typedAttrArray.getColor(R.styleable.SeekBarWithText_highlightColor, Color.parseColor("#FF604D"))
        typedAttrArray.recycle()

        disablePaint.apply {
            color = disableColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        highlightPaint.apply {
            color = highlightColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        textProgressPaint.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = textSize
            typeface =  ResourcesCompat.getFont(context, R.font.roboto_medium)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeaMode = MeasureSpec.getMode(heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), getMeasuredDimensionHeight(heightMeaMode, heightMeasureSpec))

    }

    private fun getMeasuredDimensionHeight(mode: Int, height: Int): Int {
        return when (mode) {
            MeasureSpec.AT_MOST -> {
                (sizeOfBall*2+6).roundToInt()
            }
            else -> {
                height
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        progressDistance = width-sizeOfBall*2
        drawDisableLine(canvas)
        drawHighlightLine(canvas)
        drawBall(canvas)
        drawTextProgress(canvas)
    }

    private fun drawDisableLine(canvas: Canvas?) {
        canvas?.drawRect(0f+sizeOfBall, height / 2f - sizeOfLine / 2, progressDistance+sizeOfBall, height / 2f + sizeOfLine / 2, disablePaint)
    }

    private fun drawHighlightLine(canvas: Canvas?) {
        val rightSide = progressDistance*progress/100
        canvas?.drawRect(0f+sizeOfBall, height / 2f - sizeOfLine / 2, rightSide+sizeOfBall, height / 2f + sizeOfLine / 2, highlightPaint)
    }

    private fun drawBall(canvas: Canvas?) {
        val cirCenterX = progressDistance*progress/100
        canvas?.drawCircle(cirCenterX+sizeOfBall,height/2f, sizeOfBall, highlightPaint)
    }

    private fun drawTextProgress(canvas: Canvas?) {
        val text = progress.roundToInt().toString()
        val rightSide = (progressDistance*progress/100)-getTextWidth(text, textProgressPaint)/2f
        canvas?.drawText(text, rightSide+sizeOfBall, height/2+getTextHeight(text, textProgressPaint)/2f, textProgressPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN) {
            changeProgress(event.rawX)
        } else if(event?.action == MotionEvent.ACTION_MOVE) {
            changeProgress(event.rawX)
        }

        return true
    }

    private fun changeProgress(rawX:Float) {
        //val marginS = this.marginStart
        val delta = rawX-0-sizeOfBall-x
       // val distance = rawX-0-mBallSize-x
        var progress = ((delta/progressDistance)*100)
        if(progress<=0)progress = 0f
        else if(progress >= 100) progress = 100f

        if(progress.roundToInt() != this.progress.roundToInt()) {
            onProgressChangeListener?.onChange(progress.roundToInt())
        }

        this.progress = progress
        invalidate()
    }

    private fun getTextWidth(text:String, paint: Paint):Float {
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }
    private fun getTextHeight(text:String, paint: Paint):Float {
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    fun setProgressChangeListener(onChange:(duration:Int)->Unit) {
        onProgressChangeListener = object : ProgressChangeListener {
            override fun onChange(progress: Int) {
                onChange.invoke(progress)
            }
        }
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun setHighlightColor(color: Int) {
        highlightPaint.color = color
        invalidate()
    }

    interface ProgressChangeListener {
        fun onChange(progress:Int)
    }



}