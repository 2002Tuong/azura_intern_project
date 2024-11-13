package com.slideshowmaker.slideshow.ui.custom

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.TrackingEvent
import com.slideshowmaker.slideshow.databinding.ViewSnackMessageBinding

internal class SnackMessageView(context: Context) : LinearLayout(context) {
    private var layoutBinding: ViewSnackMessageBinding

    init {
        layoutBinding = ViewSnackMessageBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setMessage(message: CharSequence) {
        layoutBinding.snackMessage.text = message
    }

    fun setIcon(@DrawableRes iconResId: Int) {
        layoutBinding.snackIcon.setImageResource(iconResId)
    }

    fun setOnClosePressedListener(onClose: () -> Unit) {
        layoutBinding.snackPrimaryAction.setOnClickListener { onClose.invoke() }
    }

    fun adjustMargin(activity: Activity) {
        this.layoutParams = (this.layoutParams as? MarginLayoutParams)?.apply {
            setMargins(
                context.resources.getDimensionPixelSize(R.dimen.space_small),
                context.resources.getDimensionPixelSize(R.dimen.snack_message_margin_status_bar) +
                    activity.getStatusBarHeightInPx(),
                context.resources.getDimensionPixelSize(R.dimen.space_small),
                context.resources.getDimensionPixelSize(R.dimen.space_small),
            )
        }
    }

    fun setColor(@ColorRes backgroundColorRes: Int) {
        layoutBinding.rootCardView.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColorRes))
    }
}

class SnackMessage private constructor(builder: Builder) {
    private var message: String
    private var iconResId: Int
    private var backgroundResId: Int
    private val duration: Long
    private lateinit var snackMessageView: SnackMessageView
    private lateinit var rootView: ViewGroup
    private lateinit var openAnimation: Animation
    private lateinit var exitAnimation: Animation
    private val dismissRunnable = Runnable {
        dismiss()
    }

    init {
        message = builder.message
        iconResId = builder.resIcon
        backgroundResId = builder.type.backgroundColor
        duration = builder.duration.durationInMillis
    }

    fun show(activity: Activity, viewGroup: ViewGroup? = null) {
        snackMessageView = SnackMessageView(activity)
        snackMessageView.setIcon(iconResId)
        snackMessageView.setMessage(message)
        snackMessageView.setColor(backgroundResId)
        snackMessageView.setOnClosePressedListener {
            dismiss()
        }
        rootView = viewGroup ?: activity.getRootView() ?: return
        exitAnimation =
            AnimationUtils.loadAnimation(activity, androidx.appcompat.R.anim.abc_fade_out)
        openAnimation =
            AnimationUtils.loadAnimation(activity, androidx.appcompat.R.anim.abc_fade_in)
        openAnimation.duration = DEFAULT_ANIMATION_DURATION
        rootView.addView(snackMessageView)
        setUpAfterMeasured(activity, rootView)
    }

    fun dismiss() {
        rootView.removeCallbacks(dismissRunnable)
        exitAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit

            override fun onAnimationEnd(animation: Animation?) {
                rootView.removeView(snackMessageView)
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit
        })
        snackMessageView.startAnimation(exitAnimation)
    }

    private fun setUpAfterMeasured(activity: Activity, view: ViewGroup) {
        view.afterMeasured {
            snackMessageView.adjustMargin(activity)
            snackMessageView.startAnimation(openAnimation)
            postDelayed(dismissRunnable, duration)
        }
    }

    class Builder {
        lateinit var message: String
            private set
        var resIcon: Int = R.drawable.icon_information_circle
            private set
        var type: Type = Type.ERROR
            private set
        var duration: Duration = Duration.MEDIUM
            private set
        var onLaunchEvent: TrackingEvent? = null
            private set
        var onCloseEvent: TrackingEvent? = null
            private set
        var onAutoDismissEvent: TrackingEvent? = null
            private set

        fun setMessage(message: String) = apply { this.message = message }
        fun setIconResId(iconResId: Int) = apply { this.resIcon = iconResId }
        fun setType(type: Type) = apply { this.type = type }
        fun setDuration(duration: Duration) = apply { this.duration = duration }
        fun build() = SnackMessage(this)
    }

    enum class Type(val backgroundColor: Int) {
        ERROR(R.color.orange_900), WARNING(R.color.greyscale_500), INFORMATIVE(R.color.greyscale_500),
        SUCCESS(R.color.gnt_green),
    }

    enum class Duration(val durationInMillis: Long) {
        SHORT(2000L), MEDIUM(3500L), LONG(5000L)
    }

    internal fun Activity?.getRootView(): ViewGroup? {
        if (this == null || window == null) {
            return null
        }
        return window.decorView as ViewGroup
    }

    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 100L
    }
}

internal fun Activity.getStatusBarHeightInPx(): Int {
    val rect = Rect()
    window.decorView.getWindowVisibleDisplayFrame(rect)
    if (rect.top > 0) {
        return rect.top
    }
    return resources.run {
        val resourceId = getIdentifier("status_bar_height", "dimen", "android")
        getDimensionPixelSize(if (resourceId > 0) resourceId else R.dimen.status_bar_height)
    }
}

fun <T : View> T.afterMeasured(f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}