package com.bloodpressure.app.camera

import android.content.Context
import android.graphics.Bitmap
import android.media.Image

interface ProcessingSupport {
    fun yuvToNv(image: Image): ByteArray

    fun yuvSpToRedAvg(yuv420sp: ByteArray?, width: Int, height: Int): List<Int>
    fun imageToBitmap(byteArray: ByteArray, image: Image): Bitmap?
}