package com.slideshowmaker.slideshow.ui.picker.view

import android.net.Uri
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.slideshowmaker.slideshow.R

@EpoxyModelClass(layout = R.layout.epoxy_view_snap_selected_media_view)
abstract class SelectedMediaEpoxyModel : EpoxyModelWithHolder<SelectedMediaEpoxyModel.Holder>() {
    @EpoxyAttribute
    lateinit var imageUri: Uri

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var clickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        Glide.with(holder.image).load(imageUri)
            .placeholder(R.drawable.icon_image_place_holder)
            .into(holder.image)
        holder.removeBtn.setOnClickListener(clickListener)
    }

    class Holder : EpoxyHolder() {
        lateinit var image: ImageView
        lateinit var removeBtn: ImageButton
        override fun bindView(itemView: View) {
            image = itemView.findViewById(R.id.ivImage)
            removeBtn = itemView.findViewById(R.id.ibRemove)
        }
    }

    companion object {
        internal const val ID = "SnapImageModelView"
    }
}
