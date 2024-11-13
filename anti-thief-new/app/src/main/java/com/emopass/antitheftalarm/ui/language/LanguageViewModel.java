package com.emopass.antitheftalarm.ui.language;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.emopass.antitheftalarm.utils.PreferencesHelper;

public class LanguageViewModel extends ViewModel {

    private MutableLiveData<String> _selectedLanguage = new MutableLiveData<>();
    public LiveData<String> selectedLanguage = _selectedLanguage;

    public LanguageViewModel() {
        _selectedLanguage.postValue(PreferencesHelper.getLanguage());
    }

    public void selectLanguage(String languageCode) {
        _selectedLanguage.postValue(languageCode);
    }
}
