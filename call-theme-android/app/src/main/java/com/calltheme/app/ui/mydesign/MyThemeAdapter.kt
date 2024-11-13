package com.calltheme.app.ui.mydesign

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.screentheme.app.R
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.helpers.ThemeManager
import com.screentheme.app.utils.helpers.dummyContacts
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyThemeAdapter(
    private val context: Context,
    private val themeConfigs: ArrayList<ThemeConfig> = ArrayList(),
    private var showDeleteButton: Boolean = false,
) :
    RecyclerView.Adapter<MyThemeAdapter.ViewHolder>() {

    var mCallBack: ((themeConfig: ThemeConfig) -> Unit)? = null
    var mOnDeleteCallBack: ((themeConfig: ThemeConfig) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_my_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themeConfigs[position]
        holder.bind(theme)
    }

    override fun getItemCount(): Int {
        return themeConfigs.size
    }

    fun setCallBack(callback: (themeConfig: ThemeConfig) -> Unit) {
        this.mCallBack = callback
    }

    fun setOnDeleteItem(callback: (themeConfig: ThemeConfig) -> Unit) {
        this.mOnDeleteCallBack = callback
    }

    fun updateItems(newThemes: ArrayList<ThemeConfig>) {
        themeConfigs.clear()
        themeConfigs.addAll(newThemes)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), KoinComponent {

        val context: Context = itemView.context
        private val themeManager: ThemeManager by inject()

        private val backgroundImageView: ImageView = itemView.findViewById(R.id.call_background)
        private val acceptImageView: ImageView = itemView.findViewById(R.id.call_accept)
        private val endImageView: ImageView = itemView.findViewById(R.id.call_end)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.caller_avatar)
        private val callerName: TextView = itemView.findViewById(R.id.caller_name_label)
        private val callerNumber: TextView = itemView.findViewById(R.id.caller_number)
        private val applyThemeButton: AppCompatButton = itemView.findViewById(R.id.applyThemeButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(themeConfig: ThemeConfig) {

            itemView.setOnClickListener {
                mCallBack?.invoke(themeConfig)
            }

            if (showDeleteButton) {
                deleteButton.visibility = View.VISIBLE
            } else {
                deleteButton.visibility = View.GONE
            }
            deleteButton.setOnClickListener {
                mOnDeleteCallBack?.invoke(themeConfig)
            }

            val currentThemeId = themeManager.currentThemeId

            if (themeConfig.id == currentThemeId) {
                applyThemeButton.visibility = View.VISIBLE
            } else {
                applyThemeButton.visibility = View.GONE
            }

            val contact = dummyContacts.random()

            val contactName = contact["contact_name"]
            val phoneNumber = contact["phone_number"]

            if (themeConfig.id == ThemeManager.DEFAULT_THEME_ID) {
                callerName.text = "Sample"
            } else {
                callerName.text = contactName
            }

            callerNumber.text = phoneNumber

            Glide.with(context)
                .load(themeConfig.background)
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(backgroundImageView)

            Glide.with(context)
                .load(themeConfig.avatar)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .fitCenter()
                .into(avatarImageView)

            Glide.with(context)
                .load(themeConfig.declineCallIcon)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(endImageView)

            Glide.with(context)
                .load(themeConfig.acceptCallIcon)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(acceptImageView)
        }
    }
}