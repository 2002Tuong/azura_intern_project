package com.bloodpressure.app.screen.aboutme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AboutMeViewModel(private val dataStore: AppDataStore) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {

        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }

            dataStore.age
                .distinctUntilChanged()
                .collectLatest { age ->
                    if (age <= 0) {
                        _uiState.update { it.copy(age = 22) }
                    } else {
                        _uiState.update { it.copy(age = age) }
                    }

                }

        }

        viewModelScope.launch {
            dataStore.gender
                .distinctUntilChanged()
                .collectLatest { gender ->
                    _uiState.update { it.copy(gender = gender) }
                }
        }
    }

    fun setAge(age: Int) {
        viewModelScope.launch {
            dataStore.setAge(age)
        }
    }

    fun setGender(gender: GenderType) {
        viewModelScope.launch {
            dataStore.setGender(gender)
        }
    }

    data class UiState(
        val age: Int = 0,
        val gender: GenderType = GenderType.OTHERS,
        val isPurchased: Boolean = false
    )

}