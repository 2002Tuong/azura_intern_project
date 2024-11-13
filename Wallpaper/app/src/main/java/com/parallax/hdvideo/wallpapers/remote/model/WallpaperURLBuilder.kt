package com.parallax.hdvideo.wallpapers.remote.model

import android.text.TextUtils
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.urlEncoder
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig.Companion.ANDROID_ID
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig.Companion.commonData
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import java.io.Serializable
import java.util.*

class WallpaperURLBuilder private constructor() : Serializable {

    private val serialVersionUID = 1L
    var DEFAULT_SERVER = ""
    var SERVER_FAILED = ""
    var DEFAULT_STORAGE = ""
    var DEFAULT_SERVER_VIDEO = ""
    var DEFAULT_STORAGE_VIDEO = ""
    var DEFAULT_SERVER_NTF = ""
    var urlCountry = ""
    var urlServerInfo = ""
    var urlServerInfo2 = ""
    var urlDefaultInfo = ""
    var urlDefaultInfo2 = ""
    var format = ""
    var notifyFormat = ""
    var hashtagTrendFormat = ""
    var downloadFormat = ""
    var topDownFormat = ""
    var topNewFormat = ""
    var categoryFormat = ""
    var categorySearchFormat = ""
    var wallpaperByCategoryFormat = ""
    var videoWallpaperFormat = ""
    var parallaxWallpaperFormat = ""
    var moreAppFormat = ""
    var clickAdvertisement = ""
    var feedbackFormat = ""
    var originStorageFormat = ""
    var hashtagAllFormat = ""
    var defaultWallFormat = ""
    var languageCountry: String = ""

    var wallpaperStorage: String = ""
    var wallpaperServer: String = ""
    var videoServer: String = ""
    var videoStorage: String = ""
    var wallpaperRequest: String = ""
    var wallpaperServerFailed = ""
    var videoServerFailed = ""
    var imageStorageFailed = ""
    var colorApiFormat = ""
    var trendingApiFormat = ""
    @JvmField
    var fullStorage = ""
    @JvmField
    var storageOrigin: String = ""
    var videoSearchPath = ""
    var wallpaperImage4D =""

    fun getImageUrlDefault(pageNumber: String, gender: String, hashTags: String) : String {
        return wallpaperServer + String.format(defaultWallFormat, hashTags.urlEncoder(), languageCountry, pageNumber, mobileId, gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getParallaxUrlDefault(pageNumber: String, gender: String, type: String = "home", hashTags: String = "none") : String {
        return wallpaperServer + String.format(parallaxWallpaperFormat, type, hashTags.urlEncoder(), pageNumber, languageCountry, mobileId, gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getImageCategoryUrl(cat: String, pageNumber: Int, gender: String) : String {
        val limited = MAX_ITEM_WALL_IN_PAGE
        val offset: Int = (pageNumber - 1)* limited
        return wallpaperServer + String.format(wallpaperByCategoryFormat, cat, languageCountry, offset, limited, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getImageCategoryVideoUrl(cat: String, pageNumber: Int, gender: String) : String {
        val limited = 10
        val offset: Int = pageNumber * limited
        return videoServer + String.format(wallpaperByCategoryFormat, cat, languageCountry, offset, limited, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getMoreAppUrl(gender: String) = baseUrl(moreAppFormat, gender)

    fun getHashTagsUrl(gender: String) = baseUrl(hashtagAllFormat, gender)

    fun getHashTagsTrendUrl(gender: String) = baseUrl(hashtagTrendFormat, gender)

    fun getTopDownUrl(gender: String) = baseUrl(topDownFormat, gender)

    fun getImage4D(gender: String, pageNumber: Int) =
        get4DImageUrl(wallpaperImage4D, gender, pageNumber)

    private fun get4DImageUrl(pattern: String, gender: String, pageNumber: Int) = wallpaperServer + String.format(pattern,"none",pageNumber, languageCountry, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))

    private fun baseUrl(pattern: String, gender: String) = wallpaperServer + String.format(pattern, languageCountry, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))
    private fun baseVideoTrendingUrl(pattern: String, gender: String) = videoServer + String.format(pattern, languageCountry, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))

    fun getSearchUrl(keyWord: String, gender: String, pageNumber: Int = 1, isSuggestion: Boolean = false) : String {
        val limited = MAX_ITEM_WALL_IN_PAGE
        val offset = (pageNumber - 1) * limited
        val infos = if (!commonData.isActiveServer) { if (isSuggestion) "suggestion" else "wallpapers" }
        else if (isSuggestion) SUGGESTION_TYPE else SEARCH_TYPE
        return wallpaperServer + String.format(format, infos, keyWord.urlEncoder(), languageCountry, offset, limited, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getHashTagsUrl(keyWord: String, gender: String, pageNumber: Int = 1, isHashTag: Boolean, isNotify: Boolean = false) : String {
        val limited = MAX_ITEM_WALL_IN_PAGE
        val offset = (pageNumber - 1) * limited
        val infos = if (isHashTag) format.replaceFirst("%s?q", "%s?hashtags") else format
        val res = wallpaperServer + String.format(infos, SEARCH_TYPE, keyWord.urlEncoder(), languageCountry, offset, limited, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))
        return res.plus("&isNotify=").plus(isNotify)
    }

    fun getColorUrl(gender: String): String {
        return baseUrl(colorApiFormat, gender)
    }

    fun getTrendingWallUrl(gender: String): String {
        return baseUrl(trendingApiFormat, gender)
    }

    fun getTrendingVideoUrl(gender: String): String {
        return baseVideoTrendingUrl(trendingApiFormat, gender)
    }

    val originStorageUrl
       get() =  String.format(originStorageFormat, languageCountry, RemoteConfig.getToken(WallpaperApp.instance))


    /**
     * type = "home"  is get bestHashTags otherwise param hashTags
     */
    fun getVideoUrlDefault(pageNumber: String, gender: String, type: String = "home", hashTags: String = "none") : String {
        return videoServer + String.format(videoWallpaperFormat, type, hashTags.urlEncoder(), pageNumber, languageCountry, mobileId, gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getCategoryUrl(gender: String, page: Int = 1, limit: Int = 100) : String {
        val offset = (page - 1) * 100
        return wallpaperServer + String.format(categoryFormat, languageCountry, offset.toString(), limit.toString(), mobileId, gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getFeedbackUrl(content: String) : String {
        return wallpaperServer + String.format(feedbackFormat, content.urlEncoder(), mobileId, languageCountry, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun logDownloadWallpaperUrl(isVideo: Boolean, downId: String, gender: String, isFavorite: Boolean = false) : String {
        val oldest = String.format(downloadFormat, downId, if (isFavorite) "favorite" else "down", if(isVideo) "video"  else "image", languageCountry, mobileId, gender, RemoteConfig.getToken(WallpaperApp.instance))
        return (if (isVideo) videoServer else  wallpaperServer) + oldest
    }

    fun getWallpaperRequestUrl(email: String, topic: String, desc: String = "") : String {
        return wallpaperServer + String.format(wallpaperRequest, topic, desc, email, languageCountry, mobileId, RemoteConfig.getToken(WallpaperApp.instance))
    }

    fun getVideoSearchUrl(hashTags: String, gender: String, pageNumber: Int = 1, isRelative: Boolean = false) : String {
        val limited = MAX_ITEM_WALL_IN_PAGE
        val offset = (pageNumber - 1) * limited
        return videoServer + String.format(if (isRelative) videoSearchPath.replaceFirst("search", "relationvideos")
        else videoSearchPath, hashTags.urlEncoder(), languageCountry, offset, limited, mobileId,  gender, RemoteConfig.getToken(WallpaperApp.instance))
    }

    val mobileId: String
        get() = if (AppConstants.isActiveLogcat) { ANDROID_ID.plus(Random().nextInt(Int.MAX_VALUE)) } else ANDROID_ID

    fun getFullStorage(isMin: Boolean): String {
        return if (isMin) {
            getOriginStorage()
        } else {
            getFullStorage()
        }
    }

    fun getFullStorage() : String {
        return if (TextUtils.isEmpty(fullStorage)) {
            wallpaperStorage
        } else {
            fullStorage
        }
    }

    fun getOriginStorage(): String {
        return if (TextUtils.isEmpty(storageOrigin)) {
            getFullStorage()
        } else {
            storageOrigin
        }
    }

    @Synchronized
    fun setConfigUrl() {
        val info = commonData
        if (info.blockDevicesIfNeeded()) return
//        if (!TextUtils.isEmpty(info.urlDefaultWall)) {
//            val list = info.urlDefaultWall.split(",")
//            if (list.size > 1) {
//                urlDefaultInfo2 = list[1]
//            }
//            urlDefaultInfo = list[0]
//        }
//        val oldest = "/wallstorage/"
//        val storage = info.wallStorage
//        wallpaperStorage = if (!storage.contains(oldest)) {
//            if (commonData.isWallCoordinator) {
//                storage + RemoteConfig.countryName + oldest
//            } else {
//                storage + oldest
//            }
//        } else {
//            storage
//        }
//        if (info.originStoragePattern.isNotEmpty()) originStorageFormat = info.originStoragePattern
//        wallpaperServer = info.wallServer.ifEmpty { DEFAULT_SERVER }
        videoServer = info.videoServer.ifEmpty { DEFAULT_SERVER_VIDEO }
//        videoStorage = info.videoStorage.ifEmpty { DEFAULT_STORAGE_VIDEO }
//        languageCountry = Locale.getDefault().language.plus("_").plus(RemoteConfig.countryName)
//        val region = getRegion()
//        getServerByRegion(region, commonData.haServers)?.let {
//            wallpaperServer = String.format( "https://%s/wall7hashtaggz/rest/", it)
//        }
//        var regionStorage = region ?: "US"
//        if (regionStorage == "EA") regionStorage = "AS"
//        videoStorage = when {
//            commonData.videoStorage.contains(";") -> {
//                getServerByRegion(regionStorage, commonData.videoStorage)?.let {
//                    if (it.isEmpty()) DEFAULT_STORAGE_VIDEO else it.plus("/video7storage/")
//                } ?: DEFAULT_STORAGE_VIDEO
//            }
//            commonData.videoStorage.isNotEmpty() -> commonData.videoStorage
//            else -> DEFAULT_STORAGE_VIDEO
//        }
        val map = commonData.mapCountryAndInterAd
        val numberOfSwipeToShowInter = map[RemoteConfig.countryName.toUpperCase(Locale.ENGLISH)]
        if (numberOfSwipeToShowInter != null) {
            commonData.showAdWhenSwiping = numberOfSwipeToShowInter
        }
    }

    @Synchronized
    fun setConfigUrl(storage: LocalStorage) : Boolean {
        return if (!RemoteConfig.onLoadedConfig) {
             storage.configApp?.commonData?.let {
                 commonData = it
                 setConfigUrl()
                 true
            } ?: false
        } else true
    }

    private fun getServerByRegion(region: String?, pattern: String) : String? {
        val listServer = pattern.split(";")
        if (listServer.isEmpty() || region == null) return null
        var temp: String? = null
        for (server in listServer) {
            val array = server.split(":", limit = 2)
            if (array.size == 2) {
                if (array[0].equals(RemoteConfig.countryName, ignoreCase = true)) {
                    return array[1]
                } else if (region == array[0]) {
                    temp = array[1]
                }
            }
        }
        return temp
    }

    private fun getRegion(): String? {
        return when {
            RemoteConfig.REGION_EU_SERVER.contains(RemoteConfig.countryName, ignoreCase = true) -> "EU"
            RemoteConfig.REGION_ASIA_SERVER.contains(RemoteConfig.countryName, ignoreCase = true) -> "AS"
            RemoteConfig.REGION_EAST_ASIA.contains(RemoteConfig.countryName, ignoreCase = true) -> "EA"
            else -> null
        }
    }

    fun getUrlFail(fromUrl: String, isVideo: Boolean) : String {
        val res = if (fromUrl.contains("7storage")) {
            if (isVideo) fromUrl.replaceFirst("(http.+?)video7storage/".toRegex(), shared.DEFAULT_STORAGE_VIDEO)
            else fromUrl.replaceFirst("(http.+//.+?)wall7storage/".toRegex(), shared.imageStorageFailed)
        } else {
            if (isVideo) fromUrl.replaceFirst("(http.+?)videowalls/".toRegex(), shared.DEFAULT_STORAGE_VIDEO)
            else fromUrl.replaceFirst("(http.+//.+?)wallstorage/".toRegex(), shared.imageStorageFailed)
        }
        return res.replaceFirst("/(0\\d_\\d+x.+?)/".toRegex(), "/")
    }

    companion object {
        private const val SEARCH_TYPE = "wallpapersv2"
        private const val SUGGESTION_TYPE = "suggestionv2"

        const val MAX_ITEM_WALL_IN_PAGE = 20
        val shared = WallpaperURLBuilder()

        fun getURLByRegion(countryCode: String, originUrl: String): String {
            if (RemoteConfig.REGION_EU.contains(countryCode.toLowerCase(Locale.ENGLISH))) {
                return originUrl.replace("/configstorage/", "/configstorageeu/").replace("us-west-2", "eu-west-2")
            }
            return if (RemoteConfig.REGION_ASIA.contains(countryCode.toLowerCase(Locale.ENGLISH))) {
                originUrl.replace("/configstorage/", "/configstorageasia/")
                    .replace("us-west-2", "ap-southeast-1")
            } else originUrl
        }

        fun getCountryByLang(): String {
            val language = Locale.getDefault().language.toLowerCase(Locale.ENGLISH)
            for (country in RemoteConfig.langCode) {
                if (country.startsWith(language)) {
                    return country.replace(language + "_", "")
                }
            }
            return RemoteConfig.DEFAULT_LANGUAGE
        }
    }
}