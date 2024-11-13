package com.example.videoart.batterychargeranimation.ui.mytheme

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.helper.stringLiveData
import com.example.videoart.batterychargeranimation.model.Theme

class MyThemeViewModel(context: Context) : ViewModel() {
    val prefs = ThemeManager.getInstance(context).sharedPreferences
    var myThemes = MutableLiveData<List<Theme>>()
    var currentThemesId = MutableLiveData<String>()
    val mode = MutableLiveData<Mode>()
    init {
        myThemes.postValue(ThemeManager.getInstance(context).getSavedThemesFromPref())
        currentThemesId.postValue(ThemeManager.getInstance(context).currentThemeId)
        mode.postValue(Mode.NORMAL)
    }

    fun observeData(context: Context, lifecycleOwner: LifecycleOwner) {
        val savedThemesPrefLiveData = prefs.stringLiveData("savedThemes", "")
        savedThemesPrefLiveData.observe(lifecycleOwner) {
            myThemes.postValue(ThemeManager.getInstance(context).getSavedThemesFromPref())
        }

        val currentThemeIdPrefLiveData = prefs.stringLiveData("currentThemeId", "")
        currentThemeIdPrefLiveData.observe(lifecycleOwner) {
            currentThemesId.postValue(it)
            myThemes.postValue(ThemeManager.getInstance(context).getSavedThemesFromPref())
        }
    }

    fun changeMode(newMode: Mode) {
        mode.postValue(newMode)
    }
}

enum class Mode{
    NORMAL, EDIT
}