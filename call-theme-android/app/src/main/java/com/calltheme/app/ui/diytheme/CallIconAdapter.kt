package com.calltheme.app.ui.diytheme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.screentheme.app.R
import com.screentheme.app.models.CallIconModel

class CallIconAdapter(private val callIconList: ArrayList<CallIconModel> = ArrayList()) : RecyclerView.Adapter<CallIconAdapter.CallIconViewHolder>() {

    var mCallBack: ((callIcon: CallIconModel) -> Unit)? = null

    inner class CallIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val acceptIconImageView: ImageView = itemView.findViewById(R.id.acceptIconImageView)
        private val declineIconImageView: ImageView = itemView.findViewById(R.id.declineIconImageView)

        fun bind(callIcon: CallIconModel) {

            Glide.with(itemView)
                .load(callIcon.accept_call_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(acceptIconImageView)

            Glide.with(itemView)
                .load(callIcon.decline_call_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(declineIconImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallIconViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_call_icon, parent, false)
        return CallIconViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CallIconViewHolder, position: Int) {

        val callIcon = callIconList[position]

        holder.itemView.setOnClickListener {
            this.mCallBack?.invoke(callIcon)
        }
        holder.bind(callIconList[position])
    }

    fun setCallBack(callback: (callIcon: CallIconModel) -> Unit) {
        this.mCallBack = callback
    }

    fun updateItems(newCallIcons: ArrayList<CallIconModel>) {
        callIconList.clear()
        callIconList.addAll(newCallIcons)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return callIconList.size
    }
}