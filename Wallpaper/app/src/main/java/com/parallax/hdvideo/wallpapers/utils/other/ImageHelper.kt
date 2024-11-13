package com.parallax.hdvideo.wallpapers.utils.other

import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.io.*


object ImageHelper {

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (heightValue: Int, widthValue: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (heightValue > reqHeight || widthValue > reqWidth) {

            val halfOfHeight: Int = heightValue / 2
            val halfOfWidth: Int = widthValue / 2
            while (halfOfHeight / inSampleSize >= reqHeight && halfOfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun decodeBitmap(filePath: String, reqWidth: Int = AppConfiguration.widthScreenValue, reqHeight: Int = AppConfiguration.heightScreenValue): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeFile(filePath, this)
        }
    }

    fun decodeBitmap(uri: Uri, reqWidth: Int = AppConfiguration.widthScreenValue, reqHeight: Int = AppConfiguration.heightScreenValue): Bitmap? {
        try {
            val options = BitmapFactory.Options()
//            App.instance.contentResolver.openInputStream(uri)?.use {
//                option.inJustDecodeBounds = true
//                BitmapFactory.decodeStream(it, null, option)
//            }
            WallpaperApp.instance.contentResolver.openFileDescriptor(uri, "r")?.use {
                BitmapFactory.decodeFileDescriptor(it.fileDescriptor, Rect(), options)
            }
            if (options.outWidth == -1 || options.outHeight == -1) return null
            return WallpaperApp.instance.contentResolver.openInputStream(uri)?.use {
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            Logger.d("decodeBitmap", e)
        }
        return null
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, outputStream)
        val byteArray: ByteArray = outputStream.toByteArray()
        val fileOutStream = FileOutputStream(file)
        return try {
            fileOutStream.write(byteArray)
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        } catch (e2: IOException) {
            e2.printStackTrace()
            false
        }finally {
            fileOutStream.flush()
            fileOutStream.close()
        }
    }

    fun Bitmap.crop(cropType: CropType = CropType.CENTER, w: Int = AppConfiguration.widthScreenValue, h: Int = AppConfiguration.heightScreenValue): Bitmap? {
        val widthValue = if (w <= 0) this.width else w
        val heightValue = if (h <= 0) this.height else h
        val configValue = if (config != null) config else Bitmap.Config.ARGB_8888
        val bitmapOutput = Bitmap.createBitmap(widthValue, heightValue, configValue)
        bitmapOutput.setHasAlpha(true)
        val scaleXValue = widthValue.toFloat() / this.width
        val scaleYValue = heightValue.toFloat() / this.height
        val scaleValue = kotlin.math.max(scaleXValue, scaleYValue)
        val scaledWidthValue = scaleValue * this.width
        val scaledHeightValue = scaleValue * this.height
        val leftOffset: Float = (widthValue - scaledWidthValue) / 2
        val topOffset: Float = getTop(cropType, scaledHeightValue, heightValue)
        val targetRectF = RectF(leftOffset, topOffset, leftOffset + scaledWidthValue, topOffset + scaledHeightValue)
        val canvas = Canvas(bitmapOutput)
        canvas.drawBitmap(this, null, targetRectF, null)
        return bitmapOutput
    }

    private fun getTop(cropType: CropType, scaledHeight: Float, h: Int): Float {
        return when (cropType) {
            CropType.TOP -> 0f
            CropType.CENTER -> (h - scaledHeight) / 2f
            CropType.BOTTOM -> h - scaledHeight
        }
    }


    enum class CropType {
        TOP, CENTER, BOTTOM
    }
}