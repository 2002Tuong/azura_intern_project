package com.bloodpressure.app.screen.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val selectedLanguage = languages.firstOrNull { it.languageTag == selectedLanguageTag }

        val sortedLanguages = buildList {
            if (selectedLanguage != null) {
                add(selectedLanguage)
            }
            addAll(languages.filter { it != selectedLanguage })
        }
        _uiState.update {
            it.copy(
                languages = sortedLanguages,
                selectedLanguage = selectedLanguage
            )
        }

        viewModelScope.launch {
            dataStore.isOnboardingShownFlow.collectLatest { isShown ->
                _uiState.update { it.copy(isOnboardingShown = isShown) }
            }
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
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
        val isOnboardingShown: Boolean = false,
        val isPurchased: Boolean = false
    )
}
