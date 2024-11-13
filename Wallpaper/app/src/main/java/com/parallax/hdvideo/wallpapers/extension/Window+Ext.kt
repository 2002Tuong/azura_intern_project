package com.parallax.hdvideo.wallpapers.extension

import android.view.View
import android.view.Window

fun Window.setStatusBar(show: Boolean, isLight: Boolean = false) {

    var isVisible = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

    isVisible = if (show) isVisible else isVisible or View.SYSTEM_UI_FLAG_FULLSCREEN
    if (!isLight) {
        isVisible = isVisible or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    decorView.systemUiVisibility = isVisible
}

fun Window.setLightStatusBar(isLight: Boolean) {
    decorView.systemUiVisibility =  if (isLight) decorView.systemUiVisibility and  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                                    else decorView.systemUiVisibility or  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

}