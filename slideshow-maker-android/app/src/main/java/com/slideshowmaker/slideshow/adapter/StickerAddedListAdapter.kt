package com.slideshowmaker.slideshow.adapter

import android.view.View
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.models.StickerAddedModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.Logger
import kotlinx.android.synthetic.main.item_view_sticker_added.view.*

class StickerAddedListAdapter(private val onChangeListener: OnChange) : BaseAdapter<StickerAddedModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_view_sticker_added
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val stickerItem = _itemArray[position]
        itemView.setOnClickListener {
           // if(mCurrentItem?.stickerViewId == item.stickerViewId) return@setOnClickListener
            setOffAll()
            _curItem?.onEdit = false
            stickerItem.onEdit = true
            _curItem = stickerItem
            notifyDataSetChanged()
            onChangeListener.onClickSticker(stickerItem)
        }
        Logger.e("start end --> ${stickerItem.startTimeInMilSec} ${stickerItem.endTimeInMilSec}")
        if(stickerItem.onEdit) {
            itemView.grayBg.visibility = View.VISIBLE
        } else {
            itemView.grayBg.visibility = View.GONE
        }
        itemView.stickerAddedPreview.setImageBitmap(stickerItem.bitmap)
    }

    fun addNewSticker(stickerAddedModel: StickerAddedModel) {
        _curItem?.onEdit = false
        _itemArray.add(stickerAddedModel)
        _curItem = stickerAddedModel
        notifyDataSetChanged()
    }

    fun changeStartTime(startTimeMilSec:Int) {
        _curItem?.startTimeInMilSec = startTimeMilSec
    }

    fun changeEndTime(endTimeMilSec:Int) {
        _curItem?.endTimeInMilSec = endTimeMilSec
    }

    fun deleteItem(stickerAddedModel: StickerAddedModel) {
        _itemArray.remove(stickerAddedModel)
        notifyDataSetChanged()
    }

    fun deleteAllItem() {
        _itemArray.clear()
        notifyDataSetChanged()
    }

    fun setOffAll() {
        for(item in _itemArray) {
            item.onEdit = false
        }
        notifyDataSetChanged()
    }

    interface OnChange {
        fun onClickSticker(stickerAddedModel: StickerAddedModel)
    }
}