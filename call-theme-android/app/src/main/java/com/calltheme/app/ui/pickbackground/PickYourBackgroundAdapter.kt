package com.calltheme.app.ui.pickbackground

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.screentheme.app.R
import com.screentheme.app.models.BackgroundModel

class PickYourBackgroundAdapter(private val backgroundList: ArrayList<BackgroundModel> = ArrayList()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_BACKGROUND = 1
    }

    var mCallBack: ((position: Int, background: BackgroundModel?) -> Unit)? = null
    private var removeCallback: ((background: BackgroundModel) -> Unit)? = null

    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val headerView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_pick_your_background, parent, false)
                HeaderViewHolder(headerView)
            }

            VIEW_TYPE_BACKGROUND -> {
                val avatarView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_pick_your_background, parent, false)
                BackgroundViewHolder(avatarView)
            }

            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val context = holder.itemView.context

        when (holder) {
            is HeaderViewHolder -> {

                holder.backgroundImageView.scaleType = ImageView.ScaleType.FIT_XY
                Glide.with(holder.itemView.context)
                    .load(R.drawable.icon_add_background)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.backgroundImageView);

                holder.itemView.setOnClickListener {
                    mCallBack?.invoke(position, null)
                }
                return
            }

            is BackgroundViewHolder -> {

                if (backgroundList.size <= 0) return

                val background = backgroundList[position - 1]

                holder.itemView.setOnClickListener {
                    mCallBack?.invoke(position, background)

                    // Update the selected item position
                    val previousSelectedItem = selectedItemPosition

                    if (selectedItemPosition == position) {
                        // Clicked on the selected item, change background to white
                        selectedItemPosition = RecyclerView.NO_POSITION
                    } else {
                        // Clicked on a different item, update selected item position
                        selectedItemPosition = position
                    }

                    // Notify the adapter of the item changes
                    notifyItemChanged(previousSelectedItem)
                    notifyItemChanged(selectedItemPosition)
                }

                if (selectedItemPosition == position) {
                    holder.cardView.strokeWidth = 4
                    holder.cardView.strokeColor = context.resources.getColor(R.color.colorPrimary)
                } else {
                    holder.cardView.strokeWidth = 2
                    holder.cardView.strokeColor = context.resources.getColor(R.color.purple_50)
                }

                holder.backgroundImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(holder.backgroundImageView)
                    .load(background.background)
                    .placeholder(R.drawable.background_call_placeholder)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.backgroundImageView)

                holder.removeButton.visibility = View.VISIBLE
                holder.removeButton.setOnClickListener {
                    this.removeCallback?.invoke(background)
                }
            }
        }


    }

    fun setCallBack(callback: (position: Int, background: BackgroundModel?) -> Unit) {
        this.mCallBack = callback
    }

    fun onRemoveItem(callback: (background: BackgroundModel) -> Unit) {
        this.removeCallback = callback
    }

    fun deSelect() {
        selectedItemPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun updateItems(newThemes: ArrayList<BackgroundModel>) {
        backgroundList.clear()
        backgroundList.addAll(newThemes)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return backgroundList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_BACKGROUND
        }
    }

    inner class BackgroundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundImageView: ImageView = itemView.findViewById(R.id.backgroundImageView)
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundImageView: ImageView = itemView.findViewById(R.id.backgroundImageView)
    }
}
