package com.parallax.hdvideo.wallpapers.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.extension.memoryInGB
import com.parallax.hdvideo.wallpapers.extension.round
import org.jetbrains.annotations.TestOnly
import kotlin.math.abs
import kotlin.properties.Delegates

object AppConfiguration {

    lateinit var connectivityManagerInstance: ConnectivityManager
    lateinit var displayMetrics: DisplayMetrics
    lateinit var appName: String
    var statusBarSize by Delegates.notNull<Int>()
    //First : HashTag, Second: Device Name
    var topTenDeviceInfo : Pair<String, String>? = null
    val lowMemory :  Boolean by lazy { WallpaperApp.instance.memoryInGB <= 2.0 }

    val widthScreenValue: Int
        get() = displayMetrics.widthPixels

    val heightScreenValue: Int
        get() = displayMetrics.heightPixels

    val ARRAY_ASPECT_RATIO = floatArrayOf(1.78f, 2.0f, 2.06f, 2.11f, 2.14f, 2.17f, 2.22f)
    //index from 0 to 6
    var INDEX_OF_ASPECT_RATIO = 0
    val RATIO_SCREEN_DATA = arrayOf("", "/02_18x9", "/03_185x9", "/04_19x9", "/05_193x9", "/06_195x9", "/07_20x9")
    // set value 1.78f or 2.0f... for testing
    var aspectRatio: Float = 1.78f

    fun setup(context: Context) {
        appName = context.getString(R.string.app_name)
        connectivityManagerInstance = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        displayMetrics = getScreen(context)
        statusBarSize = getStatusBarHeight(context)
        setupTop10Device()
        aspectRatio = (heightScreenValue.toFloat() / widthScreenValue).round(2)
        INDEX_OF_ASPECT_RATIO = getAspectRatioIndex(aspectRatio)
    }

    @TestOnly
    fun forTesting() {
        val aspectRatio = WallpaperApp.instance.localStorage.aspectRatio
        if (aspectRatio != null)
            this.aspectRatio = aspectRatio
        INDEX_OF_ASPECT_RATIO = getAspectRatioIndex(this.aspectRatio)
    }

    private fun getAspectRatioIndex(ratio : Float) : Int {
        val index = ARRAY_ASPECT_RATIO.binarySearch(ratio)
        if (index < 0) {
            return abs(index) - 1
        }
        return index
    }

    private fun setupTop10Device() {
        if (BuildConfig.DEBUG) {
            topTenDeviceInfo = Pair("hinhnensamsunggalaxya71", "")
        } else {
            AppConstants.TOP_10_DEVICES.forEach { (key, value) ->
                if (value.contains(Build.DEVICE, true) && value.contains(Build.BRAND, true)) {
                    topTenDeviceInfo = Pair(key, value)
                    return
                }
            }
        }
    }

    private fun getScreen(context: Context): DisplayMetrics {
        val windowManagerInstance = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics1 = DisplayMetrics()
        windowManagerInstance.defaultDisplay.getRealMetrics(displayMetrics1)
        return displayMetrics1
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) { context.resources.getDimensionPixelSize(resourceId) } else 0
    }
}

fun dpToPx(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, AppConfiguration.displayMetrics).toInt()

fun dp2Px(dp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, AppConfiguration.displayMetrics)

fun pxToDp(px: Int): Float =
    px / AppConfiguration.displayMetrics.density

fun spToPx(sp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, AppConfiguration.displayMetrics)