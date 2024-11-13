package com.slideshowmaker.slideshow.adapter

import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.FontInfo
import com.slideshowmaker.slideshow.models.FontModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_view_fonts_list.view.*

class FontListAdapter(val callback:(fontId:Int)->Unit) : BaseAdapter<FontModel>() {

    init {
        _itemArray.add(FontModel(FontInfo(R.font.amatics, "Amatics")))
        _itemArray.add(FontModel(FontInfo(R.font.barlow, "Barlow")))
        _itemArray.add(FontModel(FontInfo(R.font.bodoni, "Bodoni")))
        _itemArray.add(FontModel(FontInfo(R.font.ceasar, "Ceasar")))
        _itemArray.add(FontModel(FontInfo(R.font.creepster, "Creepster")))
        _itemArray.add(FontModel(FontInfo(R.font.emilycandy, "Emily Candy")))
        _itemArray.add(FontModel(FontInfo(R.font.forum, "Forum")))
    }
    private var currentFontId = -1
    override fun doGetViewType(position: Int): Int = R.layout.item_view_fonts_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val font = _itemArray[position]

        itemView.fontPreview.typeface = ResourcesCompat.getFont(itemView.context, font.id)
        itemView.fontPreview.text = font.name

        if(font.id == currentFontId) {
            itemView.setBackgroundResource(R.drawable.background_shape_corner_12dp)
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        itemView.setOnClickListener {
            callback.invoke(font.id)
            currentFontId = font.id
            notifyDataSetChanged()
        }
    }
}