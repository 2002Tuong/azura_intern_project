package com.screentheme.app.utils.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.CallIconModel
import com.screentheme.app.models.CallThemeConfigModel
import com.screentheme.app.models.RemoteTheme
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.extensions.merge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter

class ThemeManager(private val context: Context) {
    val sharedPreferencesInstance: SharedPreferences =
        context.getSharedPreferences("ThemePreferences", Context.MODE_PRIVATE)
    private val currentThemeIdKey = "currentThemeId"
    private val savedThemesKey = "savedThemes"

    var currentThemeId: String
        get() = sharedPreferencesInstance.getString(currentThemeIdKey, "") ?: ""
        set(value) {
            sharedPreferencesInstance.edit().putString(currentThemeIdKey, value).apply()
        }

    var savedThemes: MutableList<String>
        get() {
            val themesJson = sharedPreferencesInstance.getString(savedThemesKey, null)
            return if (!themesJson.isNullOrEmpty()) {
                Gson().fromJson(themesJson, Array<String>::class.java).toMutableList()
            } else {
                mutableListOf()
            }
        }
        set(value) {
            val themesJson = Gson().toJson(value)
            sharedPreferencesInstance.edit().putString(savedThemesKey, themesJson).apply()
        }


    companion object {
        private const val TAG = "ThemeManager"
        private const val THEMES_FOLDER = "themes"
        private const val CONFIG_FILE_NAME = "config.json"

        const val DEFAULT_THEME_ID = "default"

    }

