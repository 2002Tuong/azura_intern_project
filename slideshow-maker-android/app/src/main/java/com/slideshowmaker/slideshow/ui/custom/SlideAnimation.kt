package com.slideshowmaker.slideshow.ui.custom

import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

class SlideAnimation(
    private val view: View,
    private val viewPosition: ViewPosition,
    isVisible: Boolean = false,
) {

    init {
        if (!isVisible) {
            view.doOnLayout {
                view.translation(viewPosition.translation, viewPosition.getHiddenPosition(it))
                view.isVisible = false
            }
        }
    }

    fun start(isVisible: Boolean, endAction: ((Boolean) -> Unit)? = null) {
        if (isVisible) {
            showViewWithAnimation(endAction)
        } else {
            hideWithAnimation(endAction)
        }
    }

    private fun showViewWithAnimation(endAction: ((Boolean) -> Unit)? = null) {
        if (view.isVisible) {
            return
        }

        view.isVisible = true
        view.animate()
            .translation(viewPosition.translation, 0f)
            .setDuration(DEFAULT_DURATION)
            .setInterpolator(BASE_INTERPOLATOR)
            .withEndAction { endAction?.invoke(true) }
    }

    private fun hideWithAnimation(endAction: ((Boolean) -> Unit)? = null) {
        if (!view.isVisible) {
            return
        }

        view.animate()
            .translation(viewPosition.translation, viewPosition.getHiddenPosition(view))
            .setDuration(DEFAULT_DURATION)
            .setInterpolator(BASE_INTERPOLATOR)
            .withEndAction {
                view.isVisible = false
                endAction?.invoke(false)
            }
    }

    private fun ViewPropertyAnimator.translation(
        translation: Translation,
        value: Float,
    ): ViewPropertyAnimator = when (translation) {
        Translation.X -> translationX(value)
        Translation.Y -> translationY(value)
        Translation.Z -> translationZ(value)
    }

    private fun View.translation(translation: Translation, value: Float) = when (translation) {
        Translation.X -> translationX = value
        Translation.Y -> translationY = value
        Translation.Z -> translationZ = value
    }

    /* ktlint-disable no-semi, trailing-comma-on-declaration-site */
    enum class ViewPosition(val translation: Translation) {
        TOP(Translation.Y) {
            override fun getHiddenPosition(view: View): Float =
                -(view.height.toFloat() + view.marginTop)
        },
        LEFT(Translation.X) {
            override fun getHiddenPosition(view: View): Float =
                -(view.width.toFloat() + view.marginLeft)
        },
        RIGHT(Translation.X) {
            override fun getHiddenPosition(view: View): Float =
                view.width.toFloat() + view.marginRight
        },
        BOTTOM(Translation.Y) {
            override fun getHiddenPosition(view: View): Float =
                view.height.toFloat() + view.marginBottom
        };

        abstract fun getHiddenPosition(view: View): Float
    }

    enum class Translation {
        X, Y, Z
    }

    companion object {
        private const val DEFAULT_DURATION = 200L
        private val BASE_INTERPOLATOR = DecelerateInterpolator()
    }
}
