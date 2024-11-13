package com.wifi.wificharger.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifi.wificharger.data.local.AppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel(protected val dataStore: AppDataStore) : ViewModel() {
    protected val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> get() = _isPremium
    init {
        viewModelScope.launch {
            dataStore.isPurchasedFlow.collectLatest { isPurchase ->
                _isPremium.update { isPurchase }
            }
        }
    }
}
