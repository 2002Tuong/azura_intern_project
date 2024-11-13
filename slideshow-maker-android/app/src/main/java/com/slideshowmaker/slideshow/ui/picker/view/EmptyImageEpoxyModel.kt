package com.slideshowmaker.slideshow.ui.picker.view

import android.view.View
import android.widget.LinearLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_empty_image_layout)
abstract class EmptyImageEpoxyModel : EpoxyModelWithHolder<EmptyImageEpoxyModel.Holder>() {

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var clickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.ibAddPhoto.setOnClickListener(clickListener)
    }

    class Holder : KotlinEpoxyHolder() {
        val ibAddPhoto: LinearLayout by bind(R.id.vAddPhoto)
    }

    companion object {
        internal const val ID = "EmptyImageEpoxyModel"
    }
}
