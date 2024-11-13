package com.slideshowmaker.slideshow.ui.picker

import com.airbnb.epoxy.EpoxyController
import com.slideshowmaker.slideshow.data.response.SnapImage
import com.slideshowmaker.slideshow.ui.picker.view.SelectedMediaEpoxyModel_
import java.util.Collections

class SelectedMediaController() : EpoxyController() {

    var snapImages: MutableList<SnapImage> = mutableListOf()

    var handleCallback: OnItemSelectedListener? = null

    override fun buildModels() {
        snapImages.forEach {
            SelectedMediaEpoxyModel_()
                .id(it.toString())
                .imageUri(it.uri)
                .clickListener { _, _, _, position ->
                    snapImages.removeAt(position)
                    requestModelBuild()
                    handleCallback?.syncSelectedMedias(snapImages)
                }
                .addTo(this)
        }
    }

    fun updateUri(uri: SnapImage) {
        if (snapImages.contains(uri))
            snapImages.remove(uri)
        else
            snapImages.add(uri)
        requestModelBuild()
    }

    fun clear() {
        snapImages.clear()
        handleCallback?.syncSelectedMedias(emptyList())
        requestModelBuild()
    }

    fun swapModel(fromPosition: Int, toPosition: Int) {
        Collections.swap(snapImages, fromPosition, toPosition)
    }


    interface OnItemSelectedListener {
        fun syncSelectedMedias(uris: List<SnapImage>)
    }
}