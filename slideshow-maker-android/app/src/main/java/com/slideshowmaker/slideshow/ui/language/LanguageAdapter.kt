package com.slideshowmaker.slideshow.ui.language

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.slideshowmaker.slideshow.databinding.ItemViewLanguageBinding
import com.slideshowmaker.slideshow.models.LanguageModel

class LanguageAdapter(
    private val context: Context,
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION,
    private val onItemClick: (Int) -> Unit = {}) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var languages: MutableList<LanguageModel> = mutableListOf()
    var onLanguageSelectedCallback: (Boolean) -> Unit = {}
    fun setData(data: List<LanguageModel>) {
        languages.clear()
        languages.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemViewLanguageBinding: ItemViewLanguageBinding) :
        RecyclerView.ViewHolder(itemViewLanguageBinding.root) {
        val layutBinding = itemViewLanguageBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemViewLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val languageModel = languages[position]
        holder.layutBinding.imgLanguage.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                languageModel.idResIcon
            )
        )
        holder.layutBinding.icChoose.isSelected = languageModel.isChoose
        holder.layutBinding.ctlItemLanguage.isSelected = languageModel.isChoose
        holder.layutBinding.txtLanguage.text = context.getString(languageModel.name)
        holder.itemView.setOnClickListener {
            updateView(languageModel)
            onLanguageSelectedCallback.invoke(true)
            selectedItemPosition = position
            onItemClick.invoke(selectedItemPosition)
        }
    }

    private fun updateView(languageModel: LanguageModel) {
        languages.forEach {
            it.isChoose = it.code == languageModel.code
        }
        notifyDataSetChanged()
    }

    fun updateView() {
        if(selectedItemPosition < 0) return
        val languageModel = languages[selectedItemPosition]
        updateView(languageModel)
    }

    fun itemSelected(): LanguageModel {
        return languages.first { it.isChoose }
    }
}