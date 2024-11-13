package com.slideshowmaker.slideshow.ui.picker.view

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.response.SnapImage

@EpoxyModelClass(layout = R.layout.epoxy_view_snap_image_view)
abstract class SnapImageEpoxyModel : EpoxyModelWithHolder<SnapImageEpoxyModel.Holder>() {
    @EpoxyAttribute
    lateinit var imageUri: Uri

    @EpoxyAttribute
    lateinit var media:SnapImage

    @EpoxyAttribute
    var selected: Boolean = false

    @EpoxyAttribute
    var index: Int = -1

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var clickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        Glide.with(holder.image).load(imageUri)
            .placeholder(R.drawable.icon_image_place_holder)
            .into(holder.image)
        holder.image.setOnClickListener(clickListener)
        holder.index.text = index.toString()
        holder.container.setBackgroundResource(if (selected) R.color.transparent else R.color.greyscale_a40)
        holder.circle.isVisible = !selected
        holder.index.isVisible = selected
    }

    class Holder : EpoxyHolder() {
        lateinit var image: ImageView
        lateinit var index: TextView
        lateinit var circle: View
        lateinit var container: ConstraintLayout
        override fun bindView(itemView: View) {
            container = itemView.findViewById(R.id.container)
            image = itemView.findViewById(R.id.ivImage)
            index = itemView.findViewById(R.id.tvIndex)
            circle = itemView.findViewById(R.id.vCircle)
        }
    }

    companion object {
        internal const val ID = "SnapImageModelView"
    }
}
