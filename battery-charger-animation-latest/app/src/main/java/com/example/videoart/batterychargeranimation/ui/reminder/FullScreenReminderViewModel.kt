package com.example.videoart.batterychargeranimation.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoart.batterychargeranimation.data.remote.RemoteConfig
import com.example.videoart.batterychargeranimation.model.RemoteTheme
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
            _theme.update { RemoteConfig
                .remoteThemes
                ?.shuffled()
                ?.take(2) ?: emptyList()
            }
        }
    }
}