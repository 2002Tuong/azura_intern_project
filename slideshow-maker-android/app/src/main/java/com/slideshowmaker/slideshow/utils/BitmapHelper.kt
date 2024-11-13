package com.slideshowmaker.slideshow.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.slideshowmaker.slideshow.VideoMakerApplication
import java.io.File
import java.io.FileInputStream
import kotlin.math.max
import kotlin.math.min

object BitmapHelper {
    fun getBitmapFromFilePath(filePath: String): Bitmap {
        try {
            val imgFile = File(filePath)
            val inBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            return modifyOrientation(inBitmap, filePath)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("getBitmapFromFilePath -- $e")
            return getBlackBitmap()
        }

    }

    fun resizeBitmap(path: String?, maxSize: Int): Bitmap? {
        val bitmap: Bitmap?
        val imgFile = File(path)
        val bitmapOpts = BitmapFactory.Options()
        bitmapOpts.inJustDecodeBounds = true
        val inputStream = FileInputStream(imgFile)
        BitmapFactory.decodeStream(inputStream, null, bitmapOpts)
        inputStream.close()
        var scale = 1f
        if (bitmapOpts.outWidth > maxSize || bitmapOpts.outHeight > maxSize) {
            val ratioWidth = bitmapOpts.outWidth.toFloat() / maxSize
            val ratioHeight = bitmapOpts.outHeight.toFloat() / maxSize
            val ratioMax = max(ratioWidth, ratioHeight)
            scale = ratioMax
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = scale.toInt()
        val fs = FileInputStream(imgFile)
        bitmap = BitmapFactory.decodeStream(fs, null, options)
        fs.close()
        return bitmap
    }

    fun getStickerFromFilePath(filePath: String): Bitmap {
        val stickerFile = File(filePath)
        return BitmapFactory.decodeFile(stickerFile.absolutePath)

    }

    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val exifInterface = ExifInterface(image_absolute_path)
        val orientation =
            exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotate(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotate(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotate(bitmap, 270f)
            }
            //ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            //ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> {
                bitmap
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resizeWrapBitmap(bitmapInput: Bitmap, size: Float): Bitmap {
        if (bitmapInput.width < size && bitmapInput.height < size) return bitmapInput
        val ratio = min((size / bitmapInput.width), (size / bitmapInput.height))
        return Bitmap.createScaledBitmap(
            bitmapInput,
            (bitmapInput.width * ratio).toInt(),
            (bitmapInput.height * ratio).toInt(),
            true
        )
    }

    fun resizeWrapBitmap(bitmapInput: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val widthValue = bitmapInput.width
        val heightValue = bitmapInput.height
        val scale = min(maxWidth.toFloat() / bitmapInput.width, maxHeight.toFloat() / bitmapInput.height)

        return Bitmap.createScaledBitmap(bitmapInput, (widthValue*scale).toInt(),
            (heightValue*scale).toInt(), false)
    }


    fun resizeMatchBitmap(bitmapInput: Bitmap, size: Float): Bitmap {
        // if(bitmapInput.width < size && bitmapInput.height < size) return bitmapInput
        val ratio = max((size / bitmapInput.width), (size / bitmapInput.height))
        return Bitmap.createScaledBitmap(
            bitmapInput,
            (bitmapInput.width * ratio).toInt(),
            (bitmapInput.height * ratio).toInt(),
            true
        )
    }

    fun blurBitmapV2(bm: Bitmap?, r: Int): Bitmap? {
        if (bm == null) return null
        val radius = 25f
        Logger.e("size = ${bm.width} x ${bm.height}")
        val rsScript: RenderScript =
            RenderScript.create(VideoMakerApplication.getContext())
        val alloc: Allocation = Allocation.createFromBitmap(rsScript, bm)
        val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript))
        blur.setRadius(radius)
        blur.setInput(alloc)
        val result =
            Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.ARGB_8888)
        val outAlloc = Allocation.createTyped(rsScript, alloc.type)
        blur.forEach(outAlloc)
        outAlloc.copyTo(result)

        rsScript.destroy()
        return result
    }

    fun getBlackBitmap(): Bitmap {
        val blackBitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        for (i in 0 until blackBitmap.width) {
            for (j in 0 until blackBitmap.height) {
                blackBitmap.setPixel(i, j, Color.BLACK)
            }
        }
        return blackBitmap
    }

    fun loadBitmapFromUri(path: String?, callBack: (Bitmap?) -> Unit) {
        Glide.with(VideoMakerApplication.getContext()).asBitmap().load(path).apply(
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        ).addListener(object :
            RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                callBack.invoke(null)
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                callBack.invoke(resource)
                return true
            }
        }).submit()
    }

    fun loadBitmapFromXML(id: String, callBack: (Bitmap?) -> Unit) {
        val bitmapFromXML = VideoMakerApplication.getContext().getDrawable(id.toInt())?.toBitmap(512, 512)
        callBack.invoke(bitmapFromXML)
    }

    fun getBitmapFromAsset(path: String): Bitmap {
        val inputStream = VideoMakerApplication.getContext().assets.open(path)
        return BitmapFactory.decodeStream(inputStream)
    }

}