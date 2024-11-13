package com.slideshowmaker.slideshow.utils

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Environment
import android.os.StatFs
import android.view.View
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import kotlinx.android.synthetic.main.item_view_native_ads_in_my_studio.view.*
import kotlinx.android.synthetic.main.native_ad_big.view.*
import java.io.File

object Utils {

    fun convertSecToTimeString(sec: Int): String {
        return if (sec >= 3600) {
            val hour = zeroPrefix((sec / 3600).toString())
            val min = zeroPrefix(((sec % 3600) / 60).toString())
            val sec = zeroPrefix(((sec % 3600) % 60).toString())
            "$hour:$min:$sec"
        } else {
            val min = zeroPrefix(((sec % 3600) / 60).toString())
            val sec = zeroPrefix(((sec % 3600) % 60).toString())
            "$min:$sec"
        }
    }

    private fun zeroPrefix(string: String): String {
        if (string.length < 2) return "0$string"
        return string
    }

    fun getTextWidth(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }

    fun getTextHeight(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    fun getAvailableSpaceInMB(): Long {
        val DEFAULT_SIZE_KB = 1024L
        val DEFAULT_SIZE_MB = DEFAULT_SIZE_KB * DEFAULT_SIZE_KB
        val validSpace: Long
        try {
            val statFs = StatFs(getVideoAppDirectory())
            validSpace = statFs.availableBlocksLong * statFs.blockSizeLong
        } catch (e: java.lang.Exception) {
            return 120
        }
        return validSpace / DEFAULT_SIZE_MB
    }

    fun getVideoAppDirectory(): String? {
        val folder =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                VideoMakerApplication.getContext().getString(R.string.folder_name)
            )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder.absolutePath
    }

    fun bindBigNativeAds(nativeAd: NativeAd, adView: NativeAdView?) {
        if (adView == null) return

        adView.headlineView = adView.ad_headline
        adView.mediaView = adView.ad_media
        adView.bodyView = adView.ad_body
        adView.callToActionView = adView.ad_call_to_action
        adView.iconView = adView.ad_app_icon
        adView.priceView = adView.ad_price
        adView.starRatingView = adView.ad_stars
        adView.storeView = adView.ad_store
        adView.advertiserView = adView.ad_advertiser
        adView.ad_headline.text = nativeAd.headline

        if (nativeAd.body != null) {
            adView.ad_body.visibility = View.VISIBLE
            adView.ad_body.text = nativeAd.body
        } else {
            adView.ad_body.visibility = View.INVISIBLE
        }

        if (nativeAd.icon != null) {
            adView.ad_app_icon.visibility = View.VISIBLE
            adView.ad_app_icon.setImageDrawable(nativeAd.icon!!.drawable)
        } else {
            adView.ad_app_icon.visibility = View.INVISIBLE
        }

        if (nativeAd.callToAction != null) {
            adView.ad_call_to_action.visibility = View.VISIBLE
            adView.ad_call_to_action.text = nativeAd.callToAction

        } else {
            adView.ad_call_to_action.visibility = View.INVISIBLE
        }

        if (nativeAd.price != null) {
            adView.ad_price.visibility = View.VISIBLE
            adView.ad_price.text = nativeAd.price
        } else {
            adView.ad_price.visibility = View.INVISIBLE
        }

        if (nativeAd.store != null) {
            adView.ad_store.visibility = View.VISIBLE
            adView.ad_store.text = nativeAd.store
        } else {
            adView.ad_store.visibility = View.INVISIBLE
        }

        if (nativeAd.starRating != null) {
            adView.ad_stars.visibility = View.VISIBLE
            adView.ad_stars.rating = nativeAd.starRating!!.toFloat()
        } else {
            adView.ad_stars.visibility = View.INVISIBLE
        }

        if (nativeAd.advertiser != null) {
            adView.ad_advertiser.text = nativeAd.advertiser
            adView.visibility = View.VISIBLE
        } else {
            adView.visibility = View.INVISIBLE
        }

        adView.setNativeAd(nativeAd)

    }

/*    fun isInternetAvailable(): Boolean {
        return try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            //You can replace it with your name
            ipAddr.toString() != ""
        } catch (e: Exception) {
            false
        }
    }*/

