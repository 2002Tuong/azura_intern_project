package com.calltheme.app.ui.previewcall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.ThemeConfig

class PreviewCallViewModel() : ViewModel() {
    var themeConfigLiveData = MutableLiveData<ThemeConfig>()
}