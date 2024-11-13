package com.calltheme.app.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.models.RemoteTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullScreenReminderViewModel(
) : ViewModel() {

    private val _theme: MutableStateFlow<List<RemoteTheme>> = MutableStateFlow(emptyList())
    val theme: StateFlow<List<RemoteTheme>> get() = _theme

    init {
        viewModelScope.launch {
            _theme.update { AppRemoteConfig
                .callThemeConfigs()
                .screen_themes
                .shuffled()
                .take(2)
            }
        }
    }
}