package com.artgen.app.ui.screen.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.tracking.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LanguageSelectionViewModel(
    private val dataStore: AppDataStore,
    languageProvider: LanguageProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        val languages = languageProvider.provide()
        val selectedLanguageTag = dataStore.selectedLanguage
        val selectedLanguage = if (selectedLanguageTag.isNotEmpty()) {
            languages.firstOrNull { it.languageTag == selectedLanguageTag }
        } else {
            TrackingManager.logLanguageSelectionLaunchEvent()
            val currentSystemLanguage = languageProvider.getCurrentSystemLanguage()
            languages.firstOrNull { it.languageTag == currentSystemLanguage }
        } ?: languages.first()

        val sortedLanguages = buildList {
            add(selectedLanguage)
            addAll(languages.filter { it != selectedLanguage })
        }
        _uiState.update {
            it.copy(
                languages = sortedLanguages,
                selectedLanguage = if (selectedLanguageTag.isNotEmpty()) selectedLanguage else null
            )
        }

        viewModelScope.launch {
            dataStore.isOnboardingShownFlow.collectLatest { isShown ->
                _uiState.update { it.copy(isOnboardingShown = isShown) }
            }
        }
    }

    fun setLanguageSelected(language: Language) {
        viewModelScope.launch {
            dataStore.setSelectedLanguage(language.languageTag)
        }
    }

    fun setSelectedLanguage(language: Language) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    data class UiState(
        val languages: List<Language> = emptyList(),
        val selectedLanguage: Language? = null,
        val isOnboardingShown: Boolean = false
    )
}
