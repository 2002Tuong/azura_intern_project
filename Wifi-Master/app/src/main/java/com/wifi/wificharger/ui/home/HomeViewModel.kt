package com.wifi.wificharger.ui.home

import androidx.lifecycle.viewModelScope
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.data.repository.WifiRepository
import com.wifi.wificharger.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class HomeViewModel(
    dataStore: AppDataStore,
    private val wifiRepository: WifiRepository
): BaseViewModel(dataStore) {

    fun canShowRate() = !dataStore.isRated
    var featureUseCount = 0
    fun saveWifi(wifi: Wifi) {
        viewModelScope.launch {
            wifiRepository.getWifi(wifi.name).collect {
                it?.let {
                    wifiRepository.updateWifi(it.copy(password = wifi.password))
                } ?: run {
                    wifiRepository.insertWifi(wifi)
                }
            }
        }
    }
}