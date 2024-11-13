package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager

class VerticalViewPager : ViewPager {
    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return super.canScrollHorizontally(direction)
    }

    private fun init() {
        setPageTransformer(true, VerticalPageTransformer())
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val onIntercept = super.onInterceptTouchEvent(flipXY(ev))
        flipXY(ev)
        return onIntercept
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val onHandle = super.onTouchEvent(flipXY(ev))
        flipXY(ev)
        return onHandle
    }

    private fun flipXY(ev: MotionEvent): MotionEvent? {
        val widthValue = width.toFloat()
        val heightValue = height.toFloat()
        val xPos = ev.y / heightValue * widthValue
        val yPos = ev.x / widthValue * heightValue
        ev.setLocation(xPos, yPos)
        return ev
    }

    private class VerticalPageTransformer : PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val widthOfPage = view.width
            val heightOfPage = view.height
            if (position < -1) {
                view.alpha = 0f
            } else if (position <= 1) {
                view.alpha = 1f
                view.translationX = widthOfPage * -position
                val yPos = position * heightOfPage
                view.translationY = yPos
            } else {
                view.alpha = 0f
            }
        }
    }
}