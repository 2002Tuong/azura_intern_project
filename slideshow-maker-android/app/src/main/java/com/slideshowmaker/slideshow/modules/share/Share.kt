package com.slideshowmaker.slideshow.modules.share

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.ads.control.admob.AppOpenManager
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.extentions.getUriFromFile
import java.io.File

class Share {
    companion object {
        const val YOUTUBE_PACKAGE_NAME = "com.google.android.youtube"
        const val FACEBOOK_PACKAGE_NAME = "com.facebook.katana"
        const val TIKTOK_PACKAGE_NAME = "com.zhiliaoapp.musically"
        const val TIKTOK_PACKAGE2_NAME = "com.ss.android.ugc.trill"
        const val TWITTER_PACKAGE_NAME = "com.twitter.android"
        const val INSTAGRAM_PACKAGE_NAME = "com.instagram.android"
        const val MESSENGER_PACKAGE_NAME = "com.facebook.orca"
        const val GMAIL_PACKAGE_NAME = "com.google.android.gm"
        const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"
        const val LINE_PACKAGE_NAME = "jp.naver.line.android"
        const val VIDEO_TYPE = "video/*"
        const val BASE_URI = "content://"
        const val APP_ID = "com.app.videoedittor"
    }

    var shareType: String


    constructor() {
        this.shareType = VIDEO_TYPE
    }

    fun shareTo(
        context: Context,
        filePath: String,
        packageId: String,
        onFailure: ((String) -> Unit)? = null
    ) {
        AppOpenManager.getInstance().disableAdResumeByClickAction()
        if (packageId == TIKTOK_PACKAGE_NAME) {
            try {
                val app: ApplicationInfo =
                    context.packageManager.getApplicationInfo(TIKTOK_PACKAGE_NAME, 0)
            } catch (exception: Exception) {
                shareTo(
                    context,
                    filePath,
                    TIKTOK_PACKAGE2_NAME
                )
            }
        }
        runCatching {
            val uri = context.getUriFromFile(File(filePath))
            val intentShare = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                setPackage(packageId)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val intent = Intent.createChooser(intentShare, "Share").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }.onFailure {
            openMoreShare(context, filePath)
        }
    }

    fun openMoreShare(context: Context, filePath: String) {
        AppOpenManager.getInstance().disableAdResumeByClickAction()
        val intentShare = Intent().apply {
            action = Intent.ACTION_SEND
            type = shareType

        }
        if (Build.VERSION.SDK_INT < 24) {
            intentShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
        } else {
            intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intentShare.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(filePath)
                )
            )
        }
        context.startActivity(intentShare)
    }

    fun shareApp(context: Context, appId: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=$appId"
        )

        context.startActivity(shareIntent)
    }

    fun openStore(context: Context, packageId: String) {
        try {
            val intent = Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("market://details?id=$packageId")
            }
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            Logger.e(e.toString())
        }
    }
}