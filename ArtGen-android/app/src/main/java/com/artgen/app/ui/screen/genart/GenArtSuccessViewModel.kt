package com.artgen.app.ui.screen.genart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.data.remote.RemoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenArtSuccessViewModel(
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    init {
        viewModelScope.launch {
            dataStore.selectedStyleIdFlow
                .distinctUntilChanged()
                .collectLatest { id ->
                    _uiState.update {
                        val style = remoteConfig.artStyles.firstOrNull { style -> style.styleId == id }
                        it.copy(style = style)
                    }
                }
        }
    }
    data class UiState(
        val style: ArtStyle? = null
    )
}