package com.screentheme.app.utils

import android.os.Handler
import android.util.TypedValue
import android.view.View
import androidx.appcompat.R
import androidx.core.content.ContextCompat
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.data.remote.config.RemoteConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun delay(milliseconds: Long = 300, action: () -> Unit) {
    Handler().postDelayed(action, milliseconds)
}

fun Calendar.getTimeFormattedString(simpleDateFormatString: String): String {
    val format = SimpleDateFormat(simpleDateFormatString, Locale.getDefault())

    return format.format(time)
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, this, true)
    foreground = ContextCompat.getDrawable(context, resourceId)
}

val screenType get() = AppRemoteConfig.languageScreenType
const val SCREEN_TYPE_0 = "option0"
const val SCREEN_TYPE_1 = "option1"
const val SCREEN_TYPE_2 = "option2"