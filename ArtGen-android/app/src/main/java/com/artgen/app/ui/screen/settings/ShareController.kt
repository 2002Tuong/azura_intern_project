package com.artgen.app.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.artgen.app.BuildConfig
import com.artgen.app.R
import java.util.Locale

class ShareController(private val context: Context) {

    fun openTermOfService() {
        openUrl("https://docs.google.com/document/d/e/2PACX-1vS91CCbKe9UzdMqwpuxrI1oDpopTKEP0oiHnM7v7qTxJ1Xiek5_hgh-Wrp0cH--cWCoIMTLMbQvuevv/pub")
    }

    fun openPrivacy() {
        openUrl("https://docs.google.com/document/d/e/2PACX-1vRlmsy8dxlEsjSjNO2-o_oeDWyo64189ujeji6gXvPmuqPybHQ6vWwEAmh83IrEe2FLD3PbNhtgC_dw/pub")
    }

    private fun openUrl(url: String) {
        runCatching {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).also {
                context.startActivity(it)
            }
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
}
