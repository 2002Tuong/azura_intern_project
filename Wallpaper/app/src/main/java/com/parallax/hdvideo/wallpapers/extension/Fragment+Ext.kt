package com.parallax.hdvideo.wallpapers.extension

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ui.base.NavigationController.Companion.FRAGMENT_NAME_TAG
import com.parallax.hdvideo.wallpapers.ui.base.activity.BaseActivity
import kotlin.reflect.KClass

fun Fragment.pushFragment(fragment: Fragment, bundle: Bundle? = null, tag: String? = null, animate: Boolean = true, viewId: Int = 0, singleton: Boolean = false) {
    val curActivity = activity as? BaseActivity ?: return
    curActivity.navigationController.pushFragment(fragment = fragment, bundle = bundle, tag = tag,  animate = animate, viewId = viewId, singleton = singleton,parentTag = arguments?.getString(FRAGMENT_NAME_TAG))
}

fun Fragment.pushFragment(clazz: KClass<out Fragment>, bundle: Bundle? = null, tag: String? = null, animate: Boolean = true, singleton: Boolean = false) {
    val curActivity = activity as? BaseActivity ?: return
    curActivity.navigationController.pushFragment(clazz = clazz, bundle = bundle, tag = tag, animate = animate, singleton = singleton)
}

fun Fragment.popFragment(clazz: KClass<out Fragment>? = null, animate: Boolean = true) : Boolean {
    val curActivity = activity as? BaseActivity ?: return false
    if (clazz == null) {
        return curActivity.navigationController.popFragment(this, animate)
    }
    return curActivity.navigationController.popFragment(clazz = clazz, animate = animate)
}

fun Fragment.popToRoot(animate: Boolean = true) {
    val curActivity = activity as? BaseActivity ?: return
    return curActivity.navigationController.popToRoot(animate = animate)
}

fun Fragment.removeFragment(clazz: KClass<out Fragment>? = null, tag: String? = null) {
    val curActivity = activity as? BaseActivity ?: return
    when {
        clazz != null -> {
            curActivity.navigationController.removeFragment(clazz)
        }
        tag != null -> {
            curActivity.navigationController.removeFragment(tag)
        }
        else -> {
            curActivity.navigationController.removeFragment(this)
        }
    }
}

fun <T: Fragment>Fragment.findFragment(clazz: KClass<T>, tag: String? = null) : T? {
    return (activity as? BaseActivity)?.findFragment(clazz, tag)
}

fun Fragment.popFragment2(fragment: Fragment? = null, clazz: KClass<out Fragment>? = null, tag: String? = null, animateRightOrLeft: Boolean = false) : Boolean {
    val curActivity = activity as? BaseActivity ?: return false
    if (fragment == null && clazz == null && tag == null) {
        return curActivity.navigationController.popFragment2(this, animateRightOrLeft = animateRightOrLeft)
    }
    return curActivity.navigationController.popFragment2(fragment = fragment, clazz = clazz, tag = tag, animateRightOrLeft = animateRightOrLeft)
}

fun Fragment.transitionInLeft() {
    val context = context
    if (context != null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_left_anim)
        view?.startAnimation(anim)
    }
}

fun Fragment.transitionOutLeft() {
    val context = context
    if (context != null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.slide_out_left_anim)
        view?.startAnimation(anim)
    }
}

fun Fragment.showLoading(touchOutside: Boolean = false, canGoBack: Boolean = true) {
    (activity as? BaseActivity)?.showLoading(touchOutside = touchOutside, canGoBack = canGoBack)
}

fun Fragment.dismissLoading() {
    (activity as? BaseActivity)?.dismissLoading()
}