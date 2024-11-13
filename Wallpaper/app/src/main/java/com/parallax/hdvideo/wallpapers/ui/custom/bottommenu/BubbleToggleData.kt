package com.parallax.hdvideo.wallpapers.ui.custom.bottommenu

import android.graphics.Color
import android.graphics.drawable.Drawable

internal class BubbleToggleData {
    var iconDraw: Drawable? = null
    var shapeDraw: Drawable? = null
    var title = ""
    var activeColor = Color.BLUE
    var inactiveColor = Color.BLACK
    var shapeColor = Int.MIN_VALUE
    var badgeText: String? = null
    var colorOfBadgeText = Color.WHITE
    var colorOfBadgeBackground = Color.BLACK
    var sizeOfTitle = 0f
    var textSizeOfBadge = 0f
    var iconWidthValue = 0f
    var iconHeightValue = 0f
    var titlePadding = 0
    var internalPadding = 0
}