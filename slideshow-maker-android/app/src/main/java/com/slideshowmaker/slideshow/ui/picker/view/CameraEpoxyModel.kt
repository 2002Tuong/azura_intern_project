package com.slideshowmaker.slideshow.ui.picker.view

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.slideshowmaker.slideshow.R

@EpoxyModelClass(layout = R.layout.view_camera)
abstract class CameraEpoxyModel : EpoxyModelWithHolder<CameraEpoxyModel.Holder>() {

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var clickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.root.setOnClickListener(clickListener)
    }

    class Holder : EpoxyHolder() {
        lateinit var root: View
        override fun bindView(itemView: View) {
            root = itemView
        }
    }

    companion object {
        internal const val ID = "CameraModelView"
    }
}
