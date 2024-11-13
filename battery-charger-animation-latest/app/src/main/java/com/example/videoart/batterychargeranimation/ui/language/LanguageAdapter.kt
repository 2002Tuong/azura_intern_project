package com.example.videoart.batterychargeranimation.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoart.batterychargeranimation.databinding.ItemViewLanguageBinding
import com.example.videoart.batterychargeranimation.model.LanguageModel

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private val languages = mutableListOf<LanguageModel>()

    var languageSelectedCallback: (Int) -> Unit = {}
    class ViewHolder(val binding: ItemViewLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(languageModel: LanguageModel) {
            binding.imgLanguage.setImageResource(languageModel.idResIcon)
            binding.txtLanguage.setText(languageModel.name)
            binding.icChoose.isSelected = languageModel.isChoose
            binding.ctlItemLanguage.isSelected = languageModel.isChoose
        }
    }

    fun setData(listLanguage: List<LanguageModel>) {
        languages.clear()
        languages.addAll(listLanguage)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemViewLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lang = languages[position]
        holder.bind(lang)

        holder.itemView.setOnClickListener {
            updateView(lang)
            languageSelectedCallback.invoke(position)
        }
    }

    private fun updateView(languageModel: LanguageModel) {
        languages.forEach {
            it.isChoose = it.code == languageModel.code
        }
        notifyDataSetChanged()
    }

    fun itemSelected(): LanguageModel {
        return languages.first { it.isChoose }
    }

    fun itemIndex(): Int {
        return languages.indexOf(itemSelected())
    }

    fun updateView(position: Int) {
        val item = languages[position]
        updateView(item)
    }
}