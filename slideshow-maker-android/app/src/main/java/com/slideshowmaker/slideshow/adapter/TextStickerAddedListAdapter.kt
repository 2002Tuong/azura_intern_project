package com.slideshowmaker.slideshow.adapter

import android.view.View
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.models.TextStickerAddedModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_view_text_sticker_added.view.*

class TextStickerAddedListAdapter(private val onChange: OnChange) :
    BaseAdapter<TextStickerAddedModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_view_text_sticker_added

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val textStickeritem = _itemArray[position]

        itemView.setOnClickListener {
            setOffAll()
            _curItem?.onEdit = false
            textStickeritem.onEdit = true
            _curItem = textStickeritem
            notifyDataSetChanged()
            onChange.onClickTextSticker(textStickeritem)
        }

        if (textStickeritem.onEdit) {
            itemView.grayBg.visibility = View.VISIBLE
        } else {
            itemView.grayBg.visibility = View.GONE
        }
        itemView.textContent.text = textStickeritem.text
    }

    fun setOffAll() {
        for (item in _itemArray) {
            item.onEdit = false
        }
        _curItem = null
        notifyDataSetChanged()
    }

    fun addNewText(textStickerAddedModel: TextStickerAddedModel) {
        _curItem?.onEdit = false
        _itemArray.add(textStickerAddedModel)
        _curItem = textStickerAddedModel
        notifyDataSetChanged()
    }

    fun deleteItem(textStickerAddedModel: TextStickerAddedModel) {
        _itemArray.remove(textStickerAddedModel)
        notifyDataSetChanged()
    }

    fun deleteAllItem() {
        _itemArray.clear()
        notifyDataSetChanged()
    }

    fun getItemBytViewId(viewId: Int): TextStickerAddedModel? {
        for (item in _itemArray) {
            if (item.viewId == viewId) {
                return item
            }
        }
        return null
    }

    interface OnChange {
        fun onClickTextSticker(textStickerAddedModel: TextStickerAddedModel)
    }

}