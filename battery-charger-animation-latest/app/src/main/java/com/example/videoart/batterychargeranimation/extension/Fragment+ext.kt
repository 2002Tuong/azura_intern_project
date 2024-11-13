package com.example.videoart.batterychargeranimation.extension

import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavDirections

class FragmentDirections private constructor() {
    companion object {
        fun action(bundle: Bundle, actionId: Int): NavDirections {
            return object : NavDirections {

                override val actionId: Int
                    get() = actionId
                override val arguments: Bundle
                    get() = bundle
            }
        }
    }
}

fun DialogFragment.setWidthPercent(percentage: Float) {
    val window = dialog!!.window
    val size = Point()
    val display = window!!.windowManager.defaultDisplay
    display.getSize(size)
    window.setLayout((size.x * percentage).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    window.setGravity(Gravity.CENTER)
}