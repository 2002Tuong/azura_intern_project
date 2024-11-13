package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.cardview.widget.CardView

class AnimCardView: CardView {

    private val scaleRatio = 0.02f
    private val DURATION = 100L
    var scale = 1f
    private var latestScale = 1f
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

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
        latestScale = scale - scaleRatio
        val anim = animate().scaleX(latestScale).scaleY(latestScale)
        anim.interpolator = AccelerateInterpolator()
        anim.setDuration(DURATION).start()
    }

    private fun zoomOut() {
        if (scale - scaleRatio == latestScale)
        animate().scaleX(scale).scaleY(scale).setInterpolator(DecelerateInterpolator()).setDuration(DURATION).start()
    }

}