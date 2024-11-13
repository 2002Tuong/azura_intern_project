package com.calltheme.app.ui.edittheme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.ThemeConfig

class EditThemeViewModel : ViewModel(){
    var theme = MutableLiveData<ThemeConfig>()
}