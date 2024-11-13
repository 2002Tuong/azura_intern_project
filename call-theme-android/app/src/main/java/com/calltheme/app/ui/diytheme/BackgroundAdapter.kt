package com.calltheme.app.ui.diytheme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.screentheme.app.R
import com.screentheme.app.models.BackgroundModel

class BackgroundAdapter(private val listOfBackground: ArrayList<BackgroundModel> = ArrayList()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_BACKGROUND = 1
    }

    var mCallBack: ((position: Int, background: BackgroundModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val headerView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_background, parent, false)
                HeaderViewHolder(headerView)
            }

            VIEW_TYPE_BACKGROUND -> {
                val avatarView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_background, parent, false)
                BackgroundViewHolder(avatarView)
            }

            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is HeaderViewHolder -> {

                holder.backgroundView.scaleType = ImageView.ScaleType.FIT_XY
                Glide.with(holder.itemView.context)
                    .load(R.drawable.icon_add_background)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.backgroundView);

                holder.itemView.setOnClickListener {
                    mCallBack?.invoke(position, null)
                }
                return
            }

            is BackgroundViewHolder -> {

                if (listOfBackground.size <= 0) return

                val background = listOfBackground[position - 1]

                holder.itemView.setOnClickListener {
                    mCallBack?.invoke(position, background)
                }

                holder.backgroundView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(holder.backgroundView)
                    .load(background.background)
                    .placeholder(R.drawable.background_call_placeholder)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.backgroundView)
            }
        }


    }

    fun setCallBack(callback: (position: Int, background: BackgroundModel?) -> Unit) {
        this.mCallBack = callback
    }

    fun updateItems(newThemes: ArrayList<BackgroundModel>) {
        listOfBackground.clear()
        listOfBackground.addAll(newThemes)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listOfBackground.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_BACKGROUND
        }
    }

    inner class BackgroundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundView: ImageView = itemView.findViewById(R.id.backgroundImageView)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundView: ImageView = itemView.findViewById(R.id.backgroundImageView)
    }
}
