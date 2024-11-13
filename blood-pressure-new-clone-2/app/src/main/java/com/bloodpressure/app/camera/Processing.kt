package com.bloodpressure.app.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import timber.log.Timber
import java.io.ByteArrayOutputStream


class Processing : ProcessingSupport {
    override fun yuvToNv(image: Image): ByteArray {
        val crop = image.cropRect
        val format = image.format
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
        val rowData = ByteArray(planes[0].rowStride)

        var channelOffset = 0
        var outputStride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }

                1 -> {
                    channelOffset = width * height + 1
                    outputStride = 2
                }

                2 -> {
                    channelOffset = width * height
                    outputStride = 2
                }
            }
            val buffer = planes[i].buffer
            val rowStride = planes[i].rowStride
            val pixelStride = planes[i].pixelStride
            val shift = if (i == 0) 0 else 1
            val w = width shr shift
            val h = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until h) {
                var length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = w
                    buffer[data, channelOffset, length]
                    channelOffset += length
                } else {
                    length = (w - 1) * pixelStride + 1
                    buffer[rowData, 0, length]
                    for (col in 0 until w) {
                        data[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }
        return data


    }

    // Helper to verify result
    override fun imageToBitmap(bytes: ByteArray, image: Image): Bitmap? {
        val yuvImage = YuvImage(bytes, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    override fun yuvSpToRedAvg(yuv420sp: ByteArray?, width: Int, height: Int): List<Int> {
        if (yuv420sp == null) return listOf(0, 0, 0)
        val frameSize = width * height
        val result = yuvSpToRedSum(yuv420sp, width, height)
        val sum = result[0]
        val sumg = result[1]
        val sumb = result[2]
        Timber.d(
            "Calculated sumr: %d, sumg :%d, sumb: %d",
            sum / frameSize,
            sumg / frameSize,
            sumb / frameSize
        )

        return listOf(sum / frameSize, sumg / frameSize, sumb / frameSize)
    }

    private fun yuvSpToRedSum(yuv420sp: ByteArray?, width: Int, height: Int): List<Int> {
        if (yuv420sp == null) return listOf(0, 0, 0)
        val frameSize = width * height
        var sum = 0
        var sumg = 0
        var sumb = 0
        var j = 0
        var yp = 0
        while (j < height) {
            var uvp = frameSize + (j shr 1) * width
            var u = 0
            var v = 0
            var i = 0
            while (i < width) {
                var y = (0xff and yuv420sp[yp].toInt()) - 16
                if (y < 0) y = 0
                if (i and 1 == 0) {
                    v = (0xff and yuv420sp[uvp++].toInt()) - 128
                    u = (0xff and yuv420sp[uvp++].toInt()) - 128
                }
                val y1192 = 1192 * y
                var r = y1192 + 1634 * v
                var g = y1192 - 833 * v - 400 * u
                var b = y1192 + 2066 * u
                if (r < 0) r = 0 else if (r > 262143) r = 262143
                if (g < 0) g = 0 else if (g > 262143) g = 262143
                if (b < 0) b = 0 else if (b > 262143) b = 262143
                val pixel =
                    -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
                val red = pixel shr 16 and 0xff
                val green = pixel shr 8 and 0xff
                val blue = pixel and 0xff
                sum += red
                sumg += green
                sumb += blue
                i++
                yp++
            }
            j++
        }
        return listOf(sum, sumb, sumb)
    }

    companion object {
        private val TAG = Processing::class.java.simpleName
    }
}