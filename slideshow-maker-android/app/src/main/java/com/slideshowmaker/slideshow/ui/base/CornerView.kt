package com.slideshowmaker.slideshow.ui.base

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils

class CornerView :View{
    private var shadowRad = 0f
    private var applyShadowOnBottomOnly = false
    private var cornerRad = 0f
    private var backgroundColor = Color.WHITE
    private val backgroundPaint = Paint().apply {

    }

    constructor(context: Context?) : super(context) {
        initAttrs(null)
        init()

    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
        init()
    }

    private fun init() {
        shadowRad = DimenUtils.density(context!!)*3
        backgroundPaint.apply {
            setShadowLayer(shadowRad, 1f,1f, Color.parseColor("#e5e5e5"))
            isAntiAlias = true
            style = Paint.Style.FILL
            color = backgroundColor
        }

    }

    private fun initAttrs(attrs:AttributeSet?) {
        if(attrs == null) return
        val typedAttrArray = context.obtainStyledAttributes(attrs, R.styleable.CornerView)
        cornerRad = typedAttrArray.getDimension(R.styleable.CornerView_cornerRadiusView, 0f)
        backgroundColor = typedAttrArray.getColor(R.styleable.CornerView_bgColor, Color.GRAY)
        applyShadowOnBottomOnly = typedAttrArray.getBoolean(R.styleable.CornerView_shadowBottomOnly, false)
        typedAttrArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(getClipPath(), backgroundPaint)
    }


    fun getClipPath(): Path {
        val clipPath = Path()
        clipPath.reset()
        if(applyShadowOnBottomOnly) {
            clipPath.addRoundRect(RectF(0f,0f,width.toFloat(), height.toFloat()-shadowRad), cornerRad, cornerRad, Path.Direction.CW)
        } else {
            clipPath.addRoundRect(RectF(0f+shadowRad,0f+shadowRad,width.toFloat()-shadowRad, height.toFloat()-shadowRad), cornerRad, cornerRad, Path.Direction.CW)
        }

        clipPath.close()
        return clipPath
    }


}