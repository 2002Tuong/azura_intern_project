package com.calltheme.app.ui.pickavatar

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.utils.extensions.getAvatarList
import com.screentheme.app.utils.extensions.removeAvatar
import com.screentheme.app.utils.extensions.saveAvatar
import com.screentheme.app.utils.helpers.SharePreferenceHelper

class PickAvatarViewModel(val context: Context) : ViewModel() {

    var yourAvatars = MutableLiveData<ArrayList<AvatarModel>>()
    var pickedAvatarLiveData = MutableLiveData<AvatarModel?>()



    init {
        yourAvatars.postValue(SharePreferenceHelper.getAvatarList(context))
    }

    fun addAvatar(avatar: AvatarModel) {
        SharePreferenceHelper.saveAvatar(context, avatar)
        yourAvatars.postValue(SharePreferenceHelper.getAvatarList(context))
    }

    fun removeAvatar(avatar: AvatarModel) {
        SharePreferenceHelper.removeAvatar(context, avatar)
        yourAvatars.postValue(SharePreferenceHelper.getAvatarList(context))
    }
}