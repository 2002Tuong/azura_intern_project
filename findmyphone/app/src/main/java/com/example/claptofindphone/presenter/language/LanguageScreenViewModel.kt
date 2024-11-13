package com.example.claptofindphone.presenter.language

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.claptofindphone.data.local.PreferenceSupplier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageScreenViewModel @Inject constructor(
    preferenceSupplier: PreferenceSupplier
) : ViewModel() {

    private val _selectedLanguageState: MutableLiveData<String?> = MutableLiveData()
    val selectedLanguageState:LiveData<String?> = _selectedLanguageState

    init {
        _selectedLanguageState.postValue(preferenceSupplier.languageCode)
    }
    fun selectLanguage(language: String) {
        Log.d("Language", "calll")
        _selectedLanguageState.postValue(language)
    }
}