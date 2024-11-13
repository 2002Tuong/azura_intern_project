package com.screentheme.app.models

import androidx.annotation.Keep

@Keep
data class CallThemeConfigModel (
    val screen_themes: ArrayList<RemoteTheme> = ArrayList(),
    val diy_avatars: ArrayList<AvatarModel> = ArrayList(),
    val diy_call_icons: ArrayList<CallIconModel> = ArrayList(),
    val diy_backgrounds: ArrayList<BackgroundModel> = ArrayList(),
    val enabled_local_screen_theme_ids: ArrayList<String> = ArrayList(),
    val enabled_local_diy_avatar_ids: ArrayList<String> = ArrayList(),
    val enabled_local_diy_call_icon_ids: ArrayList<String> = ArrayList(),
    val enabled_local_diy_background_ids: ArrayList<String> = ArrayList()
)