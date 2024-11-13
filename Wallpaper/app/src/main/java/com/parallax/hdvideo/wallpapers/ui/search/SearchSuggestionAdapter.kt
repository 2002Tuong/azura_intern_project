package com.parallax.hdvideo.wallpapers.ui.search

import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.databinding.ItemSuggestionSearchBinding
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList

class SearchSuggestionAdapter: BaseAdapterList<HashTag, ItemSuggestionSearchBinding>(
    layoutId = {R.layout.item_suggestion_search}) {
    var onDeleteItemCallback : ((HashTag, Int) -> Unit)? = null
    var onClickNewTextCallback : ((HashTag, Int) -> Unit)? = null
    override fun onBindViewHolder(
        holder: BaseViewHolder<HashTag, ItemSuggestionSearchBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val data = getData(position)
        holder.dataBinding.apply {
            name.text = data.name
            val drawable = if (data.isHistory) R.drawable.ic_x_re
            else R.drawable.ic_arrowlineupleft_re
            utilButton.setImageResource(drawable)
            utilButton.setOnClickListener {
                if (data.isHistory)
                    onDeleteItemCallback?.invoke(data, position)
                else onClickNewTextCallback?.invoke(data, position)
            }
        }
    }
}