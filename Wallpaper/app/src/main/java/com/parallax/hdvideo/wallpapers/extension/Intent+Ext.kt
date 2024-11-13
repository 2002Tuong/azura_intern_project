package com.parallax.hdvideo.wallpapers.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import java.io.*

object IntentSupporter {

    fun openById(id: String?) {
        val url = id ?: return
        openById(WallpaperApp.instance, url, null)
    }

    private fun openById(context: Context, id: String, `is`: InputStream?) {
        var id = id
        if (`is` != null) {
            val bufferedReader =
                BufferedReader(InputStreamReader(`is`))
            try {
                bufferedReader.readLine()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            if (id.indexOf("referrer") < 0) {
                id += "&referrer=utm_source%3Dtpcom%26utm_medium%3D" + context.packageName
                    .replace(".", "_") + "%26utm_campaign%3Dmoreapp"
            }
            if (id.startsWith("http")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(id))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                } catch (anfe: ActivityNotFoundException) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e("Error: ", "tag", e)
        }
    }

    fun shareWallpaper(activity: Activity?, model: WallpaperModel) : Boolean {
        if (activity == null) return false
        val isVideo = model.isVideo
        return try {
            val uri = if (model.isFromLocalStorage) {
                Uri.parse(model.url)
            } else {
                val path = if (isVideo) model.pathCacheFullVideo ?: return false else model.pathCacheFull ?: return false
                var file = File(path)
                if (!isVideo) {
                    val toFile = FileUtils.getFileFromURL(model.toUrl())
                    FileUtils.copyFile(file, toFile)
                    if (toFile.exists()) file = toFile
                }
                FileProvider.getUriForFile(WallpaperApp.instance, BuildConfig.APPLICATION_ID + ".provider", file)
            }

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                val audioInfo = if (model.isContainSound || model.isFromLocalStorage) AppConstants.KEY_INTENT_VIDEO_WITH_SOUND else AppConstants.EMPTY_STRING
                putExtra(AppConstants.KEY_INTENT_SHARE, audioInfo)
                data = uri
                type = if (isVideo) "video/mp4"  else "image/jpg"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            activity.startActivity(Intent.createChooser(intent, "Share this Wallpaper!"))
            true
        } catch (e: Exception) {
            Logger.d("on share file exception e = ${e.message}")
            false
        }
    }


}