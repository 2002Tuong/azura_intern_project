package com.calltheme.app.ui.setcalltheme

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.ThemeConfig

class SaveThemeSuccessfullyViewModel : ViewModel() {

    var themeConfigLiveData = MutableLiveData<ThemeConfig>()

}