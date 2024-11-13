package com.calltheme.app.ui.mydesign

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.helpers.ThemeManager
import com.screentheme.app.utils.helpers.stringLiveData

class MyDesignViewModel(val themeManager: ThemeManager) : ViewModel() {

    val prefs = themeManager.sharedPreferencesInstance
    var myThemes = MutableLiveData<ArrayList<ThemeConfig>>()

    init {
        myThemes.postValue(themeManager.getSavedThemesFromPref())
    }

    fun observeData(lifecycleOwner: LifecycleOwner) {
        val savedThemesPrefLiveData = prefs.stringLiveData("savedThemes", "")
        savedThemesPrefLiveData.observe(lifecycleOwner) {
            myThemes.postValue(themeManager.getSavedThemesFromPref())
        }

        val currentThemeIdPrefLiveData = prefs.stringLiveData("currentThemeId", "")
        currentThemeIdPrefLiveData.observe(lifecycleOwner) {
            myThemes.postValue(themeManager.getSavedThemesFromPref())
        }
    }
}