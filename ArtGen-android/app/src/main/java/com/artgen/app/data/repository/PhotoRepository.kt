package com.artgen.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.graphics.scale
import com.artgen.app.data.remote.repository.getUriFromFile
import com.artgen.app.ui.screen.imagepicker.ArtGenFileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoRepository(val context: Context) {

    suspend fun createTmpPhoto(image: ImageBitmap, maxSize: Int): Uri? =
        withContext(Dispatchers.IO) {
            val bitmap = image.asAndroidBitmap()
            val file = File(ArtGenFileProvider.getImagesFolder(context), "cropped_photo.jpg")

            return@withContext try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                val outPutFile = File.createTempFile(
                    "input_photo_anime_",
                    ".jpg",
                    File(ArtGenFileProvider.getImagesFolder(context))
                )
                rotateAndOptimizeBitmapForAnime(context.getUriFromFile(file), maxSize, outPutFile)
                context.getUriFromFile(outPutFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun rotateAndOptimizeBitmapForAnime(
        uri: Uri,
        totalMaxSize: Int,
        outputFile: File
    ): Boolean {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val angle = getCorrectBitmapRotation(uri)
        val options = BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, it)
        }
        var width = options.outWidth
        var height = options.outHeight
        val bitmapRatio = width.toFloat() / height.toFloat()
        var outputBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

        // Limit bitmap size width + height <= totalMaxSize
        if (width + height > totalMaxSize) {
            height = (totalMaxSize.toFloat() / (bitmapRatio + 1f)).toInt()
            width = (bitmapRatio * height).toInt()
            outputBitmap = outputBitmap.scale(width, height)
        }

        // Center crop bitmap
        val multipleOfSize = 64
        val shouldCropBitmap = width % multipleOfSize != 0 || height % multipleOfSize != 0
        if (shouldCropBitmap) {
            val newWidth = findNearestMultipleDown(width, multipleOfSize)
            val pixelCutWidth = width - newWidth
            val left = pixelCutWidth / 2
            width = newWidth

            val newHeight = findNearestMultipleDown(height, multipleOfSize)
            val pixelCutHeight = height - newHeight
            val top = pixelCutHeight / 2
            height = newHeight

            outputBitmap = Bitmap.createBitmap(outputBitmap, left, top, width, height)
        }

        // Rotate bitmap
        if (angle != 0) {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            outputBitmap = Bitmap.createBitmap(
                outputBitmap,
                0,
                0,
                outputBitmap.width,
                outputBitmap.height,
                matrix,
                true,
            )
        }

        // Save bitmap to file
        val result = outputBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            80,
            FileOutputStream(outputFile),
        )
        outputBitmap.recycle()
        return result
    }

    private fun findNearestMultipleDown(value: Int, number: Int): Int {
        return (value / number) * number
    }

    private fun getCorrectBitmapRotation(uri: Uri): Int {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
        val ei = ExifInterface(inputStream)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED,
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
