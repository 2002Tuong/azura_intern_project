package com.parallax.hdvideo.wallpapers.extension

import android.app.Activity
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BasePagerAdapter
import com.parallax.hdvideo.wallpapers.utils.dpToPx

fun <T : View> Activity.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return viewLazy { findViewById<T>(idRes) }
}

fun <T : View> Fragment.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return viewLazy { view!!.findViewById<T>(idRes) }
}

fun <T : View> View.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return viewLazy { findViewById<T>(idRes) }
}

fun <T : View> RecyclerView.ViewHolder.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return viewLazy { itemView.findViewById<T>(idRes) }
}

fun <T : View> BasePagerAdapter.ViewHolder.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return viewLazy { itemView.findViewById<T>(idRes) }
}

private fun <T> viewLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

fun setOnClickListener(vararg views: View, onClick: (View) -> Unit) {
    views.forEach { it.setOnClickListener {v ->  onClick.invoke(v) } }
}

fun setOnClickListener(vararg views: View, onClickListener : View.OnClickListener) {
    views.forEach { it.setOnClickListener(onClickListener) }
}

//fun setOnClickListener(vararg views: View, onClick: KFunction<Unit>) {
//    views.forEach { it.setOnClickListener { onClick} }
//}

//fun setListClickListener(views: Array<View>, onClick: View.OnClickListener) {
//    views.forEach { it.setOnClickListener(onClick) }
//}

fun setVisibilityViews(vararg views: View, visibility: Int) {
    views.forEach { it.visibility = visibility }
}

fun setHiddenViews(vararg views: View, isHidden: Boolean = true) {
    views.forEach { it.isHidden = isHidden }
}

fun View.margin(
    left: Float? = null,
    top: Float? = null,
    right: Float? = null,
    bottom: Float? = null
) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.apply { leftMargin = dpToPx(this) }
        top?.apply { topMargin = dpToPx(this) }
        right?.apply { rightMargin = dpToPx(this) }
        bottom?.apply { bottomMargin = dpToPx(this) }
    }
}

var View.isHidden: Boolean
    get() {
        return visibility == View.GONE
    }
    set(value) {
        if (value) {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        } else {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
    }

var View.isHiddenAnimate: Boolean
    get() {
        return visibility == View.GONE
    }
    set(value) {
        if (value) {
            if (visibility == View.VISIBLE) {
                animate().cancel()
                animate().alpha(0f).setDuration(250).setInterpolator(LinearInterpolator()).withEndAction {
                    visibility = View.GONE
                }.start()
            } else {
                alpha = 0f
            }
        } else {
            if (visibility != View.VISIBLE) {
                animate().cancel()
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(250).setInterpolator(LinearInterpolator()).start()
            } else {
                alpha = 1f
            }
        }
    }

var View.isInvisible: Boolean
    get() {
        return visibility == View.INVISIBLE || visibility == View.GONE
    }
    set(value) {
        visibility = if (value) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

class DebounceOnClickListener(
    private val interval: Long,
    private val callback: (View) -> Unit
): View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View) {
        val time = System.currentTimeMillis()
        if (time - lastClickTime >= interval) {
            lastClickTime = time
            callback(v)
        }
    }
}

fun View.setOnClickListenerDebounce(debounceInterval: Long, onClick: (View) -> Unit) =
    setOnClickListener(DebounceOnClickListener(debounceInterval, onClick))

fun ViewGroup.animateFallDown() {
    val animate = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_fall_down_anim)
    layoutAnimation = animate
}

fun TextView.setGradientTextView(isSelected: Boolean) {
    val paint = this.paint
    val width = paint.measureText(this.text.toString())
    val textShader = LinearGradient(
        0f, 0f, width, this.textSize, intArrayOf(
            Color.parseColor("#436ADD"),
            Color.parseColor("#9541E5"),
        ), null, Shader.TileMode.MIRROR
    )
    if (isSelected) {
        this.paint.shader = textShader
    } else {
        this.paint.shader = null
        this.setTextColor(ContextCompat.getColor(this.context, R.color.white))
    }
}

