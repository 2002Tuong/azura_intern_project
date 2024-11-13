package com.bloodpressure.app.screen.home.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeInfoViewModel(
    private val dataStore: AppDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }
    }


    data class UiState(
        val isPurchased: Boolean = false,
        val homeInfoList: List<HomeInfoType> = listOf(
            HomeInfoType.BLOOD_PRESSURE,
            HomeInfoType.HEART_RATE,
            HomeInfoType.WEIGHT_BMI,
            HomeInfoType.BLOOD_SUGAR,
            HomeInfoType.WATER_REMINDER
        ),
    )
}
