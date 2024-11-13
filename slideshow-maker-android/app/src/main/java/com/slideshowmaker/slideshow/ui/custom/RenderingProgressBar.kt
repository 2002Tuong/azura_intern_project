package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlin.math.roundToInt

class RenderingProgressBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val density = DimenUtils.density(context)
    private var progressLineHeight = 4 * density
    private var textSize = 0 * density
    private var progress = 0f
    private val backgroundLinePaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#4E5563")
    }
    private val highlightLinePaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#FB7609")
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#627388")
        typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        textSize = textSize
    }

    override fun onDraw(canvas: Canvas) {
        drawBgLine(canvas)
        drawHighlightLine(canvas)
        drawPercentText(canvas)
    }

    private fun drawBgLine(canvas: Canvas?) {
        val drawPath = Path().apply {
            addRoundRect(
                RectF(0f, height - progressLineHeight, width.toFloat(), height.toFloat()),
                (progressLineHeight / 2),
                progressLineHeight / 2,
                Path.Direction.CW
            )
        }
        canvas?.drawPath(drawPath, backgroundLinePaint)
    }

    private fun drawHighlightLine(canvas: Canvas?) {
        val drawPath = Path().apply {
            val rightRect = width * progress / 100
            addRoundRect(
                RectF(0f, height - progressLineHeight, rightRect, height.toFloat()),
                progressLineHeight / 2,
                progressLineHeight / 2,
                Path.Direction.CW
            )
        }
        canvas?.drawPath(drawPath, highlightLinePaint)
    }

    private fun drawPercentText(canvas: Canvas?) {
        if(!progress.isNaN()) {
            val text = progress.roundToInt().toString() + "%"
            val textWidth = getTextWidth(text, textPaint)
            val textHeight = getTextHeight(text, textPaint)
            canvas?.drawText(text, width / 2f - textWidth / 2f, textHeight + 5, textPaint)
        }

    }

    private fun getTextWidth(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }

    private fun getTextHeight(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    fun setProgress(percent: Float) {
        progress = when {
            percent >= 100f -> 100f
            percent <= 0f -> 0f
            else -> percent
        }

        invalidate()
    }
    fun setProgress(percent: Int) {
        progress = when {
            percent >= 100 -> 100f
            percent <= 0 -> 0f
            else -> percent.toFloat()
        }

        invalidate()
    }
    fun addProgress(deltaProgress: Float) {


            progress+=deltaProgress
            if(progress >= 99f) progress = 99f
            invalidate()




    }

}