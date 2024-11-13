package com.parallax.hdvideo.wallpapers.ui.custom

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.ScrollView
import com.parallax.hdvideo.wallpapers.R

class CustomScrollView : ScrollView {
    private var maxHeightValue = 0
    private val defaultHeightValue = 200

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val styledSetAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
            //200 is a defualt value
            maxHeightValue = styledSetAttrs.getDimensionPixelSize(
                R.styleable.MaxHeightScrollView_maxHeight,
                defaultHeightValue
            )
            styledSetAttrs.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightValue, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}