package com.calltheme.app.ui.pickavatar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.CallIconModel
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.helpers.ThemeManager

class PickAllAvatarsViewModel(
    val themeManager: ThemeManager
) : ViewModel() {

    var themeConfigListLiveData = MutableLiveData<ArrayList<ThemeConfig>>()

    fun createThemeConfigListFromAvatars(avatarList: ArrayList<AvatarModel> = ArrayList()) {
        val defaultTheme = themeManager.getThemeConfig(ThemeManager.DEFAULT_THEME_ID) ?: return

        val themeConfigList = avatarList.map {
            ThemeConfig(
                background = defaultTheme.background,
                avatar = it.avatar,
                acceptCallIcon = defaultTheme.acceptCallIcon,
                declineCallIcon = defaultTheme.declineCallIcon,
            )
        }

        themeConfigListLiveData.postValue(themeConfigList as ArrayList<ThemeConfig>?)
    }

    fun createThemeConfigListFromBackgrounds(backgroundList: ArrayList<BackgroundModel> = ArrayList()) {
        val defaultTheme = themeManager.getThemeConfig(ThemeManager.DEFAULT_THEME_ID) ?: return

        val themeConfigList = backgroundList.map {
            ThemeConfig(
                background = it.background,
                avatar = defaultTheme.avatar,
                acceptCallIcon = defaultTheme.acceptCallIcon,
                declineCallIcon = defaultTheme.declineCallIcon,
            )
        }

        themeConfigListLiveData.postValue(themeConfigList as ArrayList<ThemeConfig>?)
    }

    fun createThemeConfigListFromCallIcons(callIconList: ArrayList<CallIconModel> = ArrayList()) {
        val defaultTheme = themeManager.getThemeConfig(ThemeManager.DEFAULT_THEME_ID) ?: return

        val themeConfigList = callIconList.map {
            ThemeConfig(
                background = defaultTheme.background,
                avatar = defaultTheme.avatar,
                acceptCallIcon = it.accept_call_icon,
                declineCallIcon = it.decline_call_icon,
            )
        }

        themeConfigListLiveData.postValue(themeConfigList as ArrayList<ThemeConfig>?)
    }
}