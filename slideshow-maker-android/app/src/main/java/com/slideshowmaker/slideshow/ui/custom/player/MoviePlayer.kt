package com.slideshowmaker.slideshow.ui.custom.player

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import java.io.File
import java.io.FileNotFoundException


class MoviePlayer(val videoPlayDrawer2: VideoPlayDrawer2) {

    private val sourceFilePath = "${FileHelper.internalPath}/deo60fps.mp4"
    private val sourceFile = File(sourceFilePath)

    private val buffer = MediaCodec.BufferInfo()

    @Volatile
    private var isStopRequest = false

    private var isLooping = false
    private var videoWidth = 0
    private var videoHeight = 0
    private var surfaceTexture: SurfaceTexture? = null

    init {
        val extractor: MediaExtractor
        try {
            Logger.e("file path = $sourceFilePath")
            extractor = MediaExtractor()
            extractor.setDataSource(sourceFilePath)
            val trackIndex = selectTrackIndex(extractor)
            if (trackIndex < 0) {
                throw FileNotFoundException("no video found in $sourceFilePath")
            }
            extractor.selectTrack(trackIndex)
            extractor.getTrackFormat(trackIndex).apply {
                videoWidth = getInteger(MediaFormat.KEY_WIDTH)
                videoHeight = getInteger(MediaFormat.KEY_HEIGHT)
            }

            surfaceTexture = SurfaceTexture(videoPlayDrawer2!!.textureId)

        } catch (e: Exception) {

        }
    }


    fun play() {
        val extractor: MediaExtractor
        val decoder: MediaCodec
        if (!sourceFile.canRead()) {
            throw FileNotFoundException("can't read file $sourceFilePath")
        }
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(sourceFilePath)
            val trackIndex = selectTrackIndex(extractor)
            if (trackIndex < 0) {
                throw FileNotFoundException("no video found in $sourceFilePath")
            }
            extractor.selectTrack(trackIndex)

            val audioIndex = selectAudioTrackIndex(extractor)
          //  extractor.selectTrack(audioIndex)

            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            decoder = MediaCodec.createDecoderByType(mime)

            decoder.apply {
                configure(format, null, null, 0)
                start()
            }
            doExtract(extractor, trackIndex, decoder)
        } catch (e: java.lang.Exception) {

        }
    }

    private fun doExtract(extractor: MediaExtractor, trackIndex: Int, decoder: MediaCodec) {
        val timeOutUSec = 10000L
        val decoderInputBuffers = decoder.inputBuffers
        var inputChunk = 0
        var firstInputTimeNsec = -1L
        var outputDone = false
        var inputDone = false
        while (!outputDone) {
            if (isStopRequest) {
                Logger.e("stop request")
                return
            }

            if (!inputDone) {
                val inputBufferIndex = decoder.dequeueInputBuffer(timeOutUSec)
                if (inputBufferIndex >= 0) {
                    if (firstInputTimeNsec == -1L) {
                        firstInputTimeNsec = System.nanoTime()
                    }
                    val inputBuf = decoderInputBuffers[inputBufferIndex]
                    val chunkSize = extractor.readSampleData(inputBuf, 0)
                    Logger.e("chunkSize = $chunkSize")
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                        Logger.e("end of stream")
                    } else {
                        if (extractor.sampleTrackIndex != trackIndex) {
                            Logger.e("WEIRD: got sample from track " + extractor.sampleTrackIndex + ", expected " + trackIndex)
                        }
                        val presentationTimeUs = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferIndex, 0, chunkSize, presentationTimeUs, 0)
                        inputChunk++
                        extractor.advance()
                    }
                } else {
                    Logger.e("input buffer not available")
                }
            }

            if (!outputDone) {
                val decodeStatus = decoder.dequeueOutputBuffer(buffer, timeOutUSec)
                if (decodeStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    Logger.e("not output available yet")
                } else if(decodeStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    Logger.e("decode output buffer change")
                } else if (decodeStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    Logger.e("decoder output format changed: $newFormat")
                } else if(decodeStatus < 0) {
                    throw RuntimeException("unexpected result from decoder.dequeueOutputBuffer: $decodeStatus")
                } else { //decode status >= 0
                    if(firstInputTimeNsec != 0L) {
                        val nowSec = System.nanoTime()
                        Logger.e("startup lag ${(firstInputTimeNsec-nowSec)/1000000} ms")
                        firstInputTimeNsec = 0L
                    }
                    var doLoop = false
                    if (buffer.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        if(isLooping) doLoop = true
                        else outputDone = true
                    }
                    val doRender = buffer.size != 0

                    if (doRender) {

                    }
                    // We use a very simple clock to keep the video FPS, or the video
                    // playback will be too fast
                    /*while (SystemColor.info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            sleep(10)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            break
                        }
                    }*/
          /*          try {
                        sleep(10)
                    } catch (e:java.lang.Exception) {

                    }*/

                    decoder.releaseOutputBuffer(decodeStatus, doRender)
                    if (doRender) {

                    }

                    if (doLoop) {
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        inputDone = false
                        decoder.flush() // reset decoder state

                    }
                }
            }
        }
    }

    private fun selectTrackIndex(extractor: MediaExtractor): Int {
        val numberTrack = extractor.trackCount
        for (index in 0 until numberTrack) {
            val format = extractor.getTrackFormat(index)
            format.getString(MediaFormat.KEY_MIME)?.let {
                if (it.startsWith("video/")) return index
            }

        }
        return -1
    }

    private fun selectAudioTrackIndex(extractor: MediaExtractor): Int {
        val numberTrack = extractor.trackCount
        for (index in 0 until numberTrack) {
            val format = extractor.getTrackFormat(index)
            format.getString(MediaFormat.KEY_MIME)?.let {
                if (it.startsWith("audio/")) return index
            }

        }
        return -1
    }


    interface PlayerFeedback {
        fun playbackStopped()
    }

    interface FrameCallback {
        fun preRender(presentationTimeUsec: Long)
        fun postRender()
        fun loopReset()
    }
}