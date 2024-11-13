package com.slideshowmaker.slideshow.ui.base

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.slideshowmaker.slideshow.R
import kotlin.math.max
import kotlin.math.sqrt

open class RippleView : View {

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private var onClickHandler: (() -> Unit)? = null

    private val lamda = 30f
    private var currentRad = 0

    private var maximumRad = 0f
    private var currentXValue = 0f
    private var currentYValue = 0f

    private var cornerRad = 0f

    private val fillingPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#4DFFFFFF")
        isAntiAlias = true
    }


    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
        val typedAttrArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView)
        cornerRad = typedAttrArray.getDimension(R.styleable.RippleView_cornerRadius, 0f)
        typedAttrArray.recycle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN || event?.action == MotionEvent.ACTION_POINTER_DOWN || event?.action == MotionEvent.ACTION_BUTTON_PRESS) {

            currentXValue = event.x
            currentYValue = event.y
            currentRad = 0
            maximumRad = max(
                (sqrt(currentXValue * currentXValue + currentYValue * currentYValue)),
                sqrt((width - currentXValue) * (width - currentXValue) + (height - currentYValue) * (height - currentYValue))
            ) + 100f
            drawRipple()


        }

        return true
    }

    fun getClipPath(): Path {
        val clipPath = Path()
        clipPath.reset()
        clipPath.addRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            cornerRad,
            cornerRad,
            Path.Direction.CW
        )
        clipPath.close()
        return clipPath
    }


    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(getClipPath())
        super.onDraw(canvas)
        canvas.drawCircle(currentXValue, currentYValue, currentRad.toFloat(), fillingPaint)
    }

    private fun drawRipple() {
        var isPressed = true
        val valueAnimator = ValueAnimator.ofFloat(0f, maximumRad)
        valueAnimator.setDuration(350)
            .addUpdateListener {
                it.animatedFraction
                currentRad = (maximumRad * it.animatedFraction).toInt()
                if (isPressed)
                    invalidate()
            }
        valueAnimator.interpolator = LinearOutSlowInInterpolator()
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

                onClickHandler?.invoke()
                currentRad = 0
                isPressed = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationStart(animation: Animator) {

            }

        })
        valueAnimator.start()
    }

    fun setClick(onClick: () -> Unit) {
        this.onClickHandler = onClick
    }


}