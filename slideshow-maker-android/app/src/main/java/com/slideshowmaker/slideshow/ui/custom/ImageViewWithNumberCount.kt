package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils

class ImageViewWithNumberCount : androidx.appcompat.widget.AppCompatImageView {

    private var count = 0
    private val mThemeColor = Color.parseColor("#1D79FF")

    private var widthOfBorder = 6f
    private val borderRectObj = Rect()
    private val borderPaintObj = Paint()

    private var countRectSize = 27
    private val countRectObj = Rect()
    private val countRectPaintObj = Paint()

    private val textSize = 16
    private val textRectObj = Rect()
    private val countTextPaintObj = Paint()

    private var shouldActiveCounter = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        init()
    }

    private fun init() {
        countRectSize = (countRectSize * DimenUtils.density(context)).toInt()
        widthOfBorder *= DimenUtils.density(context)
        borderPaintObj.apply {
            style = Paint.Style.STROKE
            strokeWidth = widthOfBorder
            color = mThemeColor
            isAntiAlias = true
        }
        countRectPaintObj.apply {
            style = Paint.Style.FILL
            color = mThemeColor
            isAntiAlias = true
        }
        countTextPaintObj.apply {
            textSize = textSize * DimenUtils.density(context)
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (count > 0 && shouldActiveCounter) {
            borderRectObj.set(0, 0, width, height)
            countRectObj.set(width - countRectSize, height - countRectSize, width, height)
            countTextPaintObj.getTextBounds(count.toString(), 0, count.toString().length, textRectObj)
            canvas.drawRect(borderRectObj, borderPaintObj)
            canvas.drawRect(countRectObj, countRectPaintObj)
            canvas.drawText(
                count.toString(),
                width.toFloat() - countRectSize / 2,
                height.toFloat() - countRectSize / 2 + textRectObj.height() / 2,
                countTextPaintObj
            )
        }

    }

    fun increaseCount() {
        count++
        invalidate()
    }

    fun deincreaseCount() {
        count--
        if (count < 0) count = 0
        invalidate()
    }

    fun setCount(count: Int) {
        this.count = count
        /*mCount = if (count <= 0) {
            0
        } else {
            count
        }*/
        invalidate()

    }

    fun getCount(): Int = count

    fun disableCounter() {
        shouldActiveCounter = false
        invalidate()
    }

    fun activeCounter() {
        shouldActiveCounter = true
        invalidate()
    }

}