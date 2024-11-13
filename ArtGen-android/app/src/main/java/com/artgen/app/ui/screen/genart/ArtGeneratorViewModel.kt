package com.artgen.app.ui.screen.genart

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.data.remote.repository.ArtGenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtGeneratorViewModel(
    private val repository: ArtGenRepository,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    private var resultUri: Uri? = null
    private val imageIdMap = hashMapOf<Uri, String>()

    init {
        viewModelScope.launch {
            dataStore.selectedPhotoUriFlow
                .distinctUntilChanged()
                .collectLatest { uriString ->
                    val uri = if (uriString.isNotEmpty()) {
                        Uri.parse(uriString)
                    } else {
                        null
                    }
                    _uiState.update { it.copy(uri = uri) }
                }
        }

        viewModelScope.launch {
            dataStore.selectedStyleIdFlow
                .distinctUntilChanged()
                .collectLatest { id ->
                    val style = remoteConfig.artStyles.firstOrNull { it.styleId == id }
                    _uiState.update { it.copy(style = style) }
                }
        }

        viewModelScope.launch {
            dataStore.isPurchasedFlow.collectLatest {isPurchase ->
                _uiState.update { it.copy(isPremium = isPurchase) }
            }
        }
    }

    fun generateArt() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val styleId = uiState.value.style?.styleId ?: return@launch
            val uri = uiState.value.uri
            val result = repository.generateArt(
                inputImage = uri,
                imageId = imageIdMap[uri].orEmpty(),
                styleId = styleId,
                prompt = uiState.value.prompt
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(genArtSuccess = true, isLoading = false) }
                resultUri = result.getOrThrow().uri
                uri?.let { imageIdMap[uri] = result.getOrThrow().imageId.orEmpty() }
            } else {
                _uiState.update { it.copy(isLoading = false, genArtFailure = true) }
            }
        }
    }

    fun removeSelectedImage() {
        viewModelScope.launch {
            dataStore.setPhotoUri("")
        }
    }

    fun clearSuccessMessage() {
        viewModelScope.launch {
            _uiState.update { it.copy(genArtSuccess = false) }
        }
    }

    fun clearFailureDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(genArtFailure = false) }
        }
    }

    fun setPrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun getResultUri(): Uri? = resultUri
    fun bannerAdsLoaded() {
        _uiState.update { it.copy(shouldLoadBannerAds = false) }
    }

    fun shouldShowBottomPopup(shouldShow: Boolean) {
        _uiState.update { it.copy(showBottomPopup = shouldShow) }
    }

    data class UiState(
        val uri: Uri? = null,
        val prompt: String = "",
        val style: ArtStyle? = null,
        val isLoading: Boolean = false,
        val genArtSuccess: Boolean = false,
        val genArtFailure: Boolean = false,
        val shouldLoadBannerAds: Boolean = true,
        val isPremium: Boolean = false,
        val showBottomPopup: Boolean = false
    )
}
