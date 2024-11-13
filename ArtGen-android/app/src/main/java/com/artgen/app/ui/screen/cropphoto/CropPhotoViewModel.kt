package com.artgen.app.ui.screen.cropphoto

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.data.repository.PhotoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CropPhotoViewModel(
    private val dataStore: AppDataStore,
    private val photoRepository: PhotoRepository,
    private val remoteConfig: RemoteConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    suspend fun saveCroppedPhoto(image: ImageBitmap) {

        _uiState.update { it.copy(isCroppingPhoto = true) }
        val saveUri = photoRepository.createTmpPhoto(
            image,
            remoteConfig.getMaxInputImageSize().toInt()
        )

        if (saveUri != null) {
            dataStore.setPhotoUri(saveUri.toString())
        }
        delay(800L)
        _uiState.update { it.copy(isCroppingPhoto = false) }
    }

    fun onAdsLoaded() {
        _uiState.update { it.copy(shouldLoadAds = false) }
    }

    data class UiState(
        val isCroppingPhoto: Boolean = false,
        val shouldLoadAds: Boolean = true
    )
}
