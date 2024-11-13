package com.parallax.hdvideo.wallpapers.ui.custom

import android.view.View
import androidx.viewpager.widget.ViewPager

class ScaleTransformer constructor(val maxElevation: Float, val minElevation: Float, val minScale: Float = 0.85f) : ViewPager.PageTransformer {

    private val maxScale = 0.97f

    override fun transformPage(view: View, position: Float) {
//        val card = view as? AnimCardView ?: return
//        var scale = 1 - abs(position) * (1 - minScale)
//        scale = max(min(scale, maxScale), minScale)
//        card.scaleY = scale
//        card.cardElevation =
//            minElevation + (maxElevation - minScale) * (1 - (maxScale - scale) / (maxScale - minScale))
//        card.maxScaleY = scale
    }
}