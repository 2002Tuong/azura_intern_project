package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.dp2Px
import com.parallax.hdvideo.wallpapers.utils.dpToPx

class IndicatorView: View {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var amount = 5
    var curPosition = 0
    var progress: Float = 0f

    private var radius: Float = dp2Px(10f)
    private var space: Float = dp2Px(5f)
    private var strokeWidthValue: Float = dp2Px(1f)

    private var strokeColorValue = Color.BLACK
    private var selectedColorValue =  Color.BLUE

    private var isStyleStroke = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup(context, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        if (attrs != null)  {
            val typedArray = context.obtainStyledAttributes(attrs,  R.styleable.IndicatorView)
            radius = typedArray.getDimensionPixelOffset(R.styleable.IndicatorView_radius, dpToPx(5f)).toFloat()
            space = typedArray.getDimensionPixelOffset(R.styleable.IndicatorView_spacing, dpToPx(3f)).toFloat()
            strokeWidthValue = typedArray.getDimensionPixelOffset(R.styleable.IndicatorView_strokeWidth, dpToPx(1f)).toFloat()
            strokeColorValue = typedArray.getColor(R.styleable.IndicatorView_color, Color.BLACK)
            selectedColorValue = typedArray.getColor(R.styleable.IndicatorView_selectedColor, Color.BLUE)
            isStyleStroke = typedArray.getBoolean(R.styleable.IndicatorView_styleStroke, true)
            typedArray.recycle()
        }
        strokePaint.color = strokeColorValue
        strokePaint.strokeWidth = strokeWidthValue
        strokePaint.style = if (isStyleStroke) Paint.Style.STROKE else Paint.Style.FILL
        strokePaint.isDither = true
        circlePaint.color = selectedColorValue
        circlePaint.style = Paint.Style.FILL
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val widthCircles = radius * 2 * amount + space * (amount - 1)
        var cx = (width - widthCircles) / 2 - radius
        val cy = height / 2f
        for (i in 0 until amount) {
            canvas.drawCircle(cx + i * (radius * 3 + space), cy, radius, strokePaint)
        }
        cx += curPosition * (radius * 3 + space)
        cx += (radius * 3 + space) * progress
        canvas.drawCircle(cx, cy, radius, circlePaint)
    }

    fun update(count: Int, pos: Int, progress: Float) {
        this.amount = count
        curPosition = pos
        this.progress = progress
        invalidate()
    }
}