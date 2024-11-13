package com.slideshowmaker.slideshow.adapter

import android.graphics.Color
import android.view.View
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_view_color_list.view.*

class TextColorListAdapter(val itemClickcallback:(Int)->Unit) : BaseAdapter<String>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_view_color_list
    private var mSelectedColor = ""
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = _itemArray[position]
        try {
            view.colorPreview.background.setTint(Color.parseColor(item))
            view.setOnClickListener {
                mSelectedColor = item
                itemClickcallback.invoke(Color.parseColor(item))
                notifyDataSetChanged()
            }
            if(mSelectedColor == item) {
                view.ivSelected.visibility = View.VISIBLE
            } else {
                view.ivSelected.visibility = View.GONE
            }
        } catch (e:Exception){

        }
    }
}