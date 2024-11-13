package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager

open class BaseViewPager : ViewPager {

    private var duration = 1000
    var hasTouched = true

    constructor(context: Context) : super(context) {
        postInitViewPager()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        postInitViewPager()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (!hasTouched) {
            false
        } else super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (!hasTouched) {
            false
        } else super.onInterceptTouchEvent(ev)
    }

    private fun postInitViewPager() {
        try {
            val viewpagerClass = ViewPager::class.java
            val scrollerField = viewpagerClass.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            scrollerField.set(this, ScrollerCustomDuration(context, duration))
        } catch (e: Exception) {
            print(this.javaClass.name + e.toString())
        }

    }

    fun setScrollDuration(duration: Int) {
        this.duration = duration
    }

    private class ScrollerCustomDuration : Scroller {
        var mills = 0
        @JvmOverloads
        constructor(
            context: Context,
            mills: Int,
            interpolator: Interpolator = DecelerateInterpolator()
        ) : super(context, interpolator) {
            this.mills = mills
        }


        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, duration)
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            super.startScroll(startX, startY, dx, dy, mills)
        }
    }
}