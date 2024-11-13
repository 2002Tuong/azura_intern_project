package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.switchmaterial.SwitchMaterial

open class CustomSwitch: SwitchMaterial {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.actionMasked == MotionEvent.ACTION_MOVE) {
            true
        } else super.onTouchEvent(ev)
    }

    fun setSwitched(checked: Boolean) {
        super.setChecked(checked)
    }

    override fun setChecked(checked: Boolean) {

    }

    fun onOff() {
        super.setChecked(!isChecked)
    }

    override fun toggle() {

    }
}