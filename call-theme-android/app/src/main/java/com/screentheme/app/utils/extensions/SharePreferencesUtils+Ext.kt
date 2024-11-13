package com.screentheme.app.utils.extensions

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.LanguageModel
import com.screentheme.app.utils.helpers.SharePreferenceHelper

fun SharePreferenceHelper.saveAvatar(context: Context, avatar: AvatarModel) {
    val avatarList = getAvatarList(context)
    avatarList.add(avatar)
    saveAvatarList(context, avatarList)
}

fun SharePreferenceHelper.removeAvatar(context: Context, avatar: AvatarModel) {
    val avatarList = getAvatarList(context)
    avatarList.remove(avatar)
    saveAvatarList(context, avatarList)
}

fun SharePreferenceHelper.getAvatarList(context: Context): ArrayList<AvatarModel> {
    val jsonString = getString(context, KEY_YOUR_AVATARS, "")
    return if (!jsonString.isNullOrEmpty()) {
        val typeToken = object : TypeToken<ArrayList<AvatarModel>>() {}.type
        Gson().fromJson(jsonString, typeToken)
    } else {
        ArrayList()
    }
}

private fun SharePreferenceHelper.saveAvatarList(context: Context, list: ArrayList<AvatarModel>) {
    val jsonString = Gson().toJson(list)
    saveString(context, KEY_YOUR_AVATARS, jsonString)
}

//============================BACKGROUND================================
fun SharePreferenceHelper.saveBackground(context: Context, background: BackgroundModel) {
    val backgroundList = getBackgroundList(context)
    backgroundList.add(background)
    saveBackgroundList(context, backgroundList)
}

fun SharePreferenceHelper.removeBackground(context: Context, background: BackgroundModel) {
    val backgroundList = getBackgroundList(context)
    backgroundList.remove(background)
    saveBackgroundList(context, backgroundList)
}

fun SharePreferenceHelper.getBackgroundList(context: Context): ArrayList<BackgroundModel> {
    val jsonString = getString(context, KEY_YOUR_BACKGROUNDS, "")
    return if (!jsonString.isNullOrEmpty()) {
        val typeToken = object : TypeToken<ArrayList<BackgroundModel>>() {}.type
        Gson().fromJson(jsonString, typeToken)
    } else {
        ArrayList()
    }
}

private fun SharePreferenceHelper.saveBackgroundList(context: Context, list: ArrayList<BackgroundModel>) {
    val jsonString = Gson().toJson(list)
    saveString(context, KEY_YOUR_BACKGROUNDS, jsonString)
}

fun SharePreferenceHelper.saveLanguage(context: Context, language: LanguageModel) {
    val gson = Gson()
    val json = gson.toJson(language)
    saveString(context, KEY_APP_LANGUAGE, json)
}

fun SharePreferenceHelper.getLanguage(context: Context): LanguageModel? {
    val gson = Gson()
    val json = getString(context, KEY_APP_LANGUAGE, "")
    val language = gson.fromJson(json, LanguageModel::class.java)
    return language
}

fun SharePreferenceHelper.increaseShowInterCustomizeCount(context: Context) {
    val value = getShowInterCustomizeCount(context) + 1
    saveInt(context, KEY_CUSTOM_THEME_ITEM_CLICKED, value)
}
fun SharePreferenceHelper.getShowInterCustomizeCount(context: Context): Int {
    return getInt(context, KEY_CUSTOM_THEME_ITEM_CLICKED, 0)
}