    fun themeFolderExists(themeId: String): Boolean {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)
        return themeFolder.exists() && themeFolder.isDirectory
    }

    fun saveDefaultTheme() {
        // Create the default theme folder
        val defaultThemeFolder = File(context.filesDir, "themes/default")

        defaultThemeFolder.mkdirs()

        // Save the default images to the theme folder
        saveAssetImage("default/background_default.webp", File(defaultThemeFolder, "background.webp"))
        saveAssetImage("default/avatar_default.webp", File(defaultThemeFolder, "avatar.webp"))
        saveAssetImage(
            "default/accept_default.webp",
            File(defaultThemeFolder, "accept_call_icon.webp")
        )
        saveAssetImage(
            "default/decline_default.webp",
            File(defaultThemeFolder, "decline_call_icon.webp")
        )


        // Create the default theme config
        val defaultThemeConfig = ThemeConfig(
            id = "default",
            themeName = "Default Theme",
            background = getLocalImagePath("default", "background.webp"),
            avatar = getLocalImagePath("default", "avatar.webp"),
            acceptCallIcon = getLocalImagePath("default", "accept_call_icon.webp"),
            declineCallIcon = getLocalImagePath("default", "decline_call_icon.webp")
        )

        // Save the default theme config
        val gson = Gson()
        val configJson = gson.toJson(defaultThemeConfig)

        val configFile = File(defaultThemeFolder, "config.json")
        val writer = OutputStreamWriter(FileOutputStream(configFile))
        writer.use {
            it.write(configJson)
        }

        Log.d(TAG, "Default theme saved successfully.")
    }

    private fun saveAssetImage(assetPath: String, outputFile: File) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var readData: Int
                    while (inputStream.read(buffer).also { readData = it } != -1) {
                        outputStream.write(buffer, 0, readData)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving asset image: ${e.message}")
        }
    }

    fun downloadThemeImages(theme: RemoteTheme, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            createConfigJson(
                themeId = theme.id,
                themeName = theme.themeName,
                backgroundFilePath = theme.background,
                avatarFilePath = theme.avatar,
                acceptCallIconFilePath = theme.acceptCallIcon,
                declineCallIconFilePath = theme.declineCallIcon,
                ringTone = "",
                flashEnabled = false
            )
            callback(true)
        }
    }

    fun getThemeConfig(themeId: String): ThemeConfig? {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)
        val configFile = File(themeFolder, CONFIG_FILE_NAME)

        if (!configFile.exists()) {
            return null
        }

        val gson = Gson()
        val reader = FileReader(configFile)
        val themeConfig = gson.fromJson(reader, ThemeConfig::class.java)
        reader.close()

        return themeConfig
    }

    fun getCurrentThemeConfig(): ThemeConfig? {
        return getThemeConfig(currentThemeId)
    }

    fun saveThemeIdToPref(themeId: String) {
        val savedThemes = savedThemes.toMutableList()
        if (!savedThemes.contains(themeId)) {
            savedThemes.add(themeId)
            this.savedThemes = savedThemes
        }
    }

    fun removeTheme(themeId: String) {
        val savedThemes = savedThemes.toMutableList()
        savedThemes.remove(themeId)
        this.savedThemes = savedThemes
    }

    fun getSavedThemesFromPref(): ArrayList<ThemeConfig> {

        val themes = savedThemes.map {
            getThemeConfig(it)
        } as ArrayList<ThemeConfig>

        return themes
    }

    private fun getLocalImagePath(themeId: String, fileName: String): String {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)
        val imageFile = File(themeFolder, fileName)
        return imageFile.absolutePath
    }

    ////=================================

    fun downloadThemeImages(themeConfig: ThemeConfig, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                // Create the config JSON
                createConfigJson(
                    themeId = themeConfig.id,
                    themeName = themeConfig.themeName,
                    backgroundFilePath = themeConfig.background,
                    avatarFilePath = themeConfig.avatar,
                    acceptCallIconFilePath = themeConfig.acceptCallIcon,
                    declineCallIconFilePath = themeConfig.declineCallIcon,
                    ringTone = themeConfig.ringtone,
                    flashEnabled = themeConfig.flashEnabled
                )

                callback.invoke(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading theme images: ${e.message}")
                callback.invoke(false)
            }
        }
    }

    private suspend fun createConfigJson(
        themeId: String,
        themeName: String,
        backgroundFilePath: String,
        avatarFilePath: String,
        acceptCallIconFilePath: String,
        declineCallIconFilePath: String,
        ringTone: String,
        flashEnabled: Boolean
    ) {
        val configData = ThemeConfig(
            id = themeId,
            themeName = themeName,
            background = backgroundFilePath,
            avatar = avatarFilePath,
            acceptCallIcon = acceptCallIconFilePath,
            declineCallIcon = declineCallIconFilePath,
            ringtone = ringTone,
            flashEnabled = flashEnabled
        )

        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)

        // Create the necessary directories if they don't exist
        themesFolder.mkdirs()
        themeFolder.mkdirs()

        val configFile = File(themeFolder, CONFIG_FILE_NAME)

        val gson = Gson()
        val json = gson.toJson(configData)

        withContext(Dispatchers.IO) {
            val writer = FileWriter(configFile)
            writer.use {
                it.write(json)
            }
        }
    }

    fun <T> getResources(remoteConfig: CallThemeConfigModel, clazz: Class<T>): List<Any?> {
        val resources = ArrayList<T>()

        when (clazz) {
            AvatarModel::class.java -> {

                if (remoteConfig.enabled_local_diy_avatar_ids.isEmpty()) {
                    return localDiyAvatars
                }

                val filteredAvatars = localDiyAvatars.filter { avatar ->
                    remoteConfig.enabled_local_diy_avatar_ids.contains(avatar.id)
                }
                return merge(remoteConfig.diy_avatars, filteredAvatars)
            }

            BackgroundModel::class.java -> {
                if (remoteConfig.enabled_local_diy_background_ids.isEmpty()) {
                    return localDiyBackgrounds
                }
                val filteredBackgrounds = localDiyBackgrounds.filter { background ->
                    remoteConfig.enabled_local_diy_background_ids.contains(background.id)
                }
                return merge(remoteConfig.diy_backgrounds, filteredBackgrounds)
            }

            CallIconModel::class.java -> {

                if (remoteConfig.enabled_local_diy_call_icon_ids.isEmpty()) {
                    return localDiyCallIcons
                }

                val filteredCallIcons = localDiyCallIcons.filter { callIcon ->
                    remoteConfig.enabled_local_diy_call_icon_ids.contains(callIcon.id)
                }
                return merge(remoteConfig.diy_call_icons, filteredCallIcons)
            }

            RemoteTheme::class.java -> {

                if (remoteConfig.enabled_local_screen_theme_ids.isEmpty()) {
                    return localScreenRemoteThemes
                }

                val filteredCallIcons = localScreenRemoteThemes.filter { theme ->
                    remoteConfig.enabled_local_screen_theme_ids.contains(theme.id)
                }
                return merge(remoteConfig.screen_themes, filteredCallIcons)
            }
        }

        return resources
    }

}