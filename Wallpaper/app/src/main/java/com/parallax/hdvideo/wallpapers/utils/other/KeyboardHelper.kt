package com.parallax.hdvideo.wallpapers.utils.other

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.parallax.hdvideo.wallpapers.WallpaperApp
import java.lang.ref.WeakReference
import kotlin.math.hypot

object KeyboardHelper {

    private var focusedViewPointOnActionDown: WeakReference<View>? = null
    private var isTouchWasInsideFocusedView = false
    private  var hasMoved = false
    private var rawXValue = 0f
    private  var rawYValue = 0f

    fun touchEvent(ev: MotionEvent, currentFocus: View?) {
        when (ev.action and ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                rawXValue = ev.rawX
                rawYValue = ev.rawY
                hasMoved = false
                if (currentFocus != null) {
                    focusedViewPointOnActionDown = WeakReference(currentFocus)
                    val rect = Rect()
                    val coordinates = IntArray(2)
                    currentFocus.getLocationOnScreen(coordinates)
                    rect[coordinates[0], coordinates[1], coordinates[0] + currentFocus.width] =
                        coordinates[1] + currentFocus.height
                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    isTouchWasInsideFocusedView = rect.contains(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!hasMoved) {
                    val delta = hypot(rawXValue - ev.rawX.toDouble(), rawYValue - ev.rawY.toDouble()).toFloat()
                    hasMoved = delta > 6f
                } else {
                    focusedViewPointOnActionDown = null
                }
            }
            MotionEvent.ACTION_UP ->  {
                val view = view ?: return
                if (hasMoved || view.context == null) {
                    focusedViewPointOnActionDown = null
                    return
                }
                if (currentFocus == view) {
                    if (isTouchWasInsideFocusedView) {
                        focusedViewPointOnActionDown = null
                        return
                    }
                } else if (currentFocus is EditText) {
                    focusedViewPointOnActionDown = null
                    return
                }
                hideKeyboard()
                focusedViewPointOnActionDown = null
            }
            MotionEvent.ACTION_CANCEL -> {
                focusedViewPointOnActionDown = null
            }
        }
    }

    private fun hideKeyboard() {
        val view = this.view as? EditText ?: return
        focusedViewPointOnActionDown = null
        val inputMethodManager = WallpaperApp.instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private val view get() = focusedViewPointOnActionDown?.get()

    fun clear() {
        isTouchWasInsideFocusedView = false
        focusedViewPointOnActionDown = null
    }

}