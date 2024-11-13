package com.parallax.hdvideo.wallpapers.ui.base

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.util.*
import kotlin.reflect.KClass

class NavigationControllerImp constructor(private val fragmentManager: FragmentManager)
    : NavigationController {

    private val listTagFragments = ArrayDeque<String>()
    var navigateCallback: NavigateCallback? = null
    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque, or returns {@code null} if this deque is empty.
     */

    override val peek: String?
        get() = listTagFragments.peek()

    /**
     * Retrieves and removes the head of the queue represented by this deque
     * (in other words, the first element of this deque), or returns
     * {@code null} if this deque is empty.
     */

    val poll: String?
        get() = listTagFragments.poll()

    override fun popToRoot(animate: Boolean) {
        val first = peek
        var last = peek
        while (last != null) {
            popFragment(tag = last, animate = false)
            last = peek
        }
        popFragment(tag = first, animate = animate)
    }

    fun popTopFragment(animate: Boolean = true) : Boolean {
        val tag = peek ?: return false
        return popFragment(tag = tag, animate = animate)
    }

    override fun popFragment(clazz: KClass<out Fragment>?, tag: String?, animate: Boolean): Boolean {
        var curFragment: Fragment? = null
        if (clazz != null) {
            val name = clazz.java.name
            curFragment = fragmentManager.fragments.lastOrNull() { it.javaClass.name == name }
        }
        if (curFragment == null) {
            curFragment = fragmentManager.findFragmentByTag(tag ?: peek) ?: return false
        }
        return popFragment(curFragment, animate)
    }

    override fun popFragment(fragment: Fragment, animate: Boolean): Boolean {
        if (!listTagFragments.remove(fragment.arguments?.getString(NavigationController.FRAGMENT_NAME_TAG))) return false
        val transaction = fragmentManager
                .beginTransaction()
                .disallowAddToBackStack()
        try {
            if (animate || fragment.arguments?.getBoolean(NavigationController.PUSH_ANIMATE_FRAGMENT_TAG) == true) {
                transaction.setCustomAnimations(R.anim.slide_in_right_anim, R.anim.slide_out_right_anim)
            }
            transaction.remove(fragment).commitNowAllowingStateLoss()
            navigateCallback?.didRemoveFragment(fragment)
        } catch (e: Exception) {
            Handler().post {
                try {
                    transaction.remove(fragment).commitNowAllowingStateLoss()
                    navigateCallback?.didRemoveFragment(fragment)
                } catch (e: Exception) {
                }
            }
        }
        return true
    }

    override fun popFragment2(fragment: Fragment?, clazz: KClass<out Fragment>?, tag: String?, animateRightOrLeft: Boolean): Boolean {
        var newFg = fragment
        if (newFg == null && clazz != null) {
            val n = clazz.java.name
            newFg = fragmentManager.fragments.lastOrNull() { it.javaClass.name == n }
        }
        if (newFg == null) {
            newFg = fragmentManager.findFragmentByTag(tag ?: peek) ?: return false
        }
        if (!listTagFragments.remove(newFg.arguments?.getString(NavigationController.FRAGMENT_NAME_TAG))) return false
        val trans = fragmentManager
                .beginTransaction()
                .disallowAddToBackStack()
        try {
            if (animateRightOrLeft) {
                trans.setCustomAnimations(R.anim.slide_in_right_anim, R.anim.slide_out_right_anim)
            } else {
                trans.setCustomAnimations(R.anim.slide_in_left_anim, R.anim.slide_out_left_anim)
            }
            trans.remove(newFg).commitNowAllowingStateLoss()
            navigateCallback?.didRemoveFragment(newFg)
        } catch (e: Exception) {
            Handler().post {
                try {
                    trans.remove(newFg).commitNowAllowingStateLoss()
                    navigateCallback?.didRemoveFragment(newFg)
                } catch (e: Exception) {
                }
            }
        }
        return true
    }


    override fun pushFragment(fragment: Fragment, bundle: Bundle?, tag: String?,
                              animate: Boolean, viewId: Int, singleton: Boolean, parentTag: String?) {
        val mBundle = fragment.arguments ?: (bundle ?: Bundle())
        mBundle.putBoolean(NavigationController.PUSH_ANIMATE_FRAGMENT_TAG, animate)
        val mTag = tag ?: fragment.javaClass.name  + getLastTag(singleton)
        mBundle.putString(NavigationController.FRAGMENT_NAME_TAG, mTag)
        mBundle.putString(NavigationController.PARENT_FRAGMENT_NAME_TAG, parentTag)
        fragment.arguments = mBundle
        val mViewId = if (viewId == 0) android.R.id.content else viewId
        //        supportFragmentManager.findFragmentByTag(listTagFragments.peek())?.transitionOutLeft()
        if (singleton) {
            synchronized(mTag) {
                if (listTagFragments.contains(mTag) || fragment.isAdded) return
                push(fragment, animate, mViewId, mTag)
            }
        } else {
            push(fragment, animate, mViewId, mTag)
        }
    }

    override fun pushFragment(clazz: KClass<out Fragment>, bundle: Bundle?,
                              tag: String?, animate: Boolean, viewId: Int, singleton: Boolean) {
        pushFragment(fragment = clazz.java.newInstance(), bundle = bundle, tag = tag, animate = animate, viewId = viewId, singleton = singleton)
    }

    private fun push(fragment: Fragment, animate: Boolean, viewId: Int, tag: String) : Boolean {
        try {
            val transaction = fragmentManager.beginTransaction()
            if (animate) {
                transaction.setCustomAnimations(R.anim.slide_in_right_anim, R.anim.slide_out_right_anim)
            }
            navigateCallback?.prepareToPushFragment(fragment)
            transaction.add(viewId, fragment, tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitNowAllowingStateLoss()
            listTagFragments.push(tag)
            navigateCallback?.didPushFragment(fragment)
            return true
        } catch (e: Exception) {
            Logger.e(e)
        }
        return false
    }

    override fun addFragment(viewId: Int, clazz: KClass<out Fragment>,
                             bundle: Bundle?, tag: String?, singleton: Boolean) {
        addFragment(viewId, clazz.java.newInstance(), bundle, tag, singleton)
    }

    override fun addFragment(viewId: Int, fragment : Fragment,
                             bundle: Bundle?, tag: String?, singleton: Boolean) {
        val mTag = tag ?: fragment::class.java.name + getLastTag(singleton)
        val mBundle = bundle ?: Bundle()
        mBundle.putString(NavigationController.FRAGMENT_NAME_TAG, mTag)
        fragment.arguments = mBundle
        try {
            if (singleton) {
                synchronized(mTag) {
                    if (findFragment(fragment::class, mTag) != null) return
                    fragmentManager
                            .beginTransaction()
                            .disallowAddToBackStack()
                            .add(viewId, fragment, mTag)
                            .commitNowAllowingStateLoss()
                }
            } else {
                fragmentManager
                        .beginTransaction()
                        .disallowAddToBackStack()
                        .add(viewId, fragment, mTag)
                        .commitNowAllowingStateLoss()
            }
        } catch (e : Exception) {
            Logger.e(e)
        }
    }

    private fun getLastTag(singleton: Boolean) : String {
        return if (singleton) "" else "_${System.nanoTime()}"
    }

    override fun removeFragment(fragment: Fragment) {
        try {
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitNowAllowingStateLoss()
        } catch (e : Exception) {

        }
    }

    override fun removeFragment(tag: String) {
        val fragment = fragmentManager.findFragmentByTag(tag) ?: return
        removeFragment(fragment)
    }

    override fun removeFragment(clazz: KClass<out Fragment>) {
        val name = clazz.java.name
        val fragment = fragmentManager.fragments.firstOrNull() { it.javaClass.name == name } ?: return
        removeFragment(fragment)
    }

    override fun replaceFragment(viewId: Int, fragment: Fragment, tag: String?) {
        try {
            fragmentManager
                    .beginTransaction()
                    .replace(viewId, fragment, tag)
                    .disallowAddToBackStack()
                    .commitNowAllowingStateLoss()
        } catch (e : Exception) {
            Logger.e(e)
        }
    }

    override fun replaceFragment(viewId: Int, clazz: KClass<out Fragment>, tag: String?) {
        try {
            fragmentManager
                    .beginTransaction()
                    .replace(viewId, clazz.java.newInstance(), tag)
                    .disallowAddToBackStack()
                    .commitNowAllowingStateLoss()
        } catch (e : Exception) {
            Logger.e(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T: Fragment>findFragment(clazz: KClass<T>, tag: String?) : T? {
        val name = clazz.java.name
        val mTag = tag ?: listTagFragments.firstOrNull { it.contains(name) } ?: name
        return try {
            fragmentManager.findFragmentByTag(mTag) as? T?
        } catch (e: Exception) {
            null
        }
    }

    override val topFragment: Fragment?
        get() {
            return fragmentManager.findFragmentByTag(peek)
        }

    override val isEmptyFragment: Boolean get() = listTagFragments.isEmpty()
    override val sizeListFragment get() = listTagFragments.size
    override val listFragment: List<Fragment>
        get() = listTagFragments.mapNotNull { fragmentManager.findFragmentByTag(it) }
//    //endregion

}