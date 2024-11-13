package com.parallax.hdvideo.wallpapers.ui.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

open class BaseAdapterList<D: Any, V: ViewDataBinding>(var layoutId: ((position: Int) -> Int)? = null,
                                                       var onBind: ((binding: V, data: D, position: Int) -> Unit)? = null) :
    RecyclerView.Adapter<BaseAdapterList.BaseViewHolder<D, V>>() {

    private var listData = mutableListOf<D>()
    var onClickedItemcallBack: ((Int, D) -> Unit)? = null
    open var enabled = true
    private var mRecyclerView: WeakReference<RecyclerView>? = null

    override fun getItemCount(): Int = listData.size

    override fun getItemViewType(position: Int): Int {
        return layoutId?.invoke(position) ?: 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<D, V> {
        val inflaterInstance = LayoutInflater.from(parent.context)
        val binding: V = DataBindingUtil.inflate(inflaterInstance, viewType, parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<D, V>, position: Int) {
        val item = listData[position]
        holder.onBind(item)
        onBind?.invoke(holder.dataBinding, item, position)
        if (enabled) {
            holder.itemView.setOnClickListener {
                onClickedItemcallBack?.invoke(position, item)
            }
        }
    }

    open class BaseViewHolder<D: Any, V: ViewDataBinding>(open val dataBinding: V) : RecyclerView.ViewHolder(dataBinding.root) {

        open fun onBind(data: D) {

        }
    }

    @Suppress("UNCHECKED_CAST")
    fun findViewHolder(position: Int): BaseViewHolder<D, V>? {
        return mRecyclerView?.get()?.findViewHolderForAdapterPosition(position) as? BaseViewHolder<D, V>
    }

    open fun setData(data: List<D>){
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    open fun addData(vararg data: D) {
        listData.addAll(data)
        notifyDataSetChanged()
    }

    fun indexOf(data : D) : Int {
        return listData.indexOf(data)
    }

    open fun addData(data: List<D>) {
        listData.addAll(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        listData.removeAt(position)
        notifyDataSetChanged()
    }

    fun getData(position: Int) = listData[position]

    fun getDataOrNull(position: Int) = listData.getOrNull(position)

    fun removeAll() {
        listData.clear()
        notifyDataSetChanged()
    }

    fun getListData() = listData

    val dataSize get() = listData.size

    val dataEmpty get() =  listData.isEmpty()

    var onScrolledCallback : ((RecyclerView, Int, Int) -> Unit)? = null

    var onScrollStateChangedCallback : ((RecyclerView, Int) -> Unit)? = null

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if(dx != 0 || dy != 0) {
                onScrolledCallback?.invoke(recyclerView, dx, dy)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
           onScrollStateChangedCallback?.invoke(recyclerView, newState)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.mRecyclerView = WeakReference(recyclerView)
        mRecyclerView!!.get()?.addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickedItemcallBack = null
        onBind = null
        layoutId = null
        mRecyclerView?.clear()
        mRecyclerView = null
    }
}

