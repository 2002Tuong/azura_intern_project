package com.artgen.app.ui.screen.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.log.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullScreenReminderViewModel(
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) : ViewModel() {
    fun setSelectStyle(style: ArtStyle?) {
        viewModelScope.launch {
            style?.styleId?.let {
                dataStore.setSelectedArtStyle(styleId = it)
                Logger.d("setting selected style to $it")
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(style = remoteConfig.artStyles.randomOrNull()) }
        }
    }

    data class UiState(val style: ArtStyle? = null)
}