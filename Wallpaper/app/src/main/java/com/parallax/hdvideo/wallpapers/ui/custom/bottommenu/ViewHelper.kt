package com.parallax.hdvideo.wallpapers.ui.custom.bottommenu

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import com.parallax.hdvideo.wallpapers.R

object ViewHelper {
    @JvmStatic
    fun getThemeAccentColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, value, true)
        return value.data
    }

    @JvmStatic
    fun updateDrawableColor(drawable: Drawable?, color: Int) {
        if (drawable == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) drawable.setTint(color) else drawable.setColorFilter(
            color,
            PorterDuff.Mode.SRC_ATOP
        )
    }
}