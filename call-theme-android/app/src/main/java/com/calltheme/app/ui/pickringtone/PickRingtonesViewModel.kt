package com.calltheme.app.ui.pickringtone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.RingtoneModel
import com.screentheme.app.utils.helpers.RingtoneController

class PickRingtonesViewModel(val ringtoneHelper: RingtoneController) : ViewModel() {

    var listOfRingtones = MutableLiveData<ArrayList<RingtoneModel>>()
    var pickedRingtoneLiveData = MutableLiveData<RingtoneModel?>()

    fun getListRingtones() {
        // How to get context here?
        listOfRingtones.postValue(ringtoneHelper.getDefaultRingtones() as ArrayList<RingtoneModel>?)
    }
}