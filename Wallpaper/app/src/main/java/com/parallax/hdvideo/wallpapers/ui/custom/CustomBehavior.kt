package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

class CustomBehavior: AppBarLayout.Behavior {

    private var canRecyclerViewScrolled: Boolean = true
    var maxItemOnScreen = 10

    constructor() : super()
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        updateScrollable(target)
        return canRecyclerViewScrolled && super.onStartNestedScroll(
            parent,
            child,
            directTargetChild,
            target,
            nestedScrollAxes,
            type
        )
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return canRecyclerViewScrolled && super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        return canRecyclerViewScrolled && super.onInterceptTouchEvent(parent, child, ev)
    }


    private fun updateScrollable(targetChild: View)  {
        canRecyclerViewScrolled = if (targetChild is RecyclerView) {
            targetChild.adapter?.run {
                itemCount > maxItemOnScreen
            } ?: false
        } else {
            true
        }
    }
}