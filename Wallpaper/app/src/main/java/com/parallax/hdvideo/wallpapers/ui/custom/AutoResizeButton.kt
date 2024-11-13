package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatButton
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.other.TextViewHelper
import com.parallax.hdvideo.wallpapers.utils.spToPx

class AutoResizeButton: AppCompatButton {
    private var needResize = false
    private var minimumTextSize = spToPx(8f)
    private var maximumTextSize = 0F
    private var curTextSize = 0F

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        maximumTextSize = textSize
        curTextSize = textSize
        context.apply {
            val array = obtainStyledAttributes(R.styleable.AutoResizeButton)
            maximumTextSize = array.getDimensionPixelOffset(R.styleable.AutoResizeButton_maxTextSize, textSize.toInt()).toFloat()
            minimumTextSize = array.getDimensionPixelOffset(R.styleable.AutoResizeButton_minTextSize, minimumTextSize.toInt()).toFloat()
            array.recycle()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            resizeText()
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        needResize = true
        resizeText()
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            needResize = true
        }
    }

    private fun resizeText() {
        if (needResize) {
            val heightLimited = height - paddingBottom - paddingTop
            val widthLimited = width - paddingLeft - paddingRight
            if (heightLimited <= 0 || widthLimited <= 0) return
            needResize = false
            val size =
                TextViewHelper.getTextSize(this, heightLimited, widthLimited, maximumTextSize, minimumTextSize)
            if (curTextSize != size) {
                curTextSize = size
                setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            }
        }
    }
}