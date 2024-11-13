package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class CustomGridLayoutManager: GridLayoutManager {

    constructor(context: Context?, spanCount: Int) : this(context, spanCount, RecyclerView.VERTICAL, false)
    constructor(context: Context?, spanCount: Int, orientation: Int, reverseLayout: Boolean) :
            super(context, spanCount, orientation, reverseLayout)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val firstItemPos = findFirstVisibleItemPosition()
        val lastItemPos = findLastVisibleItemPosition()
        val distanceRows = (lastItemPos - firstItemPos) * 5 / 2
        val avg = (firstItemPos + lastItemPos) ushr 1
        if (abs(avg - position) > distanceRows) {
            val pos = if (position < avg) position + distanceRows else position - distanceRows
            scrollToPosition(pos)
        }
        super.smoothScrollToPosition(recyclerView, state, position)
    }

}