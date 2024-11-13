package com.screentheme.app.utils.extensions

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat


fun Button.setRoundedBackgroundWithPadding(context: Context, drawableResId: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
    val paddingHorizontal = paddingStart + paddingEnd
    val paddingVertical = paddingTop + paddingBottom

    background = drawable

    // Set the padding programmatically
    val paddingLeft = compoundDrawablePadding
    val paddingRight = compoundDrawablePadding
    val paddingTop = compoundDrawablePadding
    val paddingBottom = compoundDrawablePadding

    setPadding(
        paddingHorizontal + paddingLeft,
        paddingVertical + paddingTop,
        paddingHorizontal + paddingRight,
        paddingVertical + paddingBottom
    )
}

fun View.setBackgroundAndKeepPadding(drawableResId: Int) {

    val backgroundDrawable = context.getDrawable(drawableResId) ?: return

    val padding = Rect()
    backgroundDrawable.getPadding(padding)
    val top = 10
    val left = 16
    val right = 16
    val bottom = 10

    val verSdk = android.os.Build.VERSION.SDK_INT
    if (verSdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        this.setBackgroundDrawable(backgroundDrawable)
    } else {
        this.background = backgroundDrawable
    }
    this.setPadding(left, top, right, bottom)
}
