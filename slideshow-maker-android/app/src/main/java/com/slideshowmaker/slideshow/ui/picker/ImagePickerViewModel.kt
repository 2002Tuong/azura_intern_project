package com.slideshowmaker.slideshow.ui.picker

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.slideshowmaker.slideshow.data.ImageRepository
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import java.io.File
import com.slideshowmaker.slideshow.data.response.PhotoAlbum
import com.slideshowmaker.slideshow.data.response.SnapImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class ImagePickerViewModel(
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val selectedImages: MutableList<SnapImage> = ArrayList()
    private val _selectedImages = MutableStateFlow(emptyList<SnapImage>())

    val selectedImage: StateFlow<List<SnapImage>> = _selectedImages
    var cameraTempFile: File? = null
    var selectedUri: Uri? = null

    fun getImages(album: String, mediaKind: MediaType): Flow<PagingData<SnapImage>> {
        return Pager(
            config = PagingConfig(pageSize = 50, initialLoadSize = 50, enablePlaceholders = true),
        ) {
            imageRepository.getPicturePagingSource(album, mediaKind)
        }.flow
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }

    fun getAlbums(mediaKind: MediaType): Flow<List<PhotoAlbum>> = flow {
        emit(imageRepository.getAlbums(mediaKind))
    }

    fun isPhotoSelected(pluckImage: SnapImage, isSelected: Boolean) {
        if (isSelected) {
            if (selectedImages.isEmpty() && selectedImages.isEmpty()) {
                selectedImages.add(pluckImage)
            }
        } else {
            selectedImages.filter { it.id == pluckImage.id }
                .forEach { selectedImages.remove(it) }
        }
        _selectedImages.value = (selectedImages).toSet().toList()
    }

}
