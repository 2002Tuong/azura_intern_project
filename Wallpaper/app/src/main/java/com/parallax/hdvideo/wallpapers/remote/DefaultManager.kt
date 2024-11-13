package com.parallax.hdvideo.wallpapers.remote

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.NotificationModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.extension.fromJson
import com.parallax.hdvideo.wallpapers.extension.readLineAndClose
import com.parallax.hdvideo.wallpapers.extension.readTextAndClose
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import java.io.File
import java.io.FileInputStream
import java.util.*

object DefaultManager {

    const val folderName = "data"

    private fun readLines(fileName: String) : List<String> {
        val direct = FileUtils.unzip(WallpaperApp.instance.resources.openRawResource(R.raw.data), folderName) ?: return listOf()
        val file = File(direct, fileName)
        if (!file.exists()) return listOf()
        return FileInputStream(file).readLineAndClose()
    }

    private fun readText(fileName: String) : String? {
        val direct = FileUtils.unzip(WallpaperApp.instance.resources.openRawResource(R.raw.data), folderName) ?: return null
        val file = File(direct, fileName)
        if (!file.exists()) return null
        return FileInputStream(file).readTextAndClose()
    }

    fun loadCategories() : MutableList<Category> {
        var listCategory = mutableListOf<Category>()
        if (!RemoteConfig.commonData.isActiveServer) return listCategory
        try {
            val text = readText("wallcategories.d") ?: return listCategory
            val parser = JsonParser().parse(text).asJsonArray
            val gson = Gson()
            val type = object : TypeToken<List<Category>>(){}.type
            for (it in parser) {
                val obj = it.asJsonObject
                val lang = obj.get("message").asString
                if (lang.contains(RemoteConfig.DEFAULT_LANGUAGE, ignoreCase = true)) {
                    listCategory = gson.fromJson(obj.get("data"), type)
                } else if (lang.contains(RemoteConfig.countryName, ignoreCase = true)){
                    return gson.fromJson(obj.get("data"), type)
                }
            }
        }catch (e: Exception) { }
        return listCategory
    }

    fun loadTopDownload() : MutableList<WallpaperModel> {
        var res = mutableListOf<WallpaperModel>()
        if (!RemoteConfig.commonData.isActiveServer) return res
        val language = RemoteConfig.countryName.toUpperCase(Locale.ENGLISH)
        val lines = readLines("walltopdown.d")
        for (line in lines) {
            if (line.startsWith(RemoteConfig.DEFAULT_LANGUAGE)) {
                res = convertDataToWallpapers(line.replace(RemoteConfig.DEFAULT_LANGUAGE + ":", ""))
            } else if (line.startsWith(language)) {
                res = convertDataToWallpapers(line.replace( "$language:", ""))
                break
            }
        }
        return res
    }

    private fun convertDataToWallpapers(input: String): MutableList<WallpaperModel> {
        val list = input.split(";")
        val result = mutableListOf<WallpaperModel>()
        for (ring in list) {
            val info = ring.split("|")
            if (info.size > 3) {
                val model = WallpaperModel()
                model.id = info[0]
                model.name = info[1]
                model.url = info[2]
                model.categories = info[3]
                model.imgSize = info[4]
                result.add(model)
            }
        }
        return result
    }

    fun loadNotificationData() : NotificationModel? {
        val text = readText("notification.d") ?:  return null
        val ls = fromJson<List<NotificationModel>>(text)
        val language = RemoteConfig.countryName.toUpperCase(Locale.ENGLISH)
        return ls.firstOrNull { it.countries.contains(language) } ?:
        ls.firstOrNull { it.countries.contains(RemoteConfig.DEFAULT_LANGUAGE) }
    }
}