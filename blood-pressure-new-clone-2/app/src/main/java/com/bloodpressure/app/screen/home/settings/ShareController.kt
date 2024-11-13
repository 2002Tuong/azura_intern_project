package com.bloodpressure.app.screen.home.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.bloodpressure.app.BuildConfig
import com.bloodpressure.app.R
import java.util.Locale

class ShareController(private val context: Context) {

    fun shareApp() {
        kotlin.runCatching {
            val shareMessage =
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }

    fun sendFeedback(body: String? = null) {
        runCatching {
            val subject =
                "Feedback ${context.getString(R.string.app_name)} ver ${BuildConfig.VERSION_NAME}"
            val extraText =
                "${body.orEmpty()}\n\n\n-------------\n${context.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}, ${Build.MODEL}, Android ${Build.VERSION.RELEASE}, ${Locale.getDefault()}"
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.contact_email)))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, extraText)
            }
            context.startActivity(emailIntent)
        }
    }

    fun openStore() {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                )
            )
        }
    }

    fun openAppStore(url: String) {
        runCatching {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }
    }

    fun shareFile(uri: Uri) {
        kotlin.runCatching {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share"))
        }.onFailure {
            Toast.makeText(context, "Cannot share medias!", Toast.LENGTH_SHORT).show()
        }
    }

    fun openTermOfService() {
        openUrl("https://docs.google.com/document/d/1d5-q2fCg-iMQyEROPIoVV0Lb0FD1TTAj/edit?usp=drive_link&ouid=110508997680483854337&rtpof=true&sd=true")
    }

    fun openPrivacy() {
        openUrl("https://docs.google.com/document/d/1FtwASfPDmIsu7pa5RcGzhQ3vk-hcpZzf/edit?usp=drive_link&ouid=110508997680483854337&rtpof=true&sd=true")
    }

    private fun openUrl(url: String) {
        runCatching {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).also {
                context.startActivity(it)
            }
        }
    }
}
