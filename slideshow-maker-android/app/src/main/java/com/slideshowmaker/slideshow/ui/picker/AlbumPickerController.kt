package com.slideshowmaker.slideshow.ui.picker

import com.airbnb.epoxy.EpoxyController
import com.slideshowmaker.slideshow.data.response.PhotoAlbum
import com.slideshowmaker.slideshow.ui.picker.view.AlbumEpoxyModel_

class AlbumPickerController : EpoxyController() {
    var mediAlbum: List<PhotoAlbum> = emptyList()
    var handleCallback: OnAlbumSelectedListener? = null

    override fun buildModels() {
        mediAlbum.forEach {
            AlbumEpoxyModel_()
                .id(it.id)
                .album(it)
                .listener { model, _, _, _ ->
                    handleCallback?.onAlbumSelected(model.album())
                }
                .addTo(this)
        }
    }

    interface OnAlbumSelectedListener {
        fun onAlbumSelected(album: PhotoAlbum)
    }
}
