package com.wifi.wificharger.data.repository

import com.wifi.wificharger.data.dao.WifiDao
import com.wifi.wificharger.data.model.Wifi

class WifiRepository(
    private val wifiDao: WifiDao
) {
    suspend fun insertWifi(wifi: Wifi) = wifiDao.insertWifi(wifi)
    fun getWifi(name: String) = wifiDao.getWifi(name)
    fun getWifiList() = wifiDao.getWifiList()
    fun updateWifi(wifi: Wifi) = wifiDao.updateWifi(wifi)
}