package com.parallax.hdvideo.wallpapers.utils.wallpaper

import android.app.ActivityManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.extension.memoryInGB
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.services.log.EventSetWall
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.services.wallpaper.LiveWallpaper
import com.parallax.hdvideo.wallpapers.utils.other.ImageHelper.crop
import java.io.IOException

object WallpaperHelper {

    private val UNSUPPORTED_INCREMENTAL = listOf("V10.2.2.0.MALMIXM")

    fun setWallpaper(bitmap: Bitmap, type: WallpaperType = WallpaperType.HOME, allowCrop : Boolean = true, wallModel: WallpaperModel? = null) : Boolean {
        val wallpaperManager = WallpaperManager.getInstance(WallpaperApp.instance)
        var error: IOException? = null
        try {
            val transformationBitmap = if (allowCrop) bitmap.crop() else bitmap
            when(type) {
                WallpaperType.HOME -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(transformationBitmap, null, false, WallpaperManager.FLAG_SYSTEM)
                    } else {
                        wallpaperManager.setBitmap(transformationBitmap)
                    }
                }
                WallpaperType.LOCK -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(transformationBitmap, null, false, WallpaperManager.FLAG_LOCK)
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            transformationBitmap,
                            null,
                            false,
                            WallpaperManager.FLAG_SYSTEM
                        )
                        wallpaperManager.setBitmap(
                            transformationBitmap,
                            null,
                            false,
                            WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        wallpaperManager.setBitmap(transformationBitmap)
                    }
                }
            }
            TrackingSupport.recordEventOnlyFirebase(EventSetWall.SetWallpaperImageSuccess)
            return true
        } catch (e: IOException) {
            error = e
        }
        TrackingSupport.recordEventOnlyFirebase(EventSetWall.SetWallpaperImageFail)
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun isSupported(): Boolean {
        val manager = WallpaperManager.getInstance(WallpaperApp.instance)
        return manager.isWallpaperSupported && manager.isSetWallpaperAllowed
    }

    fun reset(): Boolean {
        try {
            WallpaperManager.getInstance(WallpaperApp.instance).clearWallpaper()
            return true
        } catch (e: IOException) {

        }
        return false
    }

    enum class WallpaperType {
         HOME, LOCK, BOTH;

        companion object {
            fun init(index: Int) : WallpaperType {
                return when(index) {
                    2 -> LOCK
                    3 -> BOTH
                    else -> HOME
                }
            }
        }
    }

    val isSupportLiveWallpaper: Boolean by lazy {
        var checkIsSupport = false
        if (WallpaperApp.instance.memoryInGB > 1.7 && RemoteConfig.commonData.isSupportedDevice) {
            val packageManager = WallpaperApp.instance.packageManager
            val featuresList = packageManager.systemAvailableFeatures
            val activeLiveWallpaperActivity: Boolean =
                checkAppsForIntent(
                    WallpaperApp.instance
                )
            for (f in featuresList) {
                if (PackageManager.FEATURE_LIVE_WALLPAPER == f.name && !isUnsupportIncremental() && activeLiveWallpaperActivity) {
                    checkIsSupport = true
                    break
                }
            }
        }
        checkIsSupport
    }

    private fun checkAppsForIntent(context: Context): Boolean {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(context, LiveWallpaper::class.java))
        val packageManager = context.packageManager
        return intent.resolveActivity(packageManager) != null
    }

    private fun isUnsupportIncremental(): Boolean {
        return UNSUPPORTED_INCREMENTAL.contains(Build.VERSION.INCREMENTAL)
    }

    fun isSupport4DImage(context: Context): Boolean {
        return isSupportedOpenGL(context) && isHasAccelerometer(context)
    }

    private fun isHasAccelerometer(context: Context): Boolean {
        val sensorManagerInstance = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManagerInstance.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return accelerometerSensor != null
    }

    private fun isSupportedOpenGL(context: Context): Boolean {
        val activityService: Any = context.getSystemService(Context.ACTIVITY_SERVICE)
        val configurationInfo = (activityService as ActivityManager).deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x20000 || isProbablyEmulator()
    }

    private fun isProbablyEmulator(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")))
    }

}