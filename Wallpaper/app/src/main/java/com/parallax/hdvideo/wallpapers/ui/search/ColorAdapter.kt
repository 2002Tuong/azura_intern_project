package com.parallax.hdvideo.wallpapers.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.databinding.ItemColorListFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.dpToPx

class ColorAdapter(
    var layoutCallback: ((position: Int) -> Int)? = null,
    var onBindViewCallback: ((binding: ItemColorListFragmentBinding, data: HashTag, position: Int) -> Unit)? = null)
    : BaseAdapterList<HashTag, ItemColorListFragmentBinding>(layoutCallback, onBindViewCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<HashTag, ItemColorListFragmentBinding> {
        val viewHolder = ItemColorViewHolder(
            ItemColorListFragmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.layoutParams.apply {
            width = ((AppConfiguration.widthScreenValue - dpToPx(48f)) / 4.5f).toInt()
            height = width
        }
        viewHolder.dataBinding.ivColor.layoutParams.width =
            ((AppConfiguration.widthScreenValue - dpToPx(48f)) / 4.5f).toInt() - dpToPx(5f)
        viewHolder.dataBinding.ivColor.layoutParams.height =
            viewHolder.dataBinding.ivColor.layoutParams.width
        return viewHolder
    }

    inner class ItemColorViewHolder constructor(binding: ItemColorListFragmentBinding) :
        BaseAdapterList.BaseViewHolder<HashTag, ItemColorListFragmentBinding>(binding)
}