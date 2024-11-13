package com.screentheme.app.utils.extensions

import android.animation.ValueAnimator
import android.view.animation.Animation
import android.widget.ImageView

fun ImageView.animateImageViewZoom(duration: Long) {
    val animator = ValueAnimator.ofFloat(0.7f, 1f)
    animator.duration = duration
    animator.addUpdateListener { animation ->
        this.scaleX = animation.animatedValue as Float
        this.scaleY = animation.animatedValue as Float
    }
    animator.repeatCount = Animation.INFINITE
    animator.repeatMode = ValueAnimator.REVERSE
    animator.start()
}