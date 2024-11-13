package com.slideshowmaker.slideshow.ui.picker

import androidx.paging.PagingData
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging3.PagingDataEpoxyController
import com.slideshowmaker.slideshow.data.response.PhotoAlbum
import com.slideshowmaker.slideshow.data.response.SnapImage
import com.slideshowmaker.slideshow.ui.picker.view.*

class ImagePickerController(
    private val listener: OnItemSelectedListener,
) : PagingDataEpoxyController<SnapImage>() {
    var photoAlbum: PhotoAlbum? = null
    var curTotalItem: Int = 0
        private set
    var selectedImages = mutableListOf<SnapImage>()

    override fun buildItemModel(currentPosition: Int, item: SnapImage?): EpoxyModel<*> {
        return if (item != null) {
            SnapImageEpoxyModel_()
                .imageUri(item.uri)
                .id(item.id)
                .media(item)
                .index(selectedImages.indexOf(item) + 1)
                .selected(selectedImages.contains(item))
                .clickListener { model, _, _, _ ->
                    listener.onImageSelected(
                        model.media(),
                        false,
                    )
                }
        } else {
            LoadingEpoxyModel_()
                .id(LoadingEpoxyModel.ID)
        }
    }

    override suspend fun submitData(pagingData: PagingData<SnapImage>) {
        super.submitData(pagingData)
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        curTotalItem = models.size
        val newEpoxyModels = mutableListOf<EpoxyModel<*>>()
        if (models.isEmpty()) {
            newEpoxyModels.add(
                EmptyImageEpoxyModel_()
                    .id(EmptyImageEpoxyModel.ID)
                    .spanSizeOverride { _, _, _ -> FULL_ROW_SPAN_COUNT }
                    .clickListener { _, _, _, _ -> listener.onCameraSelected() },
            )
        }
        newEpoxyModels.addAll(models)
        super.addModels(newEpoxyModels.distinct())
    }

    fun toggleSelectedImage(uri: SnapImage) {
        if (selectedImages.contains(uri))
            selectedImages.remove(uri)
        else selectedImages.add(uri)
        requestForcedModelBuild()
    }

    fun syncSelectedMedias(uris: List<SnapImage>) {
        selectedImages = uris.toMutableList()
        requestForcedModelBuild()
    }

    fun clear() {
        selectedImages.clear()
    }

    interface OnItemSelectedListener {
        fun onImageSelected(uri: SnapImage, isSample: Boolean)
        fun onCameraSelected()
    }

    companion object {
        private const val FULL_ROW_SPAN_COUNT = 3

    }
}
