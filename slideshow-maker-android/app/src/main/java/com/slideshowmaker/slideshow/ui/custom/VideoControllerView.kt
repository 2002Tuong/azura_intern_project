package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class VideoControllerView :View {


    private var sizeOfText = 14f
    private var lineHeight = 2f
    private var ballRadius = 10f
   // private var mFullscreenIconSize = 18f

    private val textPaint = Paint()
    private val disableLinePaint = Paint()
    private val highlightLinePaint = Paint()
    private val whiteBallPaint = Paint()
    private val mFullscreenIconPaint = Paint()

    private var paddingValue = 12f

    private var density = 1f

    private var maximumDuration = 0f // mini sec
    private var curProgress = 0f

    private var curTimeTextWidth = 1f
    private var maxTimeTextOffsetStart = 1f
    private var distance = 1f

    private val ballControllerRegion = Region()

    private var deltaX = 0f

    private var startPosOffset = 0L

    var onChangeListenerCallback: OnChangeListener? = null

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        setBackgroundColor(context.getColor(R.color.greyscale800))
        density = DimenUtils.density(context)

        sizeOfText*=density
        lineHeight*=density
        ballRadius*=density
        //mFullscreenIconSize*=mDensity
        paddingValue*=density

        textPaint.apply {
            isAntiAlias = true
            textSize = sizeOfText
            color = Color.WHITE
            typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        }

        disableLinePaint.apply {
            isAntiAlias = true
            textSize = sizeOfText
            color = Color.parseColor("#4E5563")
        }

        highlightLinePaint.apply {
            isAntiAlias = true
            textSize = sizeOfText
            color = Color.parseColor("#FB7609")
          //  color = Color.BLACK
        }

        whiteBallPaint.apply {
            isAntiAlias = true
            textSize = sizeOfText
            color = Color.parseColor("#FB7609")
        }

    }

    private fun getTextWidth(text:String, paint: Paint):Float {
        val rectangle = Rect()
        paint.getTextBounds(text, 0, text.length, rectangle)
        return rectangle.width().toFloat()
    }
    private fun getTextHeight(text:String, paint: Paint):Float {
        val rectangle = Rect()
        paint.getTextBounds(text, 0, text.length, rectangle)
        return rectangle.height().toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        drawCurrentTime(canvas)
        drawMaxTime(canvas)
        drawDisableLine(canvas)
        drawHighlightLine(canvas)
        drawBall(canvas)
    }



    private fun drawCurrentTime(canvas: Canvas?) {
        val curTime = maximumDuration*curProgress/100

            val text = try {
                convertSecToTimeString(curTime.roundToLong()/1000)
            } catch (e:Exception) {
                convertSecToTimeString(0)
            }
            val right = paddingValue

            canvas?.drawText(text, right, height/2f+getTextHeight(text,textPaint)/2f, textPaint)


    }

    private fun drawMaxTime(canvas: Canvas?) {
        val timeInString = try {
            convertSecToTimeString((maximumDuration/1000).roundToInt())
        } catch (e:Exception) {
            convertSecToTimeString(0)
        }
        //val text = convertSecToTimeString((mMaxDuration/1000).roundToLong())
        val rightSide = width  - 2*paddingValue - getTextWidth(timeInString, textPaint)
        maxTimeTextOffsetStart = rightSide
        curTimeTextWidth = getTextWidth(timeInString, textPaint)
        canvas?.drawText(timeInString, rightSide, height/2f+getTextHeight(timeInString,textPaint)/2f, textPaint)
    }

    private fun drawDisableLine(canvas: Canvas?) {
        val offset = paddingValue*2+curTimeTextWidth
        distance = maxTimeTextOffsetStart-paddingValue-offset
        deltaX = distance*curProgress/100+offset
        val rectF = RectF(offset, height/2f-lineHeight/2f, offset+distance,height/2f+lineHeight)
        canvas?.drawRect(rectF, disableLinePaint)

        //canvas?.drawRect(offset, height/2f-mLineHeight/2f, offset+mDistance,height/2f+mLineHeight, mDisableLinePaint)
    }
    private fun drawHighlightLine(canvas: Canvas?) {

        val sValue = (startPosOffset.toFloat()/maximumDuration)
        if(curProgress/100 < sValue) {
            return
        }

        val offset = paddingValue*2+curTimeTextWidth+distance*sValue
        canvas?.drawRect(offset, height/2f-lineHeight/2f, offset+distance*((curProgress)/100-sValue),height/2f+lineHeight, highlightLinePaint)
    }


    private fun drawBall(canvas: Canvas?) {
        val drawPath = Path().apply {
            addCircle(deltaX, height/2f, ballRadius,Path.Direction.CW)
            val boundRecF = RectF()
            computeBounds(boundRecF, true)
            ballControllerRegion.setPath(this, Region(boundRecF.left.toInt(), boundRecF.top.toInt(), boundRecF.right.toInt(), boundRecF.bottom.toInt()))
        }
        canvas?.drawPath(drawPath, whiteBallPaint)
    }

    fun setMaxDuration(newMaxDurationMiniSec:Int) {
        maximumDuration = newMaxDurationMiniSec.toFloat()
        curProgress = 0f
        invalidate()
    }

    fun setCurrentDuration(durationMiniSec:Int) {
        if(isTouched) return
        var progress = (100*durationMiniSec)/maximumDuration
        if(progress <= 0f) progress = 0f
        else if(progress >= 100f) progress = 100f
        curProgress = progress
        invalidate()
    }
    fun setCurrentDuration(durationMiniSec:Long) {
        if(isTouched) return
        var progress = (100*durationMiniSec)/maximumDuration
        if(progress <= 0f) progress = 0f
        else if(progress >= 100f) progress = 100f
        curProgress = progress
        invalidate()
    }
    fun setProgress(progress:Float) {
        curProgress = when {
            progress <= 0 -> 0f
            progress >= 100f -> 100f
            else -> progress
        }
        invalidate()
    }
    private var isTouched = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event?.action == MotionEvent.ACTION_DOWN) {
            if(event.rawX < (2*paddingValue+curTimeTextWidth) || event.rawX > (maxTimeTextOffsetStart-paddingValue)) {} else {
                isTouched = true
                onMoveController(event.rawX)
            }
        } else if(event?.action == MotionEvent.ACTION_MOVE) {
            onMoveController(event.rawX)
        } else if(event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_CANCEL) {
            onTouchUp(event.rawX)
            isTouched = false
        }
        return true
    }

    private fun onMoveController(rawX:Float) {
        var progress = 100*(rawX-(2*paddingValue+curTimeTextWidth))/distance

        if(progress<=0f)progress = 0f
        else if(progress >= 100f) progress = 100f
        curProgress = progress
        invalidate()
        onChangeListenerCallback?.onMove(curProgress)

    }

    private fun onTouchUp(rawX: Float) {
        var progress = 100*(rawX-(2*paddingValue+curTimeTextWidth))/distance
        if(progress <= 0f) progress = 0f
        else if(progress >= 100f) progress = 100f
        curProgress = progress
        invalidate()
        onChangeListenerCallback?.onUp((maximumDuration*progress/100).roundToInt())
    }

    private fun convertSecToTimeString(sec: Int): String {
        return if (sec >= 3600) {
            val hour = zeroPrefix((sec / 3600).toString())
            val min = zeroPrefix(((sec % 3600) / 60).toString())
            val sec = zeroPrefix(((sec % 3600) % 60).toString())
            "$hour:$min:$sec"
        } else {
            val min = zeroPrefix(((sec % 3600) / 60).toString())
            val sec = zeroPrefix(((sec % 3600) % 60).toString())
            "$min:$sec"
        }
    }
    private fun convertSecToTimeString(sec: Long): String {
        return if (sec >= 3600) {
            val hour = zeroPrefix((sec / 3600).toString())
            val min = zeroPrefix(((sec % 3600) / 60).toString())
            val sec = zeroPrefix(((sec % 3600) % 60).toString())
            "$hour:$min:$sec"
        } else {
            val min = zeroPrefix(((sec % 3600) / 60).toString())
            val sec = zeroPrefix(((sec % 3600) % 60).toString())
            "$min:$sec"
        }
    }
    private fun zeroPrefix(string: String):String {
        if(string.length<2) return "0$string"
        return string
    }

    interface OnChangeListener {
        fun onUp(timeMilSec:Int)
        fun onMove(progress:Float)
    }
     fun changeStartPositionOffset(timeMs:Long) {
        startPosOffset = timeMs
         invalidate()
    }

}