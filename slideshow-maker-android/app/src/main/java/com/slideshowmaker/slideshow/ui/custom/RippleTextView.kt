package com.slideshowmaker.slideshow.ui.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import com.slideshowmaker.slideshow.R
import kotlin.math.max
import kotlin.math.sqrt

class RippleTextView : AppCompatTextView {

    private var cornerRad = 0f
    private var backgroundColor = Color.TRANSPARENT
    protected var onClickCallback:(()->Unit)? = null
    protected var instantClickCallback:(()->Unit)? = null
    private var isPressed = false
    private val backgroundPaint = Paint()
    private val ripplePaint = Paint()
    private var curRadius = 0
    private var maximumRadius = 0f
    private var curPointX = 0f
    private var curPointY = 0f
    constructor(context: Context) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs:AttributeSet?) {
        if(attrs == null) return
        val typedAttrArray = context.obtainStyledAttributes(attrs, R.styleable.RippleTextView)
        cornerRad = typedAttrArray.getDimension(R.styleable.RippleTextView_cornerRadiusTextView, 0f)
        backgroundColor = typedAttrArray.getColor(R.styleable.RippleTextView_bgColorTextView, Color.TRANSPARENT)
        typedAttrArray.recycle()

        backgroundPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = backgroundColor
        }

        ripplePaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.parseColor("#4DFFFFFF")
        }

    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(getClipPath())
        canvas.drawPath(getClipPath(), backgroundPaint)
        super.onDraw(canvas)

        if(isPressed) {
            canvas.drawCircle(curPointX, curPointY, curRadius.toFloat(), ripplePaint)
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_UP) {
            instantClickCallback?.invoke()
            curPointX = event.x
            curPointY = event.y
            curRadius = 0
            maximumRadius = max((sqrt(curPointX*curPointX+curPointY*curPointY)), sqrt((width-curPointX) *(width-curPointX)+(height-curPointY)*(height-curPointY))) + 100f
            drawRipple()
        }



        return true
    }

    fun getClipPath(): Path {
        val clipPath = Path()
        clipPath.reset()
        clipPath.addRoundRect(RectF(0f,0f,width.toFloat(), height.toFloat()), cornerRad, cornerRad, Path.Direction.CW)
        clipPath.close()
        return clipPath
    }

    private fun drawRipple() {
        isPressed = true
        val valueAnimator = ValueAnimator.ofFloat(0f,maximumRadius)
        valueAnimator.addUpdateListener {
            it.animatedFraction
            curRadius = (maximumRadius*it.animatedFraction).toInt()
            invalidate()
        }
        valueAnimator.duration = 200
        valueAnimator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                onClickCallback?.invoke()
                isPressed = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationStart(animation: Animator) {

            }

        })
        valueAnimator.start()
    }

    fun setClick(onClick:()->Unit) {
        this.onClickCallback = onClick
    }
    fun setInstanceClick(onClick:()->Unit) {
        this.instantClickCallback = onClick
    }

}