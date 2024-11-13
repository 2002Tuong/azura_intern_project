package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.ImageView
import com.slideshowmaker.slideshow.utils.DimenUtils

class CornerImageView(context: Context, attrs: AttributeSet?) :
    ImageView(context, attrs) {
    private var cornerValue = 6f
    private fun getClipPath(): Path {
        val cornerRad = cornerValue*DimenUtils.density(context)
        val path = Path()
        path.reset()
        path.addRoundRect(RectF(0f,0f,width.toFloat(), height.toFloat()), cornerRad, cornerRad, Path.Direction.CW)
        path.close()
        return path
    }

    fun changeCorner(corner:Float) {
        cornerValue = corner
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(getClipPath())
        super.onDraw(canvas)
    }


}