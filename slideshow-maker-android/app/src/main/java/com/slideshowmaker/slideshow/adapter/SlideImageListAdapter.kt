package com.slideshowmaker.slideshow.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.models.SlideSourceModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlinx.android.synthetic.main.item_view_image_list_in_slide_show.view.*

class SlideImageListAdapter : BaseAdapter<SlideSourceModel>() {

    var onClickItemCallback:((Int)->Unit)?=null

    override fun doGetViewType(position: Int): Int = R.layout.item_view_image_list_in_slide_show

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val slideItem = _itemArray[position]
        val imageSize = DimenUtils.density(itemView.context)*64
        if(slideItem.isSelect) {
            itemView.strokeBg.visibility = View.VISIBLE
        } else {
            itemView.strokeBg.visibility = View.GONE
        }
        Glide.with(itemView.context).load(slideItem.path).into(itemView.imagePreview)
        itemView.setOnClickListener {
            setOffAll()

            slideItem.isSelect = true
            notifyDataSetChanged()
            onClickItemCallback?.invoke(position)
        }
    }

    private fun setOffAll(){
        for(item in _itemArray) item.isSelect = false

    }

    fun addImagePathList(arrayList: ArrayList<String>) {
        _itemArray.clear()
        notifyDataSetChanged()
        for(item in arrayList) {
            _itemArray.add(SlideSourceModel(item))
        }
        notifyDataSetChanged()
    }

    fun changeVideo(position:Int) {
        if(position >= 0 && position < _itemArray.size) {

            setOffAll()
            _itemArray[position].isSelect =true
            notifyDataSetChanged()
        }
    }

    fun changeHighlightItem(position:Int) {
        if(position >= 0 && position < _itemArray.size) {
            setOffAll()
            _itemArray[position].isSelect =true
            notifyDataSetChanged()
        }
    }
}