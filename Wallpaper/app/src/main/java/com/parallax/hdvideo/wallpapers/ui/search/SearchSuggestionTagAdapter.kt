package com.parallax.hdvideo.wallpapers.ui.search

import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.databinding.ItemSearchSuggestedTagBinding
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList

class SearchSuggestionTagAdapter : BaseAdapterList<HashTag, ItemSearchSuggestedTagBinding>(
    layoutId = { R.layout.item_search_suggested_tag}
) {

    override fun onBindViewHolder(
        holder: BaseViewHolder<HashTag, ItemSearchSuggestedTagBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val item = getData(position)
        holder.dataBinding.tag.text = item.name
    }
}