package com.parallax.hdvideo.wallpapers.utils.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.renderscript.*
import android.renderscript.RenderScript.RSMessageHandler
import androidx.annotation.ColorInt
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest


class BlurBitmapTransformation(private val context: Context) : BitmapTransformation() {

    private  val DEF_DOWN_SAMPLING = 0.9f

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap? {
        val bitmapToTransform: Bitmap = toTransform
        val scaledWidthValue = (bitmapToTransform.width * DEF_DOWN_SAMPLING).toInt()
        val scaledHeightValue = (bitmapToTransform.height * DEF_DOWN_SAMPLING).toInt()
        val bitmap = pool[scaledWidthValue, scaledHeightValue, Bitmap.Config.ARGB_8888]
        return BitmapResource.obtain(this.blurBitmap(context, bitmapToTransform, bitmap, Color.argb(90, 255, 255, 255)), pool)?.get()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("blur transformation".toByteArray())
    }

    @Synchronized
    fun blurBitmap(context: Context, source: Bitmap?, bitmap: Bitmap, @ColorInt colorOverlay: Int): Bitmap? {
        if (source == null) return bitmap
        Canvas(bitmap).apply {
            scale(DEF_DOWN_SAMPLING, DEF_DOWN_SAMPLING)
            drawBitmap(source, 0f, 0f, Paint().apply {
                flags = Paint.FILTER_BITMAP_FLAG
            })
//            drawColor(colorOverlay)
        }
        return try {
            blur(context, bitmap)
        } catch (e: RSRuntimeException) {
            e.printStackTrace()
            bitmap
        }
    }

    @Throws(RSRuntimeException::class)
    private fun blur(context: Context, bitmap: Bitmap): Bitmap {
        var renderScript: RenderScript? = null
        var inputAllocation: Allocation? = null
        var outputAllocation: Allocation? = null
        var blur: ScriptIntrinsicBlur? = null
        try {
            renderScript = RenderScript.create(context)
            renderScript.messageHandler = RSMessageHandler()
            inputAllocation = Allocation.createFromBitmap(renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
            outputAllocation = Allocation.createTyped(renderScript, inputAllocation.type)
            blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).apply {
                setInput(inputAllocation)
                setRadius(3f)
                forEach(outputAllocation)
            }
            outputAllocation.copyTo(bitmap)
        } finally {
            renderScript?.destroy()
            inputAllocation?.destroy()
            outputAllocation?.destroy()
            blur?.destroy()
        }
        return bitmap
    }
}