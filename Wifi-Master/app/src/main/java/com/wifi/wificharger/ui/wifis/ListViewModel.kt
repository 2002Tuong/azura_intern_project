package com.wifi.wificharger.ui.wifis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.data.repository.WifiRepository
import com.wifi.wificharger.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ListViewModel(
    dataStore: AppDataStore,
    private val wifiRepository: WifiRepository
): BaseViewModel(dataStore) {

    val listWifi: LiveData<List<Wifi>> get() = _listWifi
    private val _listWifi: MutableLiveData<List<Wifi>> = MutableLiveData()

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

    fun updateWifi(wifi: Wifi) {
        viewModelScope.launch {
            wifiRepository.getWifi(wifi.name).collect {
                it?.let {
                    wifiRepository.updateWifi(it.copy(rewardedEarn = true))
                }
            }
        }
    }

    fun getSavedWifi() {
        viewModelScope.launch {
            wifiRepository.getWifiList().collect {
                _listWifi.value = it
            }
        }
    }

    fun setListWifi(listWifi: List<Wifi>) {
        _listWifi.postValue(listWifi)
    }
}
