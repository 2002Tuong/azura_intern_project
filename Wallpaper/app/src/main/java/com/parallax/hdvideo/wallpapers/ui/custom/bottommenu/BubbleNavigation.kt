package com.parallax.hdvideo.wallpapers.ui.custom.bottommenu

import android.graphics.Typeface

interface BubbleNavigation {
    fun setNavigationChangeListener(navigationChangeListener: BubbleNavigationChangeListener?)
    fun setTypeface(typeface: Typeface?)
    val currentActiveItemPosition: Int
    fun setCurrentActiveItem(position: Int)
    fun setBadgeValue(position: Int, value: String?)
}