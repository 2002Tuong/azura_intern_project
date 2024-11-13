package com.slideshowmaker.slideshow.ui.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.slideshowmaker.slideshow.R
import kotlin.math.max
import kotlin.math.sqrt

open class RippleImageView :ImageView {

    constructor(context: Context) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }
    private var rippleColor = Color.parseColor("#4DFFFFFF")
    protected var onClickCallback:(()->Unit)? = null
    protected var onInstantClickCallback:(()->Unit)? = null

    private var currentRad = 0

    private var maximumRadius = 0f
    private var currentPointX = 0f
    private var currentPointY = 0f

    private var isPressed = false

    private var cornerRad = 0f


    private val fillingPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private fun initAttrs(attrs:AttributeSet?) {
        if(attrs == null) return
        val typedAttrArray = context.obtainStyledAttributes(attrs, R.styleable.RippleImageView)
        cornerRad = typedAttrArray.getDimension(R.styleable.RippleView_cornerRadius, 0f)
        rippleColor = typedAttrArray.getColor(R.styleable.RippleImageView_rippleColor, Color.parseColor("#4DFFFFFF"))
        typedAttrArray.recycle()

        fillingPaint.color = rippleColor
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event?.action == MotionEvent.ACTION_DOWN || event?.action == MotionEvent.ACTION_POINTER_DOWN) {
            onInstantClickCallback?.invoke()
            currentPointX = event.x
            currentPointY = event.y
            currentRad = 0
            maximumRadius = max((sqrt(currentPointX*currentPointX+currentPointY*currentPointY)), sqrt((width-currentPointX) *(width-currentPointX)+(height-currentPointY)*(height-currentPointY))) + 100f
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


    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(getClipPath())
        super.onDraw(canvas)

        if(isPressed) {
            canvas.drawCircle(currentPointX, currentPointY, currentRad.toFloat(), fillingPaint)
        }
    }

    private fun drawRipple() {
        isPressed = true
        val valueAnimator = ValueAnimator.ofFloat(0f,maximumRadius)
        valueAnimator.addUpdateListener {
            it.animatedFraction
            currentRad = (maximumRadius*it.animatedFraction).toInt()
            invalidate()
        }
        valueAnimator.duration = 200
        valueAnimator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

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
        this.onInstantClickCallback = onClick
    }
}