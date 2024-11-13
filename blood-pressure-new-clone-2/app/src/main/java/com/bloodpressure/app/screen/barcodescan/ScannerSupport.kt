package com.bloodpressure.app.screen.barcodescan

import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.Image
import android.util.Log
import com.bloodpressure.app.camera.CameraSupport
import com.bloodpressure.app.camera.PreviewListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScannerSupport(
    private val cameraSupport: CameraSupport
) : PreviewListener{
    private val _detectResult = MutableStateFlow<Result?>(null)
    val detectResult = _detectResult.asStateFlow()
    override fun onPreviewData(data: ByteArray?, width: Int?, height: Int?) {
        //Log.d("ScanCode", "ImageSize: ${width} | ${height}")

        data?.let {
            val source = PlanarYUVLuminanceSource(
                it,
                width?: 480,
                height?: 640,
                0, 0,
                width?:480,
                height?:640,
                false
            )

            val binaryBmp = BinaryBitmap(HybridBinarizer(source))
            try {
                val res = MultiFormatReader().apply {
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
                        )
                    )
                }.decode(binaryBmp)
                _detectResult.update { res }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onPreviewCount(count: List<Int>) {

    }

    fun setSizeSurfaceTexture(width: Int, height: Int) {
        cameraSupport.setSurfaceTextureSize(width, height)
    }

    fun addPreviewSurface(surfaceTexture: SurfaceTexture) {
        cameraSupport.addPreviewSurface(surfaceTexture)
    }

    fun startScanCode() {
        if (!cameraSupport.isCameraInUse) {
            Log.d("ScanCode", "open camera")
            cameraSupport.open().addOnPreviewListener(this)
        }
    }

    fun stopScanCode() {
        if(cameraSupport.isCameraInUse) {
            cameraSupport.close()
            //_detectResult.update { null}
        }
    }

    fun resetResult() {
        _detectResult.update { null }
    }
}