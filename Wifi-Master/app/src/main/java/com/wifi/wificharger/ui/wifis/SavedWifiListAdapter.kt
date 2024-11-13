package com.wifi.wificharger.ui.wifis

import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.databinding.ItemLocalWifiBinding
import com.wifi.wificharger.ui.base.BaseAdapter
import com.wifi.wificharger.ui.base.BaseViewHolder
import com.wifi.wificharger.utils.AdsUtils


class SavedWifiListAdapter(
    private val onItemClick: (Wifi) -> Unit
) : BaseAdapter<Wifi, ItemLocalWifiBinding, BaseViewHolder<ItemLocalWifiBinding>>(
    ItemLocalWifiBinding::inflate,
    object : DiffUtil.ItemCallback<Wifi>() {
        override fun areItemsTheSame(oldItem: Wifi, newItem: Wifi): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Wifi, newItem: Wifi): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bindView(
        holder: BaseViewHolder<ItemLocalWifiBinding>,
        binding: ItemLocalWifiBinding,
        item: Wifi,
        position: Int
    ) {
        with(binding) {
            tvName.text = item.name
            tvPassword.isVisible = item.rewardedEarn
            bSearch.isVisible = !item.rewardedEarn
            tvPassword.text = item.password
            imvIcon.setImageResource(item.icon)
            bSearch.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}