package com.wifi.wificharger.ui.wifis

import androidx.recyclerview.widget.DiffUtil
import com.wifi.wificharger.data.model.ConnectedWifi
import com.wifi.wificharger.databinding.ItemConnectedDevicesBinding
import com.wifi.wificharger.ui.base.BaseAdapter
import com.wifi.wificharger.ui.base.BaseViewHolder

class ConnectedDevicesAdapter(
    private val onItemClick: (ConnectedWifi) -> Unit
) : BaseAdapter<ConnectedWifi, ItemConnectedDevicesBinding, BaseViewHolder<ItemConnectedDevicesBinding>>(
    ItemConnectedDevicesBinding::inflate,
    object : DiffUtil.ItemCallback<ConnectedWifi>() {
        override fun areItemsTheSame(oldItem: ConnectedWifi, newItem: ConnectedWifi): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ConnectedWifi, newItem: ConnectedWifi): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bindView(
        holder: BaseViewHolder<ItemConnectedDevicesBinding>,
        binding: ItemConnectedDevicesBinding,
        item: ConnectedWifi,
        position: Int
    ) {
        with(binding) {
            tvName.text = item.ipAddress
        }
    }
}