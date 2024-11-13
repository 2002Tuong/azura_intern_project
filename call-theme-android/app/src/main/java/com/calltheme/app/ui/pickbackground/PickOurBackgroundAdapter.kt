package com.calltheme.app.ui.pickbackground

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.screentheme.app.R
import com.screentheme.app.models.BackgroundModel

class PickOurBackgroundAdapter(private val backgroundList: ArrayList<BackgroundModel> = ArrayList()) :
    RecyclerView.Adapter<PickOurBackgroundAdapter.BackgroundViewHolder>() {

    var mCallBack: ((position: Int, background: BackgroundModel?) -> Unit)? = null

    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickOurBackgroundAdapter.BackgroundViewHolder {

        val avatarView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_background, parent, false)
        return BackgroundViewHolder(avatarView)
    }

    override fun onBindViewHolder(holder: PickOurBackgroundAdapter.BackgroundViewHolder, position: Int) {

        val context = holder.itemView.context

        if (backgroundList.size <= 0) return

        val background = backgroundList[position]

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
    }

    fun setCallBack(callback: (position: Int, background: BackgroundModel?) -> Unit) {
        this.mCallBack = callback
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
        return backgroundList.size
    }

    inner class BackgroundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundImageView: ImageView = itemView.findViewById(R.id.backgroundImageView)
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
    }
}
