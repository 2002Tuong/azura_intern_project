package com.slideshowmaker.slideshow.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter <T> : RecyclerView.Adapter<BaseViewHolder>() {
    protected val _itemArray = ArrayList<T>()
    val itemArray get() = _itemArray

    protected var _curItem:T? = null
    val curItem get() = _curItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return BaseViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return _itemArray.size
    }

    open fun setItemList(arrayList: ArrayList<T>) {
        _itemArray.clear()
        _itemArray.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun addItem(item:T) {
        _itemArray.add(item)
        notifyDataSetChanged()
    }

    fun addItems(items:List<T>){
        _itemArray.addAll(items)
        notifyDataSetChanged()
    }
    fun clear() {
        _itemArray.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = doGetViewType(position)

    abstract fun doGetViewType(position:Int):Int

}