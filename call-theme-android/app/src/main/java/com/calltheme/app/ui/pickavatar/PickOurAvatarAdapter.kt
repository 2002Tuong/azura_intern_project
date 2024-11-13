package com.calltheme.app.ui.pickavatar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.screentheme.app.R
import com.screentheme.app.models.AvatarModel
import de.hdodenhof.circleimageview.CircleImageView

class PickOurAvatarAdapter(
    private val avatarList: ArrayList<AvatarModel> = ArrayList(), private var showHeader: Boolean = true,
    private var itemAvatarRes: Int = R.layout.item_layout_avatar
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_AVATAR = 1
    }

    var mCallBack: ((position: Int, avatar: AvatarModel?) -> Unit)? = null

    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val headerView = LayoutInflater.from(parent.context).inflate(itemAvatarRes, parent, false)
                HeaderViewHolder(headerView)
            }

            VIEW_TYPE_AVATAR -> {
                val avatarView = LayoutInflater.from(parent.context).inflate(itemAvatarRes, parent, false)
                AvatarViewHolder(avatarView)
            }

            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val context = holder.itemView.context

        when (holder) {
            is HeaderViewHolder -> {
                holder.avatarImageView.setBackgroundResource(R.drawable.icon_add_avatar)

                holder.itemView.setOnClickListener {
                    this.mCallBack?.invoke(position, null)
                }
            }

            is AvatarViewHolder -> {

                if (avatarList.size <= 0) {
                    return
                }

                val avatar = avatarList[position - if (showHeader) 1 else 0]

                holder.itemView.setOnClickListener {
                    this.mCallBack?.invoke(position, avatar)

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
                    holder.avatarImageView.borderWidth = 4
                    holder.avatarImageView.borderColor = context.resources.getColor(R.color.colorPrimary)
                } else {
                    holder.avatarImageView.borderWidth = 0
                }

                Glide.with(holder.avatarImageView)
                    .load(avatar.avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.avatarImageView)
            }
        }

    }

    fun setCallBack(callback: (position: Int, avatar: AvatarModel?) -> Unit) {
        this.mCallBack = callback
    }

    fun deSelect() {
        selectedItemPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun updateItems(newAvatars: ArrayList<AvatarModel>) {
        avatarList.clear()
        avatarList.addAll(newAvatars)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return avatarList.size + if (showHeader) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && showHeader) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_AVATAR
        }
    }

    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: CircleImageView = itemView.findViewById(R.id.avatarImageView)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
    }
}
