package com.wifi.wificharger.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.wifi.wificharger.data.model.Feature
import com.wifi.wificharger.databinding.ItemFeatureBinding
import com.wifi.wificharger.ui.base.BaseAdapter
import com.wifi.wificharger.ui.base.BaseViewHolder

class HomeAdapter(
    private val onItemClick: (Feature) -> Unit
): BaseAdapter<Feature, ItemFeatureBinding, BaseViewHolder<ItemFeatureBinding>>(
    ItemFeatureBinding::inflate,
    object : DiffUtil.ItemCallback<Feature>() {
        override fun areItemsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bindView(
        holder: BaseViewHolder<ItemFeatureBinding>,
        binding: ItemFeatureBinding,
        item: Feature,
        position: Int
    ) {
        with(binding) {
            tvName.text = item.name.title
            imvIcon.setImageResource(item.icon)
            root.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}