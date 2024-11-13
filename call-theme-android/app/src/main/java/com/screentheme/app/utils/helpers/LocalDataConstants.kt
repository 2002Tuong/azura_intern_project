package com.screentheme.app.utils.helpers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.CallIconModel
import com.screentheme.app.models.RemoteTheme

const val LOCAL_SCREEN_THEMES_JSON = """
[
    {
        "id": "local_screen_theme_1",
        "theme_name": "Theme 1",
        "background": "file:///android_asset/screen_background/background1.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar1.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept1.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline1.webp",
        "category": "Neon"  
    },
    {
        "id": "local_screen_theme_2",
        "theme_name": "Theme 2",
        "background": "file:///android_asset/screen_background/background2.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar2.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept2.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline2.webp",
        "category": "Car"
    },
    {
        "id": "local_screen_theme_3",
        "theme_name": "Theme 3",
        "background": "file:///android_asset/screen_background/background3.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar3.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept3.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline3.webp",
        "category": "Car"
    },
    {
        "id": "local_screen_theme_4",
        "theme_name": "Theme 4",
        "background": "file:///android_asset/screen_background/background4.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar4.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept4.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline4.webp",
        "category": "Car"
    },
    {
        "id": "local_screen_theme_5",
        "theme_name": "Theme 5",
        "background": "file:///android_asset/screen_background/background5.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar5.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept5.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline5.webp",
        "category": "Car"
    },
    {
        "id": "local_screen_theme_6",
        "theme_name": "Theme 6",
        "background": "file:///android_asset/screen_background/background6.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar6.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept6.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline6.webp",
        "category": "Marvel"
    },
    {
        "id": "local_screen_theme_7",
        "theme_name": "Theme 7",
        "background": "file:///android_asset/screen_background/background7.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar7.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept7.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline7.webp",
        "category": "Halloween"
    },
    {
        "id": "local_screen_theme_8",
        "theme_name": "Theme 8",
        "background": "file:///android_asset/screen_background/background8.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar8.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept8.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline8.webp",
        "category": "Marvel"
    },
    {
        "id": "local_screen_theme_9",
        "theme_name": "Theme 9",
        "background": "file:///android_asset/screen_background/background9.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar9.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept9.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline9.webp",
        "category": "Halloween"
    },
    {
        "id": "local_screen_theme_10",
        "theme_name": "Theme 10",
        "background": "file:///android_asset/screen_background/background10.webp",
        "avatar": "file:///android_asset/screen_avatar/avatar10.webp",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept10.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline10.webp",
        "category": "Halloween"
    }
]
"""

const val LOCAL_DIY_CALL_ICONS_JSON = """
[
    {
        "id": "local_diy_accept1_decline_1",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept1.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline1.webp"
    },
    {
        "id": "local_diy_accept2_decline_2",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept2.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline2.webp"
    },
    {
        "id": "local_diy_accept3_decline_3",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept3.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline3.webp"
    },
    {
        "id": "local_diy_accept4_decline_4",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept4.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline4.webp"
    },
    {
        "id": "local_diy_accept5_decline_5",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept5.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline5.webp"
    },
    {
        "id": "local_diy_accept6_decline_6",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept6.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline6.webp"
    },
    {
        "id": "local_diy_accept7_decline_7",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept7.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline7.webp"
    },
    {
        "id": "local_diy_accept8_decline_8",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept8.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline8.webp"
    },
    {
        "id": "local_diy_accept9_decline_9",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept9.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline9.webp"
    },
    {
        "id": "local_diy_accept10_decline_10",
        "accept_call_icon": "file:///android_asset/screen_accept_icon/accept10.webp",
        "decline_call_icon": "file:///android_asset/screen_decline_icon/decline10.webp"
    }
]
"""

const val LOCAL_DIY_AVATARS = """
[
    {
        "id": "local_diy_avatar_1",
        "avatar": "file:///android_asset/screen_avatar/avatar1.webp"
    },
    {
        "id": "local_diy_avatar_2",
        "avatar": "file:///android_asset/screen_avatar/avatar2.webp"
    },
    {
        "id": "local_diy_avatar_3",
        "avatar": "file:///android_asset/screen_avatar/avatar3.webp"
    },
    {
        "id": "local_diy_avatar_4",
        "avatar": "file:///android_asset/screen_avatar/avatar4.webp"
    },
    {
        "id": "local_diy_avatar_5",
        "avatar": "file:///android_asset/screen_avatar/avatar5.webp"
    }
]
"""

const val LOCAL_DIY_BACKGROUNDS = """
[
    {
        "id": "local_diy_background_1",
        "background": "file:///android_asset/screen_background/background1.webp"
    },
    {
        "id": "local_diy_background_2",
        "background": "file:///android_asset/screen_background/background2.webp"
    },
    {
        "id": "local_diy_background_3",
        "background": "file:///android_asset/screen_background/background3.webp"
    },
    {
        "id": "local_diy_background_4",
        "background": "file:///android_asset/screen_background/background4.webp"
    },
    {
        "id": "local_diy_background_5",
        "background": "file:///android_asset/screen_background/background5.webp"
    }
]
"""

fun convertJsonToRemoteThemes(jsonString: String): ArrayList<RemoteTheme> {
    val gson = Gson()
    val listType = object : TypeToken<ArrayList<RemoteTheme>>() {}.type
    return gson.fromJson(jsonString, listType)
}

val localScreenRemoteThemes: ArrayList<RemoteTheme> = convertJsonToRemoteThemes(LOCAL_SCREEN_THEMES_JSON)

fun convertJsonToCallIcons(jsonString: String): ArrayList<CallIconModel> {
    val gson = Gson()
    val listType = object : TypeToken<ArrayList<CallIconModel>>() {}.type
    return gson.fromJson(jsonString, listType)
}

val localDiyCallIcons: ArrayList<CallIconModel> = convertJsonToCallIcons(LOCAL_DIY_CALL_ICONS_JSON)

fun convertJsonToAvatars(jsonString: String): ArrayList<AvatarModel> {
    val gson = Gson()
    val listType = object : TypeToken<ArrayList<AvatarModel>>() {}.type
    return gson.fromJson(jsonString, listType)
}

val localDiyAvatars: ArrayList<AvatarModel> = convertJsonToAvatars(LOCAL_DIY_AVATARS)

fun convertJsonToBackgrounds(jsonString: String): ArrayList<BackgroundModel> {
    val gson = Gson()
    val listType = object : TypeToken<ArrayList<BackgroundModel>>() {}.type
    return gson.fromJson(jsonString, listType)
}

val localDiyBackgrounds: ArrayList<BackgroundModel> = convertJsonToBackgrounds(LOCAL_DIY_BACKGROUNDS)