package com.parallax.hdvideo.wallpapers.ui.base.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<D: Any>: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listData = mutableListOf<D>()
    var onClickedItemCallback: ((View, Int, D) -> Unit)? = null
    var onDataChangedCallback : ((Int) -> Unit)? = null
    open var enabled = true

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (enabled) {
            holder.itemView.setOnClickListener {
                onClickedItemCallback?.invoke(holder.itemView, position, listData[position])
            }
        }
    }

    open fun getData(position: Int): D {
        return listData[position]
    }

    open fun getDataOrNull(position: Int): D? {
        return listData.getOrNull(position)
    }

    open fun setData(data: List<D>){
        listData.clear()
        listData.addAll(data)
        onDataChangedCallback?.invoke(data.size)
        notifyDataSetChanged()
    }

    open fun addData(data: List<D>){
        if (data.isNotEmpty()) {
            listData.addAll(data)
            onDataChangedCallback?.invoke(listData.size)
            notifyDataSetChanged()
        }
    }

    open fun addData(vararg data: D) {
        if (data.isNotEmpty()) {
            listData.addAll(data)
            onDataChangedCallback?.invoke(listData.size)
            notifyDataSetChanged()
        }
    }

    open fun remove(position: Int) {
        listData.removeAt(position)
        notifyDataSetChanged()
        onDataChangedCallback?.invoke(listData.size)
    }

    open fun removeAll() {
        listData.clear()
        notifyDataSetChanged()
        onDataChangedCallback?.invoke(listData.size)
    }

    val emptyData: Boolean get() = listData.isEmpty()

}