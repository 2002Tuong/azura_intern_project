package com.calltheme.app.ui.edittheme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.screentheme.app.R
import com.screentheme.app.models.CallIconModel

class CallIconInEditAdapter(private val callIcons: ArrayList<CallIconModel> = ArrayList()) :
    RecyclerView.Adapter<CallIconInEditAdapter.CallIconViewHolder>() {

    var mCallBack: ((callIcon: CallIconModel) -> Unit)? = null

    private var selectedItemPosition = RecyclerView.NO_POSITION

    inner class CallIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val acceptIconImageView: ImageView = itemView.findViewById(R.id.acceptIconImageView)

        fun bind(callIcon: CallIconModel) {

            Glide.with(itemView)
                .load(callIcon.accept_call_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(acceptIconImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallIconViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_call_icon_in_edit, parent, false)
        return CallIconViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CallIconViewHolder, position: Int) {

        val callIcon = callIcons[position]

        holder.bind(callIcon)

        holder.itemView.setOnClickListener {

            this.mCallBack?.invoke(callIcon)

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
            holder.itemView.setBackgroundResource(R.drawable.select_call_icon_in_edit_bg)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.unselect_call_icon_in_edit_bg)
        }
    }

    fun setCallBack(callback: (callIcon: CallIconModel) -> Unit) {
        this.mCallBack = callback
    }

    fun updateItems(newCallIcons: ArrayList<CallIconModel>) {
        callIcons.clear()
        callIcons.addAll(newCallIcons)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return callIcons.size
    }
}