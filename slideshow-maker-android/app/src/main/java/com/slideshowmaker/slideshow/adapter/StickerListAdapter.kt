package com.slideshowmaker.slideshow.adapter

import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_view_sticker_list.view.*

class StickerListAdapter(val callback: (String) -> Unit) : BaseAdapter<String>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_view_sticker_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val item = _itemArray[position]
        itemView.previewSticker.setImageResource(item.toInt())
        itemView.setOnClickListener {
            callback.invoke(item)
        }
    }
}