    fun binSmallNativeAds(nativeAd: NativeAd, adView: NativeAdView?) {
        if (adView == null) return
        adView.iconView = adView.ad_app_icon_small
        adView.bodyView = adView.ad_body_small
        adView.callToActionView = adView.ad_call_to_action_small
        adView.headlineView = adView.ad_headline_small
        adView.advertiserView = adView.ad_attribution_small

        if (nativeAd.icon != null) {
            adView.ad_app_icon_small.setImageDrawable(nativeAd.icon!!.drawable)
            adView.ad_app_icon_small.visibility = View.VISIBLE
        } else {
            adView.ad_app_icon_small.visibility = View.GONE
        }

        if (nativeAd.callToAction != null) {
            adView.ad_call_to_action_small.text = nativeAd.callToAction
            adView.ad_call_to_action_small.visibility = View.VISIBLE
        } else {
            adView.ad_call_to_action_small.visibility = View.GONE
        }

        if (nativeAd.body != null) {
            adView.ad_body_small.text = nativeAd.body
            adView.ad_body_small.visibility = View.VISIBLE
        } else {
            adView.ad_body_small.visibility = View.GONE
        }

        if (nativeAd.headline != null) {
            adView.ad_headline_small.apply {
                text = nativeAd.headline
                visibility = View.VISIBLE
            }
        } else {
            adView.ad_headline_small.visibility = View.GONE
        }

        if (nativeAd.advertiser != null) {
            adView.ad_attribution_small.text = nativeAd.advertiser
            adView.ad_attribution_small.visibility = View.VISIBLE
        } else {
            adView.ad_attribution_small.visibility = View.GONE
        }
        adView.setNativeAd(nativeAd)
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = if (connectivityManager != null) {
            connectivityManager.activeNetworkInfo
        } else null
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun isInternetAvailable(): Boolean {
        return isOnline(VideoMakerApplication.getContext())

    }

    private const val mSharedPreferenceName = "ratio_data"
    private const val mRatioKey = "ratio_key"
/*    private fun getNowPlayingSharedPreferences(): SharedPreferences = VideoMakerApplication.getContext().applicationContext.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE)
    fun saveOutRatio(outRatio:String) {
        getNowPlayingSharedPreferences().edit()?.apply {
            putString(mRatioKey, outRatio)
            apply()
        }
    }

    fun getOutRatio():String {
        return getNowPlayingSharedPreferences().getString(mRatioKey, "1:1") ?: "1:1"
    }*/

    fun checkStorageSpace(listFilePath: ArrayList<String>): Boolean {
        var sumLength = 0L
        for (index in 0 until listFilePath.size) {
            val file = File(listFilePath[index])
            if (file.exists()) {
                val fileLength = file.length()
                sumLength += fileLength
            }
        }

        val freeSpace = getAvailableSpaceInMB() * 1024 * 1024

        Logger.e("currentFreeSpace = $freeSpace   totalFileLength = $sumLength")
        if (freeSpace > (sumLength * 2)) return true
        return false
    }

    fun convertSecondsToTime(seconds: Int): String {
        val timeStr: String
        var hour = 0
        var minute = 0
        var second = 0
        if (seconds <= 0) {
            return "00:00"
        } else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr =
                    "00:" + unitFormat(minute) + ":" + unitFormat(
                        second
                    )
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr =
                    unitFormat(hour) + ":" + unitFormat(
                        minute
                    ) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }

    private fun unitFormat(i: Int): String? {
        return if (i in 0..9) {
            "0$i"
        } else {
            "" + i
        }
    }

}

fun Activity.hideNavigationBar() {
    val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    window.decorView.systemUiVisibility = flags
    window.decorView.setOnSystemUiVisibilityChangeListener {
        if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            window.decorView.systemUiVisibility = flags
        }
    }
}