package com.calltheme.app.ui.mydesign

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.ThemeConfig

class ApplyThemeViewModel() : ViewModel() {
    var themeConfigLiveData = MutableLiveData<ThemeConfig>()
}