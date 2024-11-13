package com.parallax.hdvideo.wallpapers.ui.custom.bottommenu

import android.view.View

interface BubbleNavigationChangeListener {
    fun onNavigationChanged(view: View?, position: Int)
    fun onChooseAgainTab(position: Int)
}