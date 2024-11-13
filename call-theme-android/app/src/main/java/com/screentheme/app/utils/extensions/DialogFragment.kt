package com.screentheme.app.utils.extensions

import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

fun DialogFragment.setWidthPercent(percentage: Float) {
    val window = dialog!!.window
    val size = Point()
    val display = window!!.windowManager.defaultDisplay
    display.getSize(size)
    window.setLayout((size.x * percentage).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    window.setGravity(Gravity.CENTER)
}