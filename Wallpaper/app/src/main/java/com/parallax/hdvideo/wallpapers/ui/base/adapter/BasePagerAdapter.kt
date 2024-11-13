package com.parallax.hdvideo.wallpapers.ui.base.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

abstract class BasePagerAdapter<D: Any, VH : BasePagerAdapter.ViewHolder> : PagerAdapter() {
    val listData = mutableListOf<D>()
    private val cacheVH = mutableListOf<VH>()
    private val attachedVH = mutableMapOf<Int, VH>()
    var keepPosition = -1
    var curPosition = 0
    private var _context: Context? = null
    abstract fun onCreateViewHolder(container: ViewGroup, viewType: Int): VH
    abstract fun onBindViewHolder(holder: VH, position: Int)

    open fun onRecycleViewHolder(holder: VH) {}

    var onClickedItemCallback: ((Int, D) -> Unit)? = null
    open var enabled = true
    open fun getViewHolder(position: Int): VH? {
        return attachedVH[position]
    }

    open fun getItemViewType(position: Int) : Int {
        return 0
    }

    open fun getViewHolder(container: ViewGroup, position: Int): VH {
        val viewHolder = attachedVH[position]
        if (viewHolder != null) return viewHolder
        val itemType = getItemViewType(position)
        val index = cacheVH.indexOfFirst { it.itemViewType == itemType }
        val itemHolder = if (index < 0) {
            onCreateViewHolder(container, viewType = itemType)
        } else {
            cacheVH.removeAt(index)
        }
        itemHolder.itemViewType = itemType
        itemHolder.position = position
        attachedVH[position] = itemHolder
        return itemHolder
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        _context = container.context
        val viewHolder = attachedVH[position] ?: getViewHolder(container, position)
        if (enabled) {
            viewHolder.itemView.setOnClickListener {
                onClickedItemCallback?.invoke(position, listData[position])
            }
        }
        container.addView(viewHolder.itemView, null)
        if (keepPosition == position) {
            keepPosition = -1
        } else {
            onBindViewHolder(viewHolder, position)
        }
        return viewHolder
    }

    @Suppress("UNCHECKED_CAST")
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val viewHolder = obj as? VH ?: return
        container.removeView(viewHolder.itemView)
        if (keepPosition != position) {
            attachedVH.remove(position)
            cacheVH.add(viewHolder)
            onRecycleViewHolder(viewHolder)
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        val viewHolder = obj as ViewHolder
        return viewHolder.itemView === view
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return listData.size
    }

    open fun getData(position: Int): D {
        return listData[position]
    }

    fun setData(data: List<D>){
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: List<D>){
        if (data.isEmpty()) return
        listData.addAll(data)
        notifyDataSetChanged(curPosition)
    }

    fun addData(vararg data: D) {
        if (data.isEmpty()) return
        listData.addAll(data)
        notifyDataSetChanged(curPosition)
    }

    fun remove(position: Int) {
        listData.removeAt(position)
        notifyDataSetChanged()
    }

    fun removeAll() {
        listData.clear()
        notifyDataSetChanged()
    }

    override fun notifyDataSetChanged() {
        keepPosition = -1
        super.notifyDataSetChanged()
    }

    fun notifyDataSetChanged(keepPosition: Int) {
        this.keepPosition = keepPosition
        super.notifyDataSetChanged()
    }



    val listViewHolder: List<VH> get() = attachedVH.values.toList()
    val context get() = _context
    open class ViewHolder(val itemView: View) {
        var itemViewType: Int = 0
        var position: Int = 0
    }
}