package com.bloodpressure.app.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class CameraNew(
    mContext: Context,
    processingSupport: ProcessingSupport?
) : CameraSupport {
    private val mContext: Context
    private var mCameraDevice: CameraDevice? = null
    private val mCameraManager: CameraManager
    private var mCaptureSession: CameraCaptureSession? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private val mCameraOpenCloseLock = Semaphore(1)
    private var mImageReader: ImageReader? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraId: String = ""
    private var mFlashSupported = false
    private var mWakeLock: WakeLock? = null
    private var mPreviewListener: PreviewListener? = null
    private val processingSupport: ProcessingSupport?
    private var mSurface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var mIsFlashOn = false
    private var width = 480
    private var height = 640
    private var previewSize: Size = Size(width,height)

    override fun open(): CameraSupport {
        try {
            startBackgroundThread()
            setCameraOutputs()
            if (!mCameraOpenCloseLock.tryAcquire(
                    2500,
                    TimeUnit.MILLISECONDS
                )
            ) {
                throw RuntimeException("Time out waiting to acquire lock while camera opening.")
            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                mWakeLock?.acquire(10 * 60 * 1000L)
                mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
            } else {
                throw RuntimeException("Permission failed while access camera.")
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to camera opening.", e)
        }
        return this
    }

    override fun close() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession?.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice?.close()
                mCameraDevice = null
            }
            if (null != mImageReader) {
                mImageReader?.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(
                "Interrupted while trying to release and close camera.",
                e
            )
        } finally {
            mWakeLock?.release()
            mCameraOpenCloseLock.release()
        }
        stopBackgroundThread()
    }

    override fun addOnPreviewListener(callBack: PreviewListener) {
        mPreviewListener = callBack
    }

    override fun addPreviewSurface(surface: SurfaceTexture): CameraSupport {
        surfaceTexture = surface
        mSurface = Surface(surface)
        return this
    }

    override fun toggleFlash() {
        mIsFlashOn = !mIsFlashOn
        if (isCameraInUse) {
            try {
                mCaptureSession?.close()
                createCameraPreviewSession()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun enableFlash(isEnabled: Boolean) {
        mIsFlashOn = isEnabled
    }

    override fun turnFlashOn(isON: Boolean) {
        mIsFlashOn = isON

        if (isCameraInUse) {
            try {
                if (mCaptureSession != null) {
                    mCaptureSession?.close()
                }
                createCameraPreviewSession() // Recreate the camera session with updated flash state
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    override val isCameraInUse: Boolean
        get() = mCameraDevice != null

    override fun setSurfaceTextureSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    private fun setCameraOutputs() {
        try {
            for (cameraId in mCameraManager.cameraIdList) {
                val mCharacteristics = mCameraManager
                    .getCameraCharacteristics(cameraId)
                val facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val streamConfigurationMap = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                val deviceRotation =  (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
                val totalRotation = sensorToDeviceRotation(mCharacteristics, deviceRotation)
                val swapRotation = totalRotation == 90 || totalRotation == 270
                var rotatedWidth = width
                var rotatedHeight = height
                if (swapRotation) {
                    rotatedHeight = width
                    rotatedWidth = height
                }

                previewSize = chooseOptimalSize(streamConfigurationMap!!.getOutputSizes(SurfaceTexture::class.java).toList(), rotatedWidth, rotatedHeight)
                surfaceTexture?.let {
                    it.setDefaultBufferSize(previewSize.width, previewSize.height)
                    mSurface = Surface(it)
                }


                mImageReader = ImageReader.newInstance(
                    200, 400,
                    ImageFormat.YUV_420_888, 5
                )
                mImageReader?.setOnImageAvailableListener(
                    mOnImageAvailableListener,
                    mBackgroundHandler
                )
                val available =
                    mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                mFlashSupported = available ?: false
                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val imageSurface = mImageReader?.surface
            val surfaceList: MutableList<Surface?> = ArrayList()

            mPreviewRequestBuilder =
                mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

            mSurface?.let {
                mPreviewRequestBuilder?.addTarget(mSurface!!)
                surfaceList.add(mSurface)
            }
            imageSurface?.let { mPreviewRequestBuilder?.addTarget(it) }
            surfaceList.add(imageSurface)

//            setAutoFlash(mPreviewRequestBuilder)

            mCameraDevice?.createCaptureSession(surfaceList, mSessionStateCallBack, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper ?: Looper.getMainLooper())
    }

    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder?) {
        if (mIsFlashOn && mFlashSupported) {
            requestBuilder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
        } else {
            requestBuilder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
    }

    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }
    }
    private val mSessionStateCallBack: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    if (null == mCameraDevice) {
                        return
                    }
                    mCaptureSession = session
                    setAutoFlash(mPreviewRequestBuilder)
                    val mPreviewRequest = mPreviewRequestBuilder?.build()
                    mPreviewRequest?.let {
                        mCaptureSession?.setRepeatingRequest(
                            it,
                            null,
                            mBackgroundHandler
                        )
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()

                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }
    private val mOnImageAvailableListener =
        OnImageAvailableListener { reader -> //pixel calculation done here
            val image = reader.acquireLatestImage()
            if (image != null) {

                if (processingSupport != null && mPreviewListener != null) {
                    val data = processingSupport.yuvToNv(image)
                    mPreviewListener?.onPreviewData(data, image.width, image.height)
                    val value = processingSupport.yuvSpToRedAvg(
                        data.clone(),
                        image.width,
                        image.height
                    )
                    mPreviewListener?.onPreviewCount(value)
                }
                image.close()
            }
        }

    init {
        this.mContext = mContext
        mCameraManager =
            mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        this.processingSupport = processingSupport
        val powerManager =
            mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DoNotDimScreen " + System.currentTimeMillis()
        )
    }

    companion object {
        private val ORIENTATIONS = mapOf(
            Surface.ROTATION_0 to 0,
            Surface.ROTATION_90 to 90,
            Surface.ROTATION_180 to 180,
            Surface.ROTATION_270 to 270,
        )

        private fun sensorToDeviceRotation(cameraCharacteristics: CameraCharacteristics, deviceOrientation: Int): Int {
            val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val deviceOrientationRealNum = ORIENTATIONS[deviceOrientation] ?: 0
            return (sensorOrientation + deviceOrientationRealNum + 360) % 360
        }

        private fun chooseOptimalSize(choices: List<Size>, width: Int, height: Int): Size {
            val bigEnough = mutableListOf<Size>()
            for (option in choices) {
                if(option.height == option.width * height / width &&
                    option.width > width && option.height > height) {
                    bigEnough.add(option)
                }
            }

            return  bigEnough.minByOrNull { it.width * it.height } ?: choices[0]
        }
    }
}