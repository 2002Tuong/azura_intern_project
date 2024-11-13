package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.viewpager.widget.ViewPager
import com.parallax.hdvideo.wallpapers.utils.dpToPx

class TabBarLayout: HorizontalScrollView {

    private lateinit var mTabBarView: TabBarView
    private val threshold = dpToPx(30f)
    private var onTabSelected: ((Int) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup(context)
    }

    fun setup(context: Context) {
        mTabBarView = TabBarView(context)
        addView(mTabBarView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        mTabBarView.tabPositionDidSelect = { tab, pos ->
            onTabSelected?.invoke(pos)
            if (tab.x + tab.width > width - threshold) {
                smoothScrollTo((tab.x + tab.width).toInt(), 0)
            } else {
                smoothScrollTo(tab.x.toInt(), 0)
            }
        }
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        mTabBarView.setupWithViewPager(viewPager)
    }

    fun setTabBackgroundColor(color : Int) {
        mTabBarView.setTabBackgroundColor(color)
    }

    fun updateFromUIWhenSelectedTab(callback: (Int) -> Unit) {
        onTabSelected = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onDetachedFromWindow() {
        onTabSelected = null
        super.onDetachedFromWindow()
    }
}