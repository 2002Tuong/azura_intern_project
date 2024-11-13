package com.calltheme.app.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SaveRemoteThemeViewModel : ViewModel() {
    var openCount = MutableLiveData(0)

    fun countOpen() {
        openCount.postValue((openCount.value?.toInt() ?: 0) + 1)
    }
}