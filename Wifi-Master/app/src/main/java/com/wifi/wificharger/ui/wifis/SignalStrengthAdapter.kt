package com.wifi.wificharger.ui.wifis

import androidx.recyclerview.widget.DiffUtil
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.databinding.ItemSignalStrengthBinding
import com.wifi.wificharger.ui.base.BaseAdapter
import com.wifi.wificharger.ui.base.BaseViewHolder

class SignalStrengthAdapter(
    private val onItemClick: (Wifi) -> Unit
) : BaseAdapter<Wifi, ItemSignalStrengthBinding, BaseViewHolder<ItemSignalStrengthBinding>>(
    ItemSignalStrengthBinding::inflate,
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
        holder: BaseViewHolder<ItemSignalStrengthBinding>,
        binding: ItemSignalStrengthBinding,
        item: Wifi,
        position: Int
    ) {
        with(binding) {
            tvName.text = item.name
            imvIcon.setImageResource(item.icon)
            progressSignalStrength.progress = item.signalStrength
            tvPercent.text = "${item.signalStrength}%"
        }
    }
}
