package com.example.videoart.batterychargeranimation.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.example.videoart.batterychargeranimation.model.Theme
import com.google.gson.Gson
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

class ThemeManager private constructor(private val context: Context) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ThemePreferences", Context.MODE_PRIVATE)
    private val currentThemeIdKey = "currentThemeId"
    private val savedThemesKey = "savedThemes"

    var currentThemeId: String
        get() = sharedPreferences.getString(currentThemeIdKey, "") ?: ""
        set(value) {
            sharedPreferences.edit().putString(currentThemeIdKey, value).apply()
        }

    var savedThemes: MutableList<String>
        get() {
            val themesJson = sharedPreferences.getString(savedThemesKey, null)
            return if (!themesJson.isNullOrEmpty()) {
                Gson().fromJson(themesJson, Array<String>::class.java).toMutableList()
            } else {
                mutableListOf()
            }
        }
        set(value) {
            val themesJson = Gson().toJson(value)
            sharedPreferences.edit().putString(savedThemesKey, themesJson).apply()
        }

    companion object {
        private const val TAG = "ThemeManager"
        const val THEMES_FOLDER = "themes"
        const val CONFIG_FILE_NAME = "config.json"
        const val FONT_FILE_NAME = "font.ttf"
        const val SOUND_FILE_NAME = "sound.mp3"
        const val GIF_FILE_NAME = "animation.mp3"

        const val DEFAULT_THEME_ID = "default"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun getLocalImagePath(themeId: String, fileName: String): String {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)
        val imageFile = File(themeFolder, fileName)
        return imageFile.absolutePath
    }


    fun themeFolderExists(themeId: String): Boolean {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)
        return themeFolder.exists() && themeFolder.isDirectory
    }

    fun saveDefault() {
        val defaultThemeFolder = File(context.filesDir, "themes/default")
        defaultThemeFolder.mkdirs()

        //save asset
        saveAssetImage("default.png", File(defaultThemeFolder, "default.png"))


        val defaultTheme = Theme(
            id="default",
            thumbnail = getLocalImagePath("default", "default.png"),
            animation = "",
            sound = "",
            fontId = "",
        )

        val gson = Gson()
        val configJson = gson.toJson(defaultTheme)
        val configFile = File(defaultThemeFolder, "config.json")
        val writer = OutputStreamWriter(FileOutputStream(configFile))
        writer.use {
            it.write(configJson)
        }

        writer.close()
    }

    private fun saveAssetImage(assetPath: String, outputFile: File) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving asset image: ${e.message}")
        }
    }

    fun downloadThemeImages(theme: RemoteTheme, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                createConfigJson(
                    themeId = theme.id,
                    thumbnail = theme.thumbUrl,
                    animation = theme.animationUrl,
                    sound = theme.soundUrl,
                    font = theme.font,
                    category = theme.categoryId,
                    fromRemote = true
                )
                callback.invoke(true)
            }catch (e: Exception) {
                Log.e(TAG, "Error downloading theme images: ${e.message}")
                callback.invoke(false)
            }
        }
    }

    fun downloadThemeImages(theme: Theme, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                createConfigJson(
                    themeId = theme.id,
                    thumbnail = theme.thumbnail,
                    animation = theme.animation,
                    sound = theme.sound,
                    font = theme.fontId,
                    category = theme.category,
                    fromRemote = theme.fromRemote
                )
                callback.invoke(true)
            }catch (e: Exception) {
                Log.e(TAG, "Error downloading theme images: ${e.message}")
                callback.invoke(false)
            }
        }
    }

    fun getTheme(themeId: String): Theme? {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)
        val configFile = File(themeFolder, CONFIG_FILE_NAME)
        if(!configFile.exists()) {
            return null
        }
        val gson = Gson()
        val reader = FileReader(configFile)
        val themeConfig = gson.fromJson(reader, Theme::class.java)

        reader.close()
        return themeConfig
    }

    fun getCurrentThemeConfig(): Theme? {
        return getTheme(currentThemeId)
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

        if(!themeFolderExists(themeId)) {
            return
        }
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)

        for(fileName in themeFolder.list() ) {
            val file = File(themeFolder, fileName)
            if(file.exists()) file.delete()
        }

        themeFolder.delete()

    }

    fun getSavedThemesFromPref(): ArrayList<Theme> {

        val themes = savedThemes.map {
            getTheme(it)
        } as ArrayList<Theme>

        return themes
    }

    private suspend fun createConfigJson(
        themeId: String,
        thumbnail: String,
        animation: String,
        sound: String,
        font: String,
        category: String,
        fromRemote: Boolean
    ) {
        val theme = Theme(
            id = themeId,
            thumbnail = thumbnail,
            animation = animation,
            sound = sound,
            fontId = font,
            category = category,
            fromRemote = fromRemote
        )

        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)

        // Create the necessary directories if they don't exist
        themesFolder.mkdirs()
        themeFolder.mkdirs()

        val configFile = File(themeFolder, CONFIG_FILE_NAME)
        val json = Gson().toJson(theme)

        withContext(Dispatchers.IO) {
            val writer = FileWriter(configFile)
            writer.use {
                it.write(json)
            }
        }
    }

    fun createSoundPath(themeId: String) : String {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)

        // Create the necessary directories if they don't exist
        themesFolder.mkdirs()
        themeFolder.mkdirs()

        return "${themeFolder}/${SOUND_FILE_NAME}"
    }

    fun createFontPath(themeId: String) : String {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)

        // Create the necessary directories if they don't exist
        themesFolder.mkdirs()
        themeFolder.mkdirs()

        return "${themeFolder}/${FONT_FILE_NAME}"
    }

    fun createAnimationPath(themeId: String) : String {
        val themesFolder = File(context.filesDir, THEMES_FOLDER)
        val themeFolder = File(themesFolder, themeId)

        // Create the necessary directories if they don't exist
        themesFolder.mkdirs()
        themeFolder.mkdirs()

        return "${themeFolder}/${GIF_FILE_NAME}"
    }
}