package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.utils.Logger

class CustomLinearLayoutManager : LinearLayoutManager {

    private val MILLISECONDS_PER_INCH = 200f
    private val linearSmoothScrollerInstance: LinearSmoothScroller
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) :
            super(context, orientation, reverseLayout) {
        linearSmoothScrollerInstance = object : LinearSmoothScroller(context) {

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this@CustomLinearLayoutManager.computeScrollVectorForPosition(targetPosition)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                val speed = MILLISECONDS_PER_INCH / displayMetrics.densityDpi
                Logger.d("speed", speed)
                return speed
            }
        }
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val firstVisiblePos = findFirstVisibleItemPosition()
        val lastVisiblePos = findLastVisibleItemPosition()
        val delta = 10
        val newPos = when {
            position in (firstVisiblePos - delta..lastVisiblePos + delta) -> null
            firstVisiblePos > position -> position + delta
            else -> position - delta
        }
        linearSmoothScrollerInstance.targetPosition = position
        if (newPos == null) {
            startSmoothScroll(linearSmoothScrollerInstance)
        } else {
            scrollToPositionWithOffset(newPos, 0)
            startSmoothScroll(linearSmoothScrollerInstance)
        }
    }

}