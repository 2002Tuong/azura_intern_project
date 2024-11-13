package com.slideshowmaker.slideshow.modules.encode

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.GLES20
import android.view.Surface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.models.StickerDrawerInfo
import com.slideshowmaker.slideshow.data.models.StickerForRenderInfo
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpeg
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpegCmd
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.EffectImageSlideThemeDrawer
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideData
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideDataContainer
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideDrawer
import com.slideshowmaker.slideshow.modules.slide_show_package_2.slide_show_gl_view_2.FrameAndFilterSlideShowDrawer
import com.slideshowmaker.slideshow.modules.slide_show_package_2.slide_show_gl_view_2.StickerDrawer
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import com.slideshowmaker.slideshow.utils.BitmapHelper
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import java.io.File
import java.util.Locale

class ImageSlideEncode constructor(
    private val listImageData: ArrayList<ImageSlideData>,
    private val listStickerAdded: ArrayList<StickerForRenderInfo>,
    private val listEffectPath: List<String>?,
    private val effectScaleType: Int,
    private val delayTime: Int,
    private val audioPath: String,
    private val audioVolume: Float,
    private val videoQuality: Int,
    private val gsTransition: List<GSTransition>,
    private val frameId: String?,
    private val filterDataId: String,
    private val outputWidthValue: Int,
    private val outputHeightValue: Int,
    private val lottiePath: String = ""
) {

    private var width = outputWidthValue
    private var height = outputHeightValue
    private var bitRare = 2500000


    private val MIME_TYPE = "video/avc"
    private val FRAME_RATE = 24 //fps
    private val IFRAME_INTERVAl = 1 // 10 sec between I-frame

    private var encoder = MediaCodec.createEncoderByType(MIME_TYPE)
    private lateinit var inputSurface: CodecInputSurface
    private lateinit var muxer: MediaMuxer

    private var trackIndex = 0
    private var muxerStarted = false

    private val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()

    private var outputFilePath = ""
    private val stickerDrawerDataList = ArrayList<StickerDrawerInfo>()
    private val imageSlideDataContainer = ImageSlideDataContainer()
    private val imageSlideDrawer = ImageSlideDrawer()
    private var effectImageSlideThemeDrawer: EffectImageSlideThemeDrawer? = null
    private var frameDrawer: FrameAndFilterSlideShowDrawer? = null
    private var filterDrawer: FrameAndFilterSlideShowDrawer? = null
    private var isUseTheme = true
    private var isUseFrame = true
    private var isUseFilter = true
    private val totalVideoTime =
        (imageSlideDataContainer.transitionTimeInMillis + delayTime) * listImageData.size

    fun performEncodeVideo(
        onUpdateProgress: (Float) -> Unit,
        onComplete: (String) -> Unit,
        onFailed: () -> Unit
    ) {

        imageSlideDataContainer.ratio = width.toFloat() / height
        imageSlideDataContainer.prepareForRender(listImageData, delayTime)
        if (width == 1080) {
            bitRare = 10000000
        } else if (width == 720) {
            bitRare = 7000000
        }
        try {
            prepareEncode()
            inputSurface.makeCurrent()
            imageSlideDrawer.prepare(gsTransition.firstOrNull() ?: GSTransition())
            if (!listEffectPath.isNullOrEmpty()) {
                isUseTheme = true
                effectImageSlideThemeDrawer = EffectImageSlideThemeDrawer(
                    effectPathList = listEffectPath,
                    effectScaleType = effectScaleType,
                    outputVideoWidthValue = width,
                    outputVideoHeightValue = height
                )
            } else {
                isUseTheme = false
            }

            // Frame
            isUseFrame = !(frameId == null || frameId == "none")

            if (isUseFrame) {
                frameDrawer = FrameAndFilterSlideShowDrawer().apply {
                    prepare(
                        BitmapHelper.getStickerFromFilePath(
                            File(FileHelper.frameFolderPath).resolve(
                                frameId!!
                            ).path
                        )
                    )
                }
            }

            // Filter
            isUseFilter = filterDataId != "none"
            if (isUseFilter) {
                filterDrawer = FrameAndFilterSlideShowDrawer().apply {
                    prepare(
                        BitmapHelper.getStickerFromFilePath(
                            File(FileHelper.filterFolderPath).resolve(
                                filterDataId
                            ).path
                        )
                    )
                }

            }


            for (item in listStickerAdded) {
                val stickerDrawer = StickerDrawer().apply {
                    prepare(BitmapHelper.getStickerFromFilePath(item.stickerPath))
                }
                val stickerDrawerInfo = StickerDrawerInfo(item.startOffset, item.endOffset, stickerDrawer)
                stickerDrawerDataList.add(stickerDrawerInfo)
            }

            GLES20.glClearColor(0f, 0f, 0f, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            //gen frame


            if (isUseTheme) {
                Thread.sleep(1000)
                var currentImageIndex = 0
                for (time in 0 until totalVideoTime step 40) {
                    drainEncoder(false)
                    if (imageSlideDataContainer.currentSlideIndex != currentImageIndex) {
                        currentImageIndex++
                        imageSlideDrawer.prepare(
                            gsTransition.getOrNull(currentImageIndex)
                                ?: gsTransition.firstOrNull()
                                ?: GSTransition()
                        )
                    }
                    imageSlideDrawer.changeFrameData(
                        imageSlideDataContainer.getFrameByTimeForRender(
                            time
                        ), true
                    )
                    imageSlideDrawer.drawFrame()
//                    Thread.sleep(16)

                    // Drawer Filter
                    filterDrawer?.drawFrame()

                    // Drawer Frame
                    frameDrawer?.drawFrame()

                    effectImageSlideThemeDrawer?.drawFrame(time)
                    // Logger.e("progress = ${time}/${totalVideoTime}")
                    for (item in stickerDrawerDataList) {
                        if (time >= item.startOffset && time <= item.endOffset) item.stickerDrawer.drawFrame()
                    }
                    inputSurface.setPresentationTime(time * 1000000L)
                    inputSurface.swapBuffer()
                    onUpdateProgress.invoke(time.toFloat() * 0.9f / totalVideoTime)
                }
            } else {

                if (stickerDrawerDataList.size == 0) {
                    val delayTime = imageSlideDataContainer.delayTimeInMillis
                    val transitionTime = imageSlideDataContainer.transitionTimeInMillis
                    var currentTimeInMillis = 0
                    for (index in 0 until listImageData.size) {
                        drainEncoder(false)
                        if (gsTransition.size > 1)
                            imageSlideDrawer.prepare(
                                gsTransition.getOrNull(index) ?: gsTransition.firstOrNull()
                                ?: GSTransition()
                            )
                        imageSlideDrawer.changeFrameData(
                            imageSlideDataContainer.getFrameByTimeForRender(
                                currentTimeInMillis
                            ), true
                        )
                        imageSlideDrawer.drawFrame()

                        // Drawer Filter
                        filterDrawer?.drawFrame()

                        // Drawer Frame
                        frameDrawer?.drawFrame()

                        inputSurface.setPresentationTime(currentTimeInMillis * 1000000L)
                        inputSurface.swapBuffer()
                        onUpdateProgress.invoke(currentTimeInMillis.toFloat() * 0.9f / totalVideoTime)
                        currentTimeInMillis += delayTime

                        /*drainEncoder(false)
                        mImageSlideDrawer.changeFrameData(mImageSlideDataContainer.getFrameByTimeForRender(currentTimeMs), true)
                        mImageSlideDrawer.drawFrame()
                        mInputSurface.setPresentationTime(currentTimeMs * 1000000L)
                        mInputSurface.swapBuffer()
                        onUpdateProgress.invoke(currentTimeMs.toFloat()*0.9f/totalVideoTime)*/

                        for (time in currentTimeInMillis until currentTimeInMillis + transitionTime step 40) {
                            drainEncoder(false)
                            imageSlideDrawer.changeFrameData(
                                imageSlideDataContainer.getFrameByTimeForRender(
                                    time
                                ), true
                            )
                            imageSlideDrawer.drawFrame()

                            // Drawer Filter
                            filterDrawer?.drawFrame()

                            // Drawer Frame
                            frameDrawer?.drawFrame()


                            inputSurface.setPresentationTime(time * 1000000L)
                            inputSurface.swapBuffer()
                            onUpdateProgress.invoke(time.toFloat() * 0.9f / totalVideoTime)
                        }
                        currentTimeInMillis += transitionTime
                    }
                } else {
                    val delayTime = imageSlideDataContainer.delayTimeInMillis
                    val transitionTime = imageSlideDataContainer.transitionTimeInMillis
                    var currentTimeInMillis = 0
                    for (index in 0 until listImageData.size) {
                        if (gsTransition.size > 1)
                            imageSlideDrawer.prepare(
                                gsTransition.getOrNull(index) ?: gsTransition.firstOrNull()
                                ?: GSTransition()
                            )
                        for (time in currentTimeInMillis until currentTimeInMillis + delayTime step 1000) {
                            drainEncoder(false)
                            val slideData =
                                imageSlideDataContainer.getFrameByTimeForRender(currentTimeInMillis)
                            imageSlideDrawer.changeFrameData(slideData, true)
                            imageSlideDrawer.drawFrame()

                            // Drawer Filter
                            filterDrawer?.drawFrame()

                            // Drawer Frame
                            frameDrawer?.drawFrame()

                            for (stickerDrawer in stickerDrawerDataList) {
                                if (time >= stickerDrawer.startOffset && time <= stickerDrawer.endOffset) stickerDrawer.stickerDrawer.drawFrame()
                            }
                            inputSurface.setPresentationTime(time * 1000000L)
                            inputSurface.swapBuffer()
                            onUpdateProgress.invoke(time.toFloat() * 0.9f / totalVideoTime)
                        }
                        currentTimeInMillis += delayTime
                        for (time in currentTimeInMillis until currentTimeInMillis + transitionTime step 40) {
                            drainEncoder(false)
                            imageSlideDrawer.changeFrameData(
                                imageSlideDataContainer.getFrameByTimeForRender(
                                    time
                                ), true
                            )
                            imageSlideDrawer.drawFrame()

                            // Drawer Filter
                            filterDrawer?.drawFrame()

                            // Drawer Frame
                            frameDrawer?.drawFrame()

                            for (stickerDrawer in stickerDrawerDataList) {
                                if (time >= stickerDrawer.startOffset && time <= stickerDrawer.endOffset) stickerDrawer.stickerDrawer.drawFrame()
                            }
                            inputSurface.setPresentationTime(time * 1000000L)
                            inputSurface.swapBuffer()
                            onUpdateProgress.invoke(time.toFloat() * 0.9f / totalVideoTime)
                        }
                        currentTimeInMillis += transitionTime
                    }
                }
            }


            /////////////////////////////////

            drainEncoder(true)
            releaseEncoder()
            //  val delta = System.currentTimeMillis() - start
            // Logger.e("total time = $delta  --- ${(delta / 1000f).roundToInt()}")


            addMusic(onComplete, onFailed, onUpdateProgress)
            // val outSpaceSize = totalVideoTime/1000*(mBitRare/10f.pow(6)/8)
            //  Logger.e("outSpaceSize  = $outSpaceSize")
        } catch (e: Exception) {
            e.printStackTrace()
            TrackingFactory.Common.encodeSlideshowFailed().track()
            Firebase.crashlytics.recordException(e)
            onFailed()
        } finally {
            //releaseEncoder()
        }

    }

    private var finalFilePath = ""
    private fun addMusic(
        onComplete: (String) -> Unit,
        onFailed: () -> Unit,
        onUpdateProgress: ((Float) -> Unit)? = null
    ) {
        Logger.e("music path = $audioPath")
        val mimeType = MediaHelper.getVideoMimeType(audioPath)
        Logger.e("mime type = $mimeType")

        if (audioPath.length < 5) {
            finalFilePath = FileHelper.getOutputVideoPath(width)
            val result = File(outputFilePath).renameTo(File(finalFilePath))
            if (result)
                onComplete.invoke(finalFilePath)
            else {
                onFailed.invoke()
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception("Unable to rename video file"))
            }
            return
        }

        if (audioPath.lowercase(Locale.ROOT).contains(".m4a")) {
            finalFilePath = FileHelper.getOutputVideoPath(width)
            val tmpAudioPath = FileHelper.getTempM4aAudioPath()
            FFmpeg(
                FFmpegCmd.trimAudio2(
                    audioPath,
                    0,
                    totalVideoTime.toLong(),
                    tmpAudioPath
                )
            ).apply {
                runCmd {
                    FFmpeg(
                        FFmpegCmd.mergeAudioToVideo(
                            tmpAudioPath,
                            outputFilePath,
                            finalFilePath,
                            audioVolume
                        )
                    ).apply {
                        runCmd({
                            onUpdateProgress?.invoke(0.9f + (it.toFloat() * 0.1f / totalVideoTime))
                        }, {
                            if (it)
                                onComplete.invoke(finalFilePath)
                            else onFailed.invoke()
                        })
                    }
                }
            }
        } else {
            finalFilePath = FileHelper.getOutputVideoPath(width)

            if (audioPath.isNotEmpty()) {
                FFmpeg(
                    FFmpegCmd.mergeAudioToVideo(
                        audioPath,
                        outputFilePath,
                        finalFilePath,
                        audioVolume
                    )
                ).apply {
                    runCmd({
                        onUpdateProgress?.invoke(0.9f + (it.toFloat() * 0.1f / totalVideoTime))
                    }, {
                        if (it)
                            onComplete.invoke(finalFilePath)
                        else onFailed.invoke()
                    })
                }
            }
        }


    }

    fun releaseEncoder() {

        if (encoder != null) {
            encoder.stop()
            encoder.release()

        }
        if (inputSurface != null) {
            inputSurface.release()
        }
        if (muxer != null) {
            muxer.stop()
            muxer.release()

        }
        effectImageSlideThemeDrawer?.release()
        effectImageSlideThemeDrawer = null
    }

    private fun prepareEncode() {
        val mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRare)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAl)
        }
        encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = CodecInputSurface(encoder.createInputSurface())
        encoder.start()
        outputFilePath = FileHelper.getTempVideoPath()
        try {
            muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        trackIndex = -1
        muxerStarted = false
    }

    private fun drainEncoder(enOfStream: Boolean) {
        val timeOutUSec = 10000L
        if (enOfStream) {
            encoder.signalEndOfInputStream()
        }

        var encoderOutputBuffers = encoder.outputBuffers
        while (true) {
            val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, timeOutUSec)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {

                if (!enOfStream) {
                    break
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                encoderOutputBuffers = encoder.outputBuffers
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                if (muxerStarted) {
                    throw RuntimeException("format changed twice")
                }
                val mediaFormat = encoder.outputFormat
                trackIndex = muxer.addTrack(mediaFormat)
                muxer.start()
                muxerStarted = true
            } else if (encoderStatus < 0) {
                Logger.e("unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
            } else {

                val encodedData = encoderOutputBuffers[encoderStatus]

                if (encodedData == null) {
                    Logger.e("encoderOutputBuffer $encoderStatus was null")
                    throw RuntimeException("encoderOutputBuffer $encoderStatus was null")
                }

                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0

                }

                if (bufferInfo.size != 0) {
                    if (!muxerStarted) {
                        throw RuntimeException("muxer hasn't started")
                    }

                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)

                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                encoder.releaseOutputBuffer(encoderStatus, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!enOfStream) {
                        Logger.e("reached end of stream unexpectedly")
                    } else {
                        Logger.e("end of stream reached")
                    }
                    break // out of while
                }
            }
        }
    }


    private class CodecInputSurface(val mSurface: Surface) {
        private val EGL_RECORDABLE_ANDROID = 0x3142

        private var EGLDisplay = EGL14.EGL_NO_DISPLAY
        private var EGLContext = EGL14.EGL_NO_CONTEXT
        private var EGLSurface = EGL14.EGL_NO_SURFACE

        init {
            eglSetup()
        }

        fun eglSetup() {
            EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (EGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("unable to get EGL14 display")
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(EGLDisplay, version, 0, version, 1)) {
                throw RuntimeException("unable to initialize EGL14");
            }

            val attrList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
            )

            val ElGconfigs: Array<EGLConfig?> = arrayOfNulls(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(EGLDisplay, attrList, 0, ElGconfigs, 0, ElGconfigs.size, numConfigs, 0)
            val attr_list = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            EGLContext =
                EGL14.eglCreateContext(EGLDisplay, ElGconfigs[0], EGL14.EGL_NO_CONTEXT, attr_list, 0)

            val surfaceAttrs = intArrayOf(EGL14.EGL_NONE)
            EGLSurface =
                EGL14.eglCreateWindowSurface(EGLDisplay, ElGconfigs[0], mSurface, surfaceAttrs, 0)
        }

        fun makeCurrent() {
            EGL14.eglMakeCurrent(EGLDisplay, EGLSurface, EGLSurface, EGLContext);
        }

        fun swapBuffer(): Boolean {
            return EGL14.eglSwapBuffers(EGLDisplay, EGLSurface)
        }

        fun setPresentationTime(nSecs: Long) {
            EGLExt.eglPresentationTimeANDROID(EGLDisplay, EGLSurface, nSecs)
        }

        fun release() {
            if (EGLDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    EGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT
                )
                EGL14.eglDestroySurface(EGLDisplay, EGLSurface)
                EGL14.eglDestroyContext(EGLDisplay, EGLContext)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(EGLDisplay)
            }
            mSurface.release()
            EGLDisplay = EGL14.EGL_NO_DISPLAY
            EGLContext = EGL14.EGL_NO_CONTEXT
            EGLSurface = EGL14.EGL_NO_SURFACE

        }

    }

}