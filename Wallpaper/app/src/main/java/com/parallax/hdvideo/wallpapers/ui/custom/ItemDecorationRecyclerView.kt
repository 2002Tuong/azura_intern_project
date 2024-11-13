package com.parallax.hdvideo.wallpapers.ui.custom

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


open class ItemDecorationRecyclerView(var marginItem: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,  parent: RecyclerView,  state: RecyclerView.State) {
//        val manager = parent.layoutManager as? GridLayoutManager ?: return super.getItemOffsets(outRect, view, parent, state)
//        if (manager.orientation == GridLayoutManager.VERTICAL) {
//            val position = parent.getChildLayoutPosition(view)
//            val columns = manager.spanCount
//            if (manager.spanSizeLookup.getSpanSize(position) == columns) return super.getItemOffsets(outRect, view, parent, state)
//            val padding = marginItem * (columns - 1) / columns
//            when (position % columns) {
//                0 -> {
//                    // start
//                    outRect.left = 0
//                    outRect.right = padding
//                }
//                columns - 1 -> {
//                    // end
//                    outRect.left = padding
//                    outRect.right = 0
//                }
//                else -> {
//                    // center
//                    val left = marginItem - padding
//                    outRect.right = padding - left
//                    outRect.left = left
//                }
//            }
            outRect.right = marginItem
            outRect.top = marginItem
//        }
    }

}