package com.slideshowmaker.slideshow.ui.home.models

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.response.SuggestedAppConfig
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder
import com.slideshowmaker.slideshow.utils.extentions.openGooglePlayApp

@EpoxyModelClass(layout = R.layout.epoxy_view_suggested_app)
abstract class SuggestedAppEpoxyModel : EpoxyModelWithHolder<SuggestedAppEpoxyModel.Holder>() {
    @EpoxyAttribute
    lateinit var app: SuggestedAppConfig

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var listener: View.OnClickListener? = null
    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.tvTitle.text = app.name
        Glide.with(holder.ivThumbnail).load(app.logoUrl)
            .placeholder(R.drawable.icon_image_place_holder)
            .into(holder.ivThumbnail)
        holder.root.setOnClickListener {
            it.context.openGooglePlayApp(app.packageId.orEmpty())
        }
    }

    class Holder : KotlinEpoxyHolder() {
        val tvTitle by bind<TextView>(R.id.tvName)
        val ivThumbnail by bind<ImageView>(R.id.ivThumbnail)
        val root by bind<LinearLayout>(R.id.rootLayout)
    }

    companion object {
        internal const val ID = "SuggestedAppEpoxyModel"
    }
}
