package com.parallax.hdvideo.wallpapers.ui.language

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.data.model.LanguageModel
import com.parallax.hdvideo.wallpapers.databinding.ItemLanguageViewBinding

class LanguageAdapter(private val context: Context) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private var languageModelList: MutableList<LanguageModel> = mutableListOf()
    private var onLanguageChangedCallback: (String) -> Unit = {}
    fun setData(data: List<LanguageModel>) {
        languageModelList.clear()
        languageModelList.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemViewLanguageBinding: ItemLanguageViewBinding) :
        RecyclerView.ViewHolder(itemViewLanguageBinding.root) {
        val itemBinding = itemViewLanguageBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemLanguageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return languageModelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languageModelList[position]
        holder.itemBinding.imgLanguage.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                language.idResIcon
            )
        )
        holder.itemBinding.icChoose.isSelected = language.isChosen
        holder.itemBinding.ctlItemLanguage.isSelected = language.isChosen
        holder.itemBinding.txtLanguage.text = context.getString(language.languageName)
        holder.itemView.setOnClickListener {
            updateView(language)
            onLanguageChangedCallback(itemSelected().languageCode)
        }
    }

    private fun updateView(languageModel: LanguageModel) {
        languageModelList.forEach {
            it.isChosen = it.languageCode == languageModel.languageCode
        }
        notifyDataSetChanged()
    }

    fun itemSelected(): LanguageModel {
        return languageModelList.first { it.isChosen }
    }

    fun setOnLanguageChange(handle: (String) -> Unit) {
        onLanguageChangedCallback = handle
    }
}