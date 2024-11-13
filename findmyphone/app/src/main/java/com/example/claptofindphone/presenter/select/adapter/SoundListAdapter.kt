package com.example.claptofindphone.presenter.select.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.databinding.ItemSoundBinding
import com.example.claptofindphone.databinding.ItemSoundLargeBinding
import com.example.claptofindphone.presenter.select.model.SoundModel


class SoundListAdapter(
    private var conText: Context,
    private var soundModelList: List<SoundModel>,
    private val onSoundSelect: (SoundModel, Int) -> Unit,
    private var screenType: ScreenType,
    private var selectedIndex: Int = 0
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItem = selectedIndex

    inner class ViewHolder(val binding: ItemSoundBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ViewHolderLarge(val binding: ItemSoundLargeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (screenType == ScreenType.Select) {
            ViewHolder(
                ItemSoundBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ViewHolderLarge(
                ItemSoundLargeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        with(holder) {
            with(soundModelList[position]) {
                if (screenType == ScreenType.Select) {
                    (holder as ViewHolder).binding.imgSoundTypeIcon.setImageResource(this.iconRes)
                    holder.binding.imgSoundTypeIcon.isSelected = false
                    if (selectedItem == position) {
                        holder.binding.imgSoundTypeIcon.isSelected = true
                    }
                    holder.binding.imgSoundTypeIcon.setOnClickListener {
                        val oldItem = selectedItem
                        selectedItem = position
                        notifyItemChanged(oldItem)
                        notifyItemChanged(position)
                        onSoundSelect(soundModelList[position], selectedItem)
                    }
                } else {
                    (holder as ViewHolderLarge).binding.imgSoundTypeIcon.setImageResource(this.iconRes)
                    holder.binding.txtSoundType.text =
                        conText.getString(soundModelList[position].soundNameRes)
                    holder.binding.rltItemSound.isSelected = false
                    if (selectedItem == position) {
                        holder.binding.rltItemSound.isSelected = true
                    }
                    holder.binding.rltItemSound.setOnClickListener {
                        val oldItem = selectedItem
                        selectedItem = position
                        if(checkPositionInValidRange(oldItem)) {
                            notifyItemChanged(oldItem)
                        }
                        notifyItemChanged(position)
                        onSoundSelect(soundModelList[position], selectedItem)
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return soundModelList.size
    }

    private fun checkPositionInValidRange(position: Int): Boolean {
        return position >= 0 && position < soundModelList.size
    }

    enum class ScreenType(val value: Int) {
        FindPhone(1),
        Select(2);

        companion object {
            fun fromInt(value: Int) = values().first { it.ordinal == value }
        }

    }
}