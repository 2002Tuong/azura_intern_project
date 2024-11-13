package com.bloodpressure.app.camera

import android.media.Image

interface PreviewListener {
    fun onPreviewData(data: ByteArray?, width: Int? = 0, height: Int? = 0)
    fun onPreviewCount(count: List<Int>)

}