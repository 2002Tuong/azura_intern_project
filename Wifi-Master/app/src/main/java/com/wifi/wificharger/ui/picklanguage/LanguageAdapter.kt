package com.wifi.wificharger.ui.picklanguage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wifi.wificharger.R
import com.wifi.wificharger.data.model.Language

class LanguageAdapter(
    val context: Context,
    private val languageList: ArrayList<Language> = ArrayList(),
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION,
    private val onItemClick: (Int) -> Unit = {}
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    companion object {
        private const val SYSTEM_LANGUAGE_POSITION = 0
    }

    var mCallBack: ((language: Language) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {

        val language = languageList[position]
        val selectionRadioButton: RadioButton =
            holder.itemView.findViewById(R.id.selectionRadioButton)
        selectionRadioButton.isChecked = selectedItemPosition == position

        holder.bind(language, position)

        holder.itemView.setOnClickListener {

            mCallBack?.invoke(language)

            selectedItemPosition = position

            // Notify the adapter of the item changes
            notifyDataSetChanged()
            onItemClick.invoke(selectedItemPosition)
        }

    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    fun onItemClicked(callback: (language: Language) -> Unit) {
        this.mCallBack = callback
    }

    fun updateItems(newLanguages: ArrayList<Language>) {
        languageList.clear()
        languageList.addAll(newLanguages)
        notifyDataSetChanged()
    }

    fun setSelectedLanguage(language: Language) {

        val position: Int = languageList.indexOf(language)

        if (position != -1) {
            val previousSelectedItem = selectedItemPosition
            selectedItemPosition = position
            notifyItemChanged(previousSelectedItem)
            notifyItemChanged(selectedItemPosition)

            this.mCallBack?.invoke(language)
        }

    }

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flagImageView: ImageView = itemView.findViewById(R.id.flagImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(language: Language, position: Int) {
            flagImageView.setImageResource(language.flagResId)
            titleTextView.text = context.getString(language.languageNameId)
        }
    }
}

