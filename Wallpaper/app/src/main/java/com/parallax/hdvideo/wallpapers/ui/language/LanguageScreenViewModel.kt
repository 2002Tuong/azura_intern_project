package com.parallax.hdvideo.wallpapers.ui.language

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.parallax.hdvideo.wallpapers.di.storage.frefs.SharedPreferencesStorage
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageScreenViewModel @Inject constructor(
    preferencesStorage: SharedPreferencesStorage
) : BaseViewModel() {

    private val _selectedLanguageState: MutableLiveData<String?> = MutableLiveData()
    val selectedLanguageState:LiveData<String?> = _selectedLanguageState

    init {
        _selectedLanguageState.postValue(preferencesStorage.languageCode)
    }
    fun selectLanguage(language: String) {
        Log.d("Language", "calll")
        _selectedLanguageState.postValue(language)
    }
}