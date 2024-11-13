package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.constraintlayout.widget.ConstraintLayout

class AnimConstraintLayout : ConstraintLayout {

    private val MIN_SCALE = 0.95f
    private val DURATION = 100L
    var maximumScaleY = 1f
    private val distance = 0.02f
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun init() {

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                this.zoomIn()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.zoomOut()
            }
            else -> {

            }
        }
        return super.onTouchEvent(event)
    }

    private fun zoomIn() {
        val anim = animate().scaleX(1 - distance).scaleY(maximumScaleY - distance)
        anim.interpolator = AccelerateInterpolator()
        anim.setDuration(DURATION).start()
    }

    private fun zoomOut() {
        animate().scaleX(1f).scaleY(maximumScaleY).setInterpolator(BounceInterpolator()).setDuration(DURATION).start()
    }
}
