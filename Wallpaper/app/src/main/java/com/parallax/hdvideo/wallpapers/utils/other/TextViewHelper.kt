package com.parallax.hdvideo.wallpapers.utils.other

import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.TextView
import com.parallax.hdvideo.wallpapers.utils.spToPx

object TextViewHelper {

    fun getTextSize(textView: TextView, width: Int, height: Int, maxTextSize: Float, minTextSize: Float) : Float {
        if (textView.text.isNullOrEmpty()) return textView.textSize
//        val lineHeight = getTextSize("g", maxTextSize).height * 2
//        val maxHeight = if(textView.maxLines == Int.MAX_VALUE || textView.maxLines == -1) height
//        else lineHeight * textView.maxLines
        var staticLayout = getLayout(textView, width, maxTextSize)
        var textSize = maxTextSize
        val pxUnit = spToPx(1f)
        while (staticLayout.height > height) {
            textSize -= pxUnit
            if (textSize <= minTextSize) {
                return minTextSize
            }
            staticLayout = getLayout(textView, width, textSize)
        }
        return textSize
    }

    private fun getLayout(textView: TextView, width: Int,
        textSize: Float): StaticLayout {
        val spacingMulti = textView.lineSpacingMultiplier
        val spacingExtra = textView.lineSpacingExtra
        val sourceText = textView.text ?: " "
        val textPaint = TextPaint(textView.paint)
        textPaint.textSize = textSize
        textPaint.typeface = textView.typeface
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(sourceText, 0,  sourceText.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(spacingExtra, spacingMulti)
                .setIncludePad(true).build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(sourceText, textPaint, width,
                Layout.Alignment.ALIGN_NORMAL, spacingMulti, spacingExtra, true)
        }
        val textResizeCanvas = Canvas()
        staticLayout.draw(textResizeCanvas)
        return staticLayout
    }


    fun getTextSize(source: CharSequence, textSize: Float, typeface: Typeface = Typeface.DEFAULT): CGSize {
        val textPaint = TextPaint()
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true
        textPaint.typeface = typeface
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(source, 0,  source.length, textPaint, Int.MAX_VALUE)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true).build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(source, textPaint, Int.MAX_VALUE,
                Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        }
        return CGSize(staticLayout.width, staticLayout.height)
    }

    data class CGSize(var width: Int, var height: Int)
}