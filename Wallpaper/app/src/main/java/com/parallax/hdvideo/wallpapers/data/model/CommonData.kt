package com.parallax.hdvideo.wallpapers.data.model

import android.annotation.SuppressLint
import android.os.Build
import com.google.gson.annotations.SerializedName
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import java.util.*

@Suppress("unused")
open class CommonData {
    @SerializedName(value = "nativeAdCountTrend")
    var nativeAdCountTrend: Long = 0
    @SerializedName(value = "awaitSend")
    var awaitSend: Long = 0
    @SerializedName(value = "nativeAdCount")
    var nativeAdCount: Long = 0
    @SerializedName(value = "nativeAdCountDetail")
    var nativeAdCountDetail: Long = 0
    @SerializedName(value = "waitingShowInter")
    var waitingShowInter: Long = 0
    @SerializedName(value = "supportInter")
    var supportInter: Boolean = false
    @SerializedName(value = "supportNative")
    var supportNative: Boolean = false
    @SerializedName(value = "supportReward")
    var supportReward: Boolean = true
    @SerializedName(value = "supportNativeDetail")
    var supportNativeDetail: Boolean = false
    @SerializedName(value = "supportIronsrc")
    var supportIronsrc: Boolean = false
    @SerializedName(value = "supportIronBanner")
    var supportIronBanner: Boolean = false
    @SerializedName(value = "supportApplovin")
    var supportApplovin: Boolean = false
    @SerializedName(value = "activeServer")
    var activeServer: Boolean = false
    @SerializedName(value = "isWallCoordinator")
    var isWallCoordinator: Boolean = true
    @SerializedName(value = "typeAds")
    var typeAds: String = ""
    @SerializedName(value = "interUnitId")
    var interUnitId: String = ""
    @SerializedName(value = "videoStorage")
    var videoStorage: String = ""
    @SerializedName(value = "videoServer")
    var videoServer: String = ""
    @SerializedName(value = "wallStorage")
    var wallStorage: String = ""
    @SerializedName(value = "wallServer")
    var wallServer: String = ""
    @SerializedName(value = "serverNtf")
    var serverNtf: String = ""
    @SerializedName(value = "endpointMnt")
    var endpointMnt: String = ""
    @SerializedName(value = "countryCodeURL")
    var countryCodeURL: String = ""
    @SerializedName(value = "urlDefaultWall")
    var urlDefaultWall: String = ""
    @SerializedName(value = "checkVideoServer")
    var checkVideoServer: String = ""
    @SerializedName(value = "checkVideoStorage")
    var checkVideoStorage: String = ""
    @SerializedName(value = "originStoragePattern")
    var originStoragePattern: String = ""
    @SerializedName(value = "delayCountries")
    var delayCountries: String = ""
    @SerializedName(value = "latestVersion")
    var latestVersion: String = ""
    @SerializedName(value = "packageName")
    var packageName: String = ""
    @SerializedName(value = "supportOpenWeb")
    var supportOpenWeb: String = ""
    @SerializedName(value = "storageInterval")
    var storageInterval = 60
    @SerializedName(value = "haServers")
    var haServers: String = ""
    @SerializedName(value = "bestHashTagNum")
    var bestHashTagNum = 5
    @SerializedName("scenarioNotify")
    var scenarioNotify: String = "2,10,23,48,72,96;4,24,48,96,120" // unit hour
    @SerializedName(value = "numberOfAdImpressions")
    var numberOfAdImpressions = 4
    @SerializedName(value = "numberOfAdImpressionsDetail")
    var numberOfAdImpressionsDetail = 4
    @SerializedName(value = "scenarioChangedWallpaper")
    var scenarioChangedWallpaper: String = "24,24,48,72,168" // days
    @SerializedName(value = "notifyUrl")
    var notifyUrl: String = "https://tpcom.xyz/notificationwall/jsons/ntf%s.json?appid=%s&lang=%s"
    @SerializedName(value = "hashtagCouple")
    var hashTagCouple: String = ""
    @SerializedName(value = "rewardedAdCount")
    var rewardedAdCount: Int = -1
    @SerializedName(value = "top10DeviceWallpaperNotification")
    var deviceWallpaperNotificationRecurrenceRate : Int = 24 //hours
    @SerializedName(value = "showAdWhenSwiping")
    var showAdWhenSwiping = 4
    @SerializedName(value = "mapCountryAndInterAd")
    var mapCountryAndInterAd = mutableMapOf<String, Int>()
    @SerializedName(value = "blackListDevices")
    var blackListDevices = arrayOf<String>() // test "nokia 5.1 plus"
    @SerializedName(value = "listDevicesDoesNotSupportFull")
    var listDevicesDoesNotSupportFull = arrayOf<String>() // test "mt6771" or "nokia 5.1 plus"
    @SerializedName(value = "classifyUsers")
    val classificationUser = ClassificationUser("", "")
    @SerializedName(value = "specialCountry")
    var specialCountry = arrayOf<String>() // "vn", "th", "ru"
    @SerializedName(value = "waitingForShowingVip")
    var waitingForShowingVip = 60_000 // milliseconds
    @SerializedName(value = "listTopicsFCM")
    var listTopicsFCM = arrayOf<String>() // "new_year, christmas"
    var listDevicesDoesNotSupportVideo = arrayOf("SM-G991;12", "SM-G991;12","SM-G996;12","SM-G990;12","SM-G990;12") // "samsung s21 ; android 12"
    var isActiveSaleOff: Boolean = false
    private val isDelay: Boolean
        @SuppressLint("DefaultLocale")
        get() = delayCountries.isNotEmpty() && delayCountries.toLowerCase(Locale.ENGLISH).contains(RemoteConfig.countryName.toLowerCase(Locale.ENGLISH))

    val isActiveServer: Boolean
        get() = !isDelay && activeServer

    private val isBlackListDevice: Boolean get() {
        val device = Build.DEVICE
        val brand = Build.BRAND
        val model = Build.MODEL
        return blackListDevices.firstOrNull {
            (it.contains(device, true) && it.contains(brand, true)) || it.contains(model, true)
        } != null
    }

    val isSupportedDevice: Boolean get()  {
        val board = Build.BOARD
        val device = Build.DEVICE
        val brand = Build.BRAND
        val model = Build.MODEL
        return listDevicesDoesNotSupportFull.firstOrNull {
            it.contains(board, true)
                    || (it.contains(device, true) && it.contains(brand, true))
                    || it.contains(model, true)

        } == null
    }

    val isSupportedVideo: Boolean by lazy {
        val model = Build.MODEL
        val android = Build.VERSION.RELEASE
        listDevicesDoesNotSupportVideo.firstOrNull {
            val array = it.split(";")
            if (array.size > 1) {
                model.contains(array[0], true) && array[1] == android
            } else false
        } == null
    }

    fun blockDevicesIfNeeded() : Boolean {
        return if (isBlackListDevice) {
            activeServer = false
            supportInter = false
            supportNative = false
            supportNativeDetail = false
            true
        } else false
    }
}
