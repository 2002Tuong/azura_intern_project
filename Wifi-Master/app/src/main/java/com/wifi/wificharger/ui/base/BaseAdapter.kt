package com.wifi.wificharger.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<Item, VB : ViewBinding, VH: BaseViewHolder<VB>>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    callback: DiffUtil.ItemCallback<Item>
) : ListAdapter<Item, VH>(callback) {

    private var _viewBinding: VB? = null
    protected lateinit var viewBinding: VB

    override fun submitList(list: List<Item>?) {
        super.submitList(ArrayList<Item>(list ?: listOf()))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        _viewBinding = inflate(LayoutInflater.from(parent.context), parent, false)
        viewBinding = requireNotNull(_viewBinding)
        return getViewHolder(viewBinding).apply { bindFirstTime(viewBinding) }
    }

    @Suppress("UNCHECKED_CAST")
    open fun getViewHolder(binding: VB) : VH {
        return BaseViewHolder(binding) as VH
    }

    fun addData(newItem: List<Item>) {
        submitList(currentList + newItem)
    }

    protected open fun bindFirstTime(binding: ViewBinding) = Unit

    override fun onBindViewHolder(holder: VH, position: Int) {
        currentList[position]?.let { item ->
            bindView(holder, holder.binding, item, position)
        }
    }
    abstract fun bindView(holder: VH, binding: VB, item: Item, position: Int)
}

open class BaseViewHolder<VB : ViewBinding>(
    val binding: VB
) : RecyclerView.ViewHolder(binding.root)
