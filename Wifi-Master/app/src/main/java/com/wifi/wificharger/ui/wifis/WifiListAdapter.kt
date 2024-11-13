package com.wifi.wificharger.ui.wifis

import androidx.recyclerview.widget.DiffUtil
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.databinding.ItemWifiBinding
import com.wifi.wificharger.ui.base.BaseAdapter
import com.wifi.wificharger.ui.base.BaseViewHolder

class WifiListAdapter(
    private val onItemClick: (Wifi) -> Unit
) : BaseAdapter<Wifi, ItemWifiBinding, BaseViewHolder<ItemWifiBinding>>(
    ItemWifiBinding::inflate,
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
        holder: BaseViewHolder<ItemWifiBinding>,
        binding: ItemWifiBinding,
        item: Wifi,
        position: Int
    ) {
        with(binding) {
            tvName.text = item.name
            imvIcon.setImageResource(item.icon)
            tvWifiSecureType.text = item.securityType
            btnConnect.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}
