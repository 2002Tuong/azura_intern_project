package com.example.videoart.batterychargeranimation.ui.mytheme

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.videoart.batterychargeranimation.databinding.ItemListGalleryBinding
import com.example.videoart.batterychargeranimation.model.Theme

class MyThemeAdapter(
    private val themeList: MutableList<Theme> = mutableListOf(),
    private var mode: Mode = Mode.NORMAL,
    private var currentThemeId: String = ""
) : RecyclerView.Adapter<MyThemeAdapter.ViewHolder>() {

    private var itemCallback: (Theme) -> Unit = {}
    private var onDeleteCallback: (Theme) -> Unit = {}
    private var selectItem: MutableList<Boolean> = themeList.map { false }.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyThemeAdapter.ViewHolder {
        val binding = ItemListGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.thumbnail.layoutParams.apply {
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyThemeAdapter.ViewHolder, position: Int) {
        val item = themeList[position]
        Log.d("Theme", "${item}")
        Log.d("Theme", "${currentThemeId}")
        Log.d("Theme", "Mode: ${mode}")
        holder.bind(item, currentThemeId, position)
    }

    override fun getItemCount(): Int {
        return themeList.size
    }

    fun setItemCallback(callback: (Theme) -> Unit) {
        itemCallback = callback
    }

    fun setDeleteCallback(callback: (Theme) -> Unit) {
        onDeleteCallback = callback
    }

    fun updateData(newList: List<Theme>) {
        themeList.clear()
        themeList.addAll(newList)
        selectItem = themeList.map { false }.toMutableList()
        notifyDataSetChanged()
    }

    fun updateCurrent(currentTheme: String) {
        currentThemeId = currentTheme
        notifyDataSetChanged()
    }

    fun changeMode(newMode: Mode) {
        mode = newMode
        notifyDataSetChanged()
    }

    fun onClickItem(position: Int) {
        selectItem[position] = !selectItem[position]
        notifyDataSetChanged()
    }

    fun clearAllSelect() {
        selectItem = selectItem.map { false }.toMutableList()
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectItem = selectItem.map { true }.toMutableList()
        notifyDataSetChanged()
    }

    fun sortList() {
        themeList.reverse()
        notifyDataSetChanged()
    }

    fun getDeleteList(): List<Theme> {
        val res = mutableListOf<Theme>()
        for(item in themeList) {
            val itemIndex = themeList.indexOf(item)
            if(itemIndex < selectItem.size && selectItem[itemIndex]) {
                res.add(item)
            }
        }
        return res
    }

    inner class ViewHolder(val binding: ItemListGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(theme: Theme, themeId: String, position: Int) {
            Glide.with(context)
                .load(theme.thumbnail)
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(binding.thumbnail)

            if(theme.id == themeId) {
                binding.currentItem.isVisible = true
            }else {
                binding.currentItem.isVisible = false
            }

            if(mode != Mode.NORMAL) {
                binding.icChoose.isVisible = true
                binding.root.setOnClickListener {
                    onClickItem(position)
                }
            }else {
                binding.icChoose.isVisible = false
                binding.root.setOnClickListener {
                    itemCallback.invoke(theme)
                }
            }

            binding.icChoose.isSelected = selectItem[position]
        }
    }
}

