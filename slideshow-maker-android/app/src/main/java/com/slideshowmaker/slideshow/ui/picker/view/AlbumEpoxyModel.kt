package com.slideshowmaker.slideshow.ui.picker.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.response.PhotoAlbum
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_album_layout)
abstract class AlbumEpoxyModel : EpoxyModelWithHolder<AlbumEpoxyModel.Holder>() {
    @EpoxyAttribute
    lateinit var album: PhotoAlbum

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var listener: View.OnClickListener? = null
    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.tvTitle.text = album.name
        holder.tvCount.text = album.imageCount.toString()
        holder.root.setOnClickListener(listener)
        Glide.with(holder.ivThumbnail).load(album.thumbnailUri)
            .placeholder(R.drawable.icon_image_place_holder)
            .into(holder.ivThumbnail)
    }

    class Holder : KotlinEpoxyHolder() {
        val tvTitle by bind<TextView>(R.id.tvName)
        val ivThumbnail by bind<ImageView>(R.id.ivThumbnail)
        val tvCount by bind<TextView>(R.id.tvCount)
        val root by bind<ConstraintLayout>(R.id.rootLayout)
    }

    companion object {
        internal const val ID = "AlbumEpoxyModel"
    }
}
