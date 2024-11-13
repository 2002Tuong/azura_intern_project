package com.calltheme.app.ui.pickbackground

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.utils.extensions.getBackgroundList
import com.screentheme.app.utils.extensions.removeBackground
import com.screentheme.app.utils.extensions.saveBackground
import com.screentheme.app.utils.helpers.SharePreferenceHelper

class PickBackgroundViewModel(val context: Context) : ViewModel() {

    var yourBackgrounds = MutableLiveData<ArrayList<BackgroundModel>>()
    var pickedBackgroundLiveData = MutableLiveData<BackgroundModel?>()

    init {
        yourBackgrounds.postValue(SharePreferenceHelper.getBackgroundList(context))
    }

    fun addBackground(background: BackgroundModel) {
        SharePreferenceHelper.saveBackground(context, background)
        yourBackgrounds.postValue(SharePreferenceHelper.getBackgroundList(context))
    }

    fun removeBackground(background: BackgroundModel) {
        SharePreferenceHelper.removeBackground(context, background)
        yourBackgrounds.postValue(SharePreferenceHelper.getBackgroundList(context))
    }
}