package com.slideshowmaker.slideshow.utils

import android.content.Context
import com.slideshowmaker.slideshow.VideoMakerApplication

object DimenUtils {
    fun screenWidth(context: Context) : Int = context.resources.displayMetrics.widthPixels

    fun screenHeight(context: Context) : Int = context.resources.displayMetrics.heightPixels

    fun density(context: Context) : Float = context.resources.displayMetrics.density

    fun density() : Float = VideoMakerApplication.getContext().resources.displayMetrics.density

    fun videoPreviewScale():Float {
        val context = VideoMakerApplication.getContext()
        val screenHeightValue = screenHeight(context)
        val screenWidthValue = screenWidth(context)
        val toolAreaHeightMin = 356*density(context)

        return if((screenHeightValue-toolAreaHeightMin) < screenWidthValue) {
            (screenHeightValue-toolAreaHeightMin)/screenWidthValue
        } else {
            1f
        }
    }


    fun videoScaleInTrim():Float {
        val context = VideoMakerApplication.getContext()
        val screenHeightValue = screenHeight(context)
        val screenWidthValue = screenWidth(context)
        val toolAreaHeightMin = 236*density(context)

        return if((screenHeightValue-toolAreaHeightMin) < screenWidthValue) {
            (screenHeightValue-toolAreaHeightMin)/screenWidthValue
        } else {
            1f
        }
    }

}