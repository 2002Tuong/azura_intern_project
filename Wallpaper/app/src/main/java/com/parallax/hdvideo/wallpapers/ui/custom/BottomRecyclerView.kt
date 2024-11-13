package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BottomRecyclerView: RecyclerView {

    var onLoadMoreCallback: (() -> Unit)? = null
    private var canLoadMore = false
    private val mLayoutManager = CustomLinearLayoutManager(context, HORIZONTAL, false)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        layoutManager = mLayoutManager
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val ss = super.onTouchEvent(e)
        val actionMasked = e.actionMasked
        canLoadMore = actionMasked == MotionEvent.ACTION_DOWN
                || actionMasked == MotionEvent.ACTION_POINTER_DOWN
                || actionMasked == MotionEvent.ACTION_MOVE
        return ss
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        if (dx > 0 && canLoadMore) {
            loadMore()
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            loadMore()
        }
    }

    private fun loadMore() {
        val manager = this.layoutManager as LinearLayoutManager
        val totalItem = manager.itemCount
        val pastVisibleItems = manager.findLastVisibleItemPosition()
        if (pastVisibleItems >= totalItem - 2) {
            onLoadMoreCallback?.invoke()
        }
    }

    override fun smoothScrollToPosition(position: Int) {
        super.smoothScrollToPosition(position)
    }
}