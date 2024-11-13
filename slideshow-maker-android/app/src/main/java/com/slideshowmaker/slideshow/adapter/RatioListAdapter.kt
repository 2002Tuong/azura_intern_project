package com.slideshowmaker.slideshow.adapter

import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.RatioInfo
import com.slideshowmaker.slideshow.models.RatioModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.layout_view_item_ratio.view.*

class RatioListAdapter(val callback: (item:RatioModel) -> Unit) : BaseAdapter<RatioModel>() {

    init {
        _itemArray.add(RatioModel(RatioInfo("1:1",R.drawable.icon_ratio_1_1, 1,1)))
        _itemArray.add(RatioModel(RatioInfo("16:9",R.drawable.icon_ratio_16_9, 16,9)))
        _itemArray.add(RatioModel(RatioInfo("4:3",R.drawable.icon_ratio_4_3, 4,3)))
        _itemArray.add(RatioModel(RatioInfo("9:16",R.drawable.icon_ratio_3_4, 9,16)))
        _itemArray.add(RatioModel(RatioInfo("3:4",R.drawable.icon_ratio_9_16, 3,4)))

    }
    private var selectedRatioIndex = 0

    fun getItem(position: Int) = _itemArray[position]

    override fun doGetViewType(position: Int): Int =R.layout.layout_view_item_ratio

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val ratioItem = _itemArray[position]

        itemView.ratioIcon.setImageResource(ratioItem.iconId)
        itemView.ratioTitle.text = ratioItem.title

        if(position == selectedRatioIndex) {
            itemView.layoutRatioIcon.setBackgroundResource(R.drawable.background_selected_transition)
        } else {
            itemView.layoutRatioIcon.setBackgroundResource(R.drawable.background_transparent)
        }

        itemView.setOnClickListener {
            callback.invoke(ratioItem)
            selectedRatioIndex = position
            notifyDataSetChanged()
        }
    }

}
