package com.example.claptofindphone.utils.extensions

import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

fun DialogFragment.setWidthPercent(percentage: Float) {
    val dialogWindow = dialog!!.window
    val size = Point()
    val windowDisplay = dialogWindow!!.windowManager.defaultDisplay
    windowDisplay.getSize(size)
    dialogWindow.setLayout((size.x * percentage).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    dialogWindow.setGravity(Gravity.CENTER)
}