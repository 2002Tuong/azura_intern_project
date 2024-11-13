package com.parallax.hdvideo.wallpapers.remote

import android.content.Context
import com.google.firebase.SecurityToken
import com.parallax.hdvideo.wallpapers.data.model.CommonData
import com.parallax.hdvideo.wallpapers.remote.model.AppInfo
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.FirebaseAnalyticSupport
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.io.*
import java.util.*

class RemoteConfig {

    companion object {
        const val REGION_EU = ",al,ad,at,by,be,ba,bg,hr,cy,cz,dk,ee,fo,fi,fr,de,gi,gr,hu,is,ie,im,it,rs,lv,li,lt,lu,mk,mt,md,mc,me,nl,no,pl,pt,ro,ru,sm,rs,sk,si,es,se,ch,ua,gb,va,rs,ml,so,ng,ci,uz,au,ye,mr,bf,ly,sn,"
        const val REGION_ASIA = ",af,am,az,bh,bd,bt,bn,kh,cx,cc,io,ge,id,ir,iq,il,jo,kz,kw,kg,la,lb,mo,my,mv,mn,mm,np,kp,om,ps,ph,qa,sa,sg,lk,sy,tj,th,tr,tm,ae,vn,tw,jp,kr,hk,cn,"

        const val REGION_EU_SERVER = ",al,ad,at,by,be,ba,bg,hr,cy,cz,dk,ee,fo,fi,fr,de,gi,gr,hu,is,ie,im,it,rs,lv,li,lt,lu,mk,mt,md,mc,me,nl,no,pl,pt,ro,ru,sm,rs,sk,si,es,se,ch,ua,gb,va,rs,ml,so,ng,ci,uz,au,ye,mr,bf,ly,sn,"
        const val REGION_ASIA_SERVER = ",af,am,az,bh,bd,bt,bn,kh,cx,cc,io,ge,id,ir,iq,il,jo,kz,kw,kg,la,lb,mo,my,mv,mn,mm,np,kp,om,ps,ph,qa,sa,sg,lk,sy,tj,th,tr,tm,ae,vn,"
        const val REGION_EAST_ASIA = ",tw,jp,kr,hk,cn,"
        val langCode = listOf(*"ar_SA,az_AZ,bg_BG,cs_CZ,da_DK,de_DE,el_GR,es_ES,fa_IR,fi_FI,fr_FR,hr_HR,hu_HU,in_ID,it_IT,iw_IL,ja_JP,ko_KR,lt_LT,lv_LV,mr_IN,ms_MY,nl_NL,pl_PL,pt_BR,ro_RO,ru_RU,sk_SK,sr_RS,sv_SE,th_TH,tr_TR,uk_UA,vi_VN,zh_TW,cn_TW,hk_TW".split(",".toRegex()).toTypedArray())


        private const val onStart = false
        private  var verified = false
        private  var isChecked = false
        var NOTIFY_CHANNEL_ID = "default_channel_id"
        var appId = "videowallpaperpinterestdev"
        var DEFAULT_ENDPOINT = "default_channel_id"
        val DEFAULT_LANGUAGE = "OT"
        var countryName = "OT"
        var languageCode = ""
        var ANDROID_ID = "08A3885D9463AE365B56C859AF40041A"
        var commonData = CommonData()
        var onLoadedConfig = false
        const val hashTagDefault = "none"

        fun loadDefaultApplicationInfo() {
            AppInfo.shared.appConfigId = "videowallpaper"
            AppInfo.shared.wallDefaultDataFileName = "videowallpaper"
            AppInfo.shared.preferencesName = "videowallpaper_pref"
            AppInfo.shared.notifyChannelName = "videowallpaper_notify_channel"
            AppInfo.shared.appVersion = "_1.0.0"
        }

        @Synchronized
        fun initPrivateKey(appInfo: AppInfo, `is`: InputStream?) {
            languageCode = Locale.getDefault().language.toLowerCase(Locale.ENGLISH)
            var fileOut: FileOutputStream? = null
            loadDefaultApplicationInfo()
            try {
                if (onStart) {
                    val file = File("fileName")
                    fileOut = FileOutputStream(file)
                }
            } catch (e: Exception) {
                Logger.e(e, "checkBuild Error: " + e.message)
            } finally {
                try {
                    FirebaseAnalyticSupport.setupConfig()
                    fileOut?.close()
                } catch (e: IOException) {
                    Logger.e(e, "checkBuild Close Error: " + e.message)
                }
            }
            if (onStart) {
                val request = OutputStreamWriter(System.out)
                try {
                    request.close()
                } catch (e: IOException) {
                    Logger.e(e, "Error")
                } finally {
                    try {
                        request?.close()
                    } catch (e: IOException) {
                        Logger.e(e, "initPrivateKey Close Error: " + e.message)
                    }
                }
            }
            if (`is` != null) {
                val reader =
                    BufferedReader(InputStreamReader(`is`))
                val stringBuilder = java.lang.StringBuilder()
                var line: String? = null
                try {
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(
                            """
                            $line
                            
                            """.trimIndent()
                        )
                    }
                } catch (e: IOException) {
                    Logger.e(e, "Error")
                } finally {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            try {
                synchronized(this) {
                    NOTIFY_CHANNEL_ID = appInfo.notifyChannelName
                    WallpaperURLBuilder.shared.urlCountry = "https://ipinfo.io/country"
                    WallpaperURLBuilder.shared.urlServerInfo = buildConfigURL("https://s3-us-west-2.amazonaws.com/configstorage/", appId, "gz_data.json")
                    WallpaperURLBuilder.shared.urlServerInfo2 = buildConfigURL("https://funny-videos-2018-8b162.firebaseapp.com/configs-sdk28/", appId, "gz_data.json")
                    WallpaperURLBuilder.shared.notifyFormat = "notifies?lang=%s&os=android&firstopen=%s&lastopen=%s&type=%s&sentid=%s&mobileid=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.moreAppFormat = "apps?lang=%s&os=android&mobileid=%s&token=%s&appid=videohdwallpapertkv2secv10"
                    WallpaperURLBuilder.shared.clickAdvertisement = "clickadvertis?advid=%s&lang=%s&mobileid=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.feedbackFormat = "feedback?content=%s&mobileid=%s&lang=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.colorApiFormat = "colorhashtags?lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.trendingApiFormat = "hottrending?lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    //Wallpaper configs
                    WallpaperURLBuilder.shared.DEFAULT_SERVER = "http://ec2-34-211-239-59.us-west-2.compute.amazonaws.com/minwallpaper/rest/"
                    WallpaperURLBuilder.shared.SERVER_FAILED = "https://iamhereforyou.xyz/wallpapergz/rest/"
                    WallpaperURLBuilder.shared.DEFAULT_SERVER_NTF = "https://tpcom.xyz/notification/rest/"
                    WallpaperURLBuilder.shared.DEFAULT_STORAGE = "https://dieuphoiwallpaper.xyz/wallstorage/"
                    WallpaperURLBuilder.shared.DEFAULT_SERVER_VIDEO = "https://videoserver.tpwildcardserver.xyz/wallpaper/restcache/"
                    WallpaperURLBuilder.shared.DEFAULT_STORAGE_VIDEO = "https://tpvideos.sfo2.digitaloceanspaces.com/videowalls/"
                    WallpaperURLBuilder.shared.urlDefaultInfo2 = buildConfigURL("https://s3-us-west-2.amazonaws.com/configstorage/default/", AppInfo.shared.wallDefaultDataFileName, "gz_data.json")
                    //
                    WallpaperURLBuilder.shared.categoryFormat = "categories?lang=%s&offset=%s&limit=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.categorySearchFormat = "categories?q=%s&lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.wallpaperByCategoryFormat = "wallpapers?cat=%s&lang=%s&offset=%s&limit=%s&order=downDate_desc&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.format = "%s?q=%s&lang=%s&offset=%s&limit=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.hashtagTrendFormat = "hashtagtrendsv2?lang=%s&type=keyword&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.downloadFormat = "download/%s?type=%s&mtype=%s&lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.hashtagAllFormat = "hashtags?lang=%s&type=keyword&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.defaultWallFormat = "defaultwallsv2?hashtags=%s&country=%s&page=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId

                    //
                    WallpaperURLBuilder.shared.videoWallpaperFormat = "videowalls?type=%s&hashtags=%s&page=%s&lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.parallaxWallpaperFormat = "parallax?type=%s&hashtags=%s&page=%s&lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.wallpaperRequest = "requirewall?topic=%s&desc=%s&email=%s&lang=%s&mobileid=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.videoSearchPath = "search?hashtags=%s&lang=%s&offset=%s&limit=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.wallpaperImage4D = "parallax?type=home&hashtags=%s&page=%s&lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId

                    WallpaperURLBuilder.shared.topDownFormat = "walltopdown?lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.topNewFormat = "walltopnew?lang=%s&mobileid=%s&sex=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.originStorageFormat = "https://phohuongyen.com/originstorage/get-storage.php?lang=%s&token=%s&appid=" + appId
                    WallpaperURLBuilder.shared.wallpaperServerFailed = "https://wallfaild.tpserverfaild.xyz/wallpapergz/rest/"
                    WallpaperURLBuilder.shared.videoServerFailed = "https://wallvideo.tpserverfaild.xyz/wallpaper/restcache/"
                    WallpaperURLBuilder.shared.imageStorageFailed = "https://storageus.hotgirlwallpaper.xyz/wallstorage/"
                    System.setProperty("tpmonitoring.publicKey", "00c5L2B3zEyd")
                    System.setProperty("tpmonitoring.secretKey", "d5U1l2e3KNRj")
                    DEFAULT_ENDPOINT = "https://tpmonitorwall.tpwildcardserver.xyz"
                }
            } catch (e: Exception) {
                Logger.e(e, "")
            }
        }

        private fun buildConfigURL(vararg params: String): String {
            var url = ""
            for (element in params) {
                url += element
            }
            return url
        }

        fun getToken(context: Context): String = SecurityToken.token ?: ""

        fun clear() {
            onLoadedConfig = false
        }
    }
}