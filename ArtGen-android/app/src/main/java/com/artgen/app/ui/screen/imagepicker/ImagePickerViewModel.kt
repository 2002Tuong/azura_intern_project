package com.artgen.app.ui.screen.imagepicker

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snapedit.app.remove.data.Image

class ImagePickerViewModel(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    fun getImages(album: String): Flow<PagingData<Image>> {
        return Pager(
            config = PagingConfig(pageSize = 50, initialLoadSize = 50, enablePlaceholders = true),
        ) {
            imageRepository.getPicturePagingSource(album)
        }.flow.cachedIn(viewModelScope)
    }

    fun loadAlbums() {
        viewModelScope.launch {
            val albums = imageRepository.getAlbums()
            val selectedAlbum = albums.firstOrNull()
            _uiState.update {
                it.copy(
                    allAlbums = albums,
                    selectedAlbum = selectedAlbum,
                )
            }
        }
    }

    fun adLoaded() {
        _uiState.update { it.copy(shouldLoadAds = false) }
    }

    fun setSelectedImage(uri: Uri) {
        _uiState.update { it.copy(selectedImage = uri) }
    }

    fun setSelectedAlbum(album: ImageAlbum) {
        _uiState.update { it.copy(selectedAlbum = album) }
    }

    data class UiState(
        val allAlbums: List<ImageAlbum> = emptyList(),
        val selectedAlbum: ImageAlbum? = null,
        val selectedImage: Uri? = null,
        val shouldLoadAds: Boolean = true
    )
}
