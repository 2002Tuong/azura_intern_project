package com.calltheme.app.ui.picklanguage

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.LanguageModel
import com.screentheme.app.utils.extensions.getLanguage
import com.screentheme.app.utils.helpers.SharePreferenceHelper

class PickLanguageViewModel(context: Application) : AndroidViewModel(context) {


    private val languageProvider: LanguageProvider = LanguageProvider(context)
    var listOfLanguages = MutableLiveData<ArrayList<LanguageModel>>()
    var languageLiveData = MutableLiveData<LanguageModel>()
    var listOfAllLanguages = MutableLiveData<ArrayList<LanguageModel>>()

    init {
        listOfLanguages.postValue(languageProvider.getLanguageList())
        languageLiveData.postValue(SharePreferenceHelper.getLanguage(context))
        listOfAllLanguages.postValue(languageProvider.getAllLanguageList())
    }
}