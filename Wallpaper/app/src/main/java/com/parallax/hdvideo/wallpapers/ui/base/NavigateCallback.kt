package com.parallax.hdvideo.wallpapers.ui.base

import androidx.fragment.app.Fragment

interface NavigateCallback {

    fun prepareToPushFragment(fragment: Fragment)
    fun didPushFragment(fragment: Fragment)
    fun didRemoveFragment(fragment: Fragment)
}