package com.artgen.app.ui.screen.result

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.data.remote.repository.ArtGenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultViewModel(
    private val handle: SavedStateHandle,
    remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore,
    private val repository: ArtGenRepository
) : ViewModel() {

    private val uri: Uri
        get() = requireNotNull(handle.get<String>("uri")?.toUri())

    private val _uiState = MutableStateFlow(
        UiState(uri, remoteConfig.artStyles)
    )
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow.collectLatest {isPurchase ->
                _uiState.update {
                    it.copy(isPremium = isPurchase)
                }
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            dataStore.increaseSavePhotoCount()
            _uiState.update { it.copy(isSaving = true) }
            val uri = repository.saveImageToGallery(uri)
            if (uri != null) {
                _uiState.update { it.copy(shouldSaveSaveSuccessMessage = true) }
            }
            _uiState.update { it.copy(isSaving = false) }
            _uiState.update { it.copy(hasSaved = true) }
        }
    }

    fun bannerAdsLoaded(){
        _uiState.update { it.copy(shouldLoadBannerAds = false) }
    }

    fun setSelectedArtStyle(artStyle: ArtStyle) {
        viewModelScope.launch {
            dataStore.setSelectedArtStyle(artStyle.styleId)
        }
    }

    fun hideSaveSuccessMessage() {
        _uiState.update { it.copy(shouldSaveSaveSuccessMessage = false) }
    }

    fun shouldShowBottomSheet(shouldShow: Boolean) {
        _uiState.update {
            it.copy(shouldShowSaveBottomSheet = shouldShow)
        }
    }

    fun shouldShowSaveDialog(shouldShow: Boolean) {
        _uiState.update {
            it.copy(shouldShowSaveDialog = shouldShow)
        }
    }

    data class UiState(
        val uri: Uri,
        val styles: List<ArtStyle>,
        val isSaving: Boolean = false,
        val hasSaved: Boolean = false,
        val shouldSaveSaveSuccessMessage: Boolean = false,
        val shouldLoadBannerAds:Boolean = true,
        val shouldShowSaveBottomSheet: Boolean = false,
        val isPremium: Boolean = false,
        val shouldShowSaveDialog: Boolean = false,
    )
}
