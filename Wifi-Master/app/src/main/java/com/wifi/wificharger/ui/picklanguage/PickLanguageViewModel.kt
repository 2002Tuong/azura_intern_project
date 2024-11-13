package com.wifi.wificharger.ui.picklanguage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wifi.wificharger.ui.base.BaseViewModel
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.model.Language
import kotlinx.coroutines.launch

class PickLanguageViewModel(
    appDataStore: AppDataStore
) : BaseViewModel(appDataStore) {

    private val languageProvider = LanguageProvider(dataStore)

    var listOfLanguages = MutableLiveData<ArrayList<Language>>()
    var languageLiveData = MutableLiveData<Language>()
    var listOfAllLanguages = MutableLiveData<ArrayList<Language>>()

    init {
        val listLanguage = languageProvider.getLanguageList()
        listOfLanguages.postValue(listLanguage)
        languageLiveData.postValue(listLanguage.firstOrNull { it.languageCode == dataStore.selectedLanguage } ?: listLanguage[0])
        listOfAllLanguages.postValue(languageProvider.getAllLanguageList())
    }

    fun getCurrentAppLanguage(): Language? {
        return listOfAllLanguages.value?.firstOrNull { it.languageCode == dataStore.selectedLanguage }
    }

    fun setLanguageSelectedStatus() {
        viewModelScope.launch {
            dataStore.setLanguageSelected(true)
        }
    }
}