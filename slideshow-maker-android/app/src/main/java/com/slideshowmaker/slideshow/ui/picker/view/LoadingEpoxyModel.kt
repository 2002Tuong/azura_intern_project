package com.slideshowmaker.slideshow.ui.picker.view

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.slideshowmaker.slideshow.R

@EpoxyModelClass(layout = R.layout.loading_layout)
abstract class LoadingEpoxyModel : EpoxyModelWithHolder<LoadingEpoxyModel.Holder>() {

    class Holder : EpoxyHolder() {
        override fun bindView(itemView: View) {
        }
    }

    companion object {
        internal const val ID = "LoadingEpoxyModel"
    }
}
