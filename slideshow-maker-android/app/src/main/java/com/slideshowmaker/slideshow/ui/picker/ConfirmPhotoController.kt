package com.slideshowmaker.slideshow.ui.picker

import com.airbnb.epoxy.EpoxyController
import com.slideshowmaker.slideshow.data.response.SnapImage
import com.slideshowmaker.slideshow.ui.picker.view.ConfirmMediaEpoxyModel_
import java.util.Collections

class ConfirmPhotoController : EpoxyController() {

    var images: MutableList<SnapImage> = mutableListOf()

    var handleCallback: OnItemSelectedListener? = null

    override fun buildModels() {
        images.forEach {
            ConfirmMediaEpoxyModel_()
                .id(it.toString())
                .imageUri(it.uri)
                .clickListener { _, _, _, position ->
                    images.removeAt(position)
                    requestModelBuild()
                    handleCallback?.syncSelectedMedias(images)
                }
                .addTo(this)
        }
    }

    fun updateUri(uri: SnapImage) {
        if (images.contains(uri))
            images.remove(uri)
        else
            images.add(uri)
        requestModelBuild()
    }

    fun clear() {
        images.clear()
        handleCallback?.syncSelectedMedias(emptyList())
        requestModelBuild()
    }

    fun swapModel(fromPosition: Int, toPosition: Int) {
        Collections.swap(images, fromPosition, toPosition)
    }


    interface OnItemSelectedListener {
        fun syncSelectedMedias(uris: List<SnapImage>)
    }
}