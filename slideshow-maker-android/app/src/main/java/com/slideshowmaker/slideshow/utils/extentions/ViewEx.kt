package com.slideshowmaker.slideshow.utils.extentions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.*

fun View.getColor(resId:Int):Int = ResourcesCompat.getColor(context.resources, resId, null)

fun View.fadeInAnimation() {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 250
    }.start()
}

fun View.scaleAnimation() {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1f)
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.8f, 1f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 300
    }.start()
}

fun View.fadeOutAnimation(onEnd:()->Unit) {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.9f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.9f)
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
   ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 250
        addListener(object :Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                onEnd.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationStart(animation: Animator) {

            }

        })
    }.start()

}

fun View.delayOnLifecycle(
    duration: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: () -> Unit,
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        delay(duration)
        block()
    }
}