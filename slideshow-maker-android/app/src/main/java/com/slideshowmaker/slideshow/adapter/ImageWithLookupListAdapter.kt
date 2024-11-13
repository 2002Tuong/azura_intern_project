package com.slideshowmaker.slideshow.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideData
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.ImageFilterHelper
import kotlinx.android.synthetic.main.item_view_image_list_in_slide_show.view.*

class ImageWithLookupListAdapter(private val onSelectImage:(Long)->Unit): BaseAdapter<ImageSlideData>() {
    private var mCurPosition = -1
    override fun doGetViewType(position: Int): Int = R.layout.item_view_image_list_in_slide_show

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val imgItem = _itemArray[position]
        itemView.setOnClickListener {
            _curItem = imgItem
            mCurPosition = position
            onSelectImage.invoke(imgItem.slideId)
            notifyDataSetChanged()
        }
        if(position == mCurPosition) {
            itemView.strokeBg.visibility = View.VISIBLE
        } else {
            itemView.strokeBg.visibility = View.GONE
        }
        Glide.with(itemView.context).load(imgItem.fromImagePath).into(itemView.imagePreview)
    }

    fun changeLookupOfCurretItem(lookupType: ImageFilterHelper.LookupType) {
        _curItem?.lookupType = lookupType
    }

    fun changeFilterOfAllItem(filterType: ImageFilterHelper.LookupType){
        _itemArray.forEach {
            it.lookupType = filterType
        }
    }
    fun changeHighlightItem(position:Int) :ImageFilterHelper.LookupType{
        if(position >= 0 && position < _itemArray.size) {
            mCurPosition = position
            _curItem = _itemArray[mCurPosition]
            notifyDataSetChanged()
            return _itemArray[mCurPosition].lookupType
        }
        return ImageFilterHelper.LookupType.DEFAULT

    }

}