package com.calltheme.app.ui.setcalltheme

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.ThemeConfig

class SetCallThemeViewModel: ViewModel() {
    var theme = MutableLiveData<ThemeConfig>()

    var openCount = MutableLiveData(0)

    fun countOpen() {
        openCount.postValue((openCount.value?.toInt() ?: 0) + 1)
    }
}