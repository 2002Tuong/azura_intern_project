package com.slideshowmaker.slideshow.ui.export_video

import android.annotation.SuppressLint
import android.content.ContentValues
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.view.isVisible
import com.ads.control.ads.VioAdmob
import com.bumptech.glide.Glide
import com.daasuu.gpuv.composer.FillMode
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.daasuu.gpuv.egl.filter.GlFilter
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.StickerForRenderInfo
import com.slideshowmaker.slideshow.databinding.ActivityProcessVideoScreenBinding
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpeg
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpegCmd
import com.slideshowmaker.slideshow.modules.encode.ImageSlideEncode
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideData
import com.slideshowmaker.slideshow.modules.transition.GSTransitionUtils
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.share_video.ShareVideoActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import com.slideshowmaker.slideshow.utils.Utils
import com.slideshowmaker.slideshow.utils.extentions.isFirstInstall
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_process_video_screen.art
import kotlinx.android.synthetic.main.activity_process_video_screen.imgFilter
import kotlinx.android.synthetic.main.activity_process_video_screen.imgFrame
import kotlinx.android.synthetic.main.activity_process_video_screen.progressBar
import kotlinx.android.synthetic.main.activity_process_video_screen.tvProgressText
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class ProcessVideoActivity : BaseActivity() {

    companion object {
        const val action = "action"
        const val renderSlideActionCode = 1001
        const val renderVideoSlideActionCode = 1003
        const val joinVideoActonCode = 1002
        const val trimVideoActonCode = 1004
    }

    override fun getContentResId(): Int = R.layout.activity_process_video_screen

    val compoiteDisposable = CompositeDisposable()

    private var cancelable = false
    private lateinit var layoutBinding: ActivityProcessVideoScreenBinding
    private fun showNativeAds() {
    }

    override fun initViews() {
        layoutBinding = ActivityProcessVideoScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        val bundle = intent.getBundleExtra("bundle")
        val action = intent.getIntExtra(action, 1000)

        showNativeAds()
        AdsHelper.loadInterSuccess(this)

        when (action) {
            renderSlideActionCode -> {
                // Action save ảnh thành video từ màn ImageSlideShowActivity
                bundle?.let { it ->
                    if (SharedPreferUtils.exportSlideshowFirstTime && isFirstInstall()) {
                        TrackingFactory.Common.startExportingFirstTime().track()
                        SharedPreferUtils.exportSlideshowFirstTime = false
                    }
                    val imageSlideDatas =
                        it.getSerializable("imageDataList") as ArrayList<ImageSlideData>
                    val stickerAddeds =
                        it.getSerializable("stickerDataList") as ArrayList<StickerForRenderInfo>
                    val themeInfo = it.getString("themeData")
                    val themeScaleType = it.getInt("themeScaleType")
                    val frameInfoId = it.getString("frameData")
                    val filterInfoId = it.getString("filterData")!!
                    val delayTime = it.getInt("delayTime")
                    val musicFilePath = it.getString("musicPath") ?: ""
                    val musicVol = it.getFloat("musicVolume")
                    val videoQuality = it.getInt("videoQuality")
                    val lottiePath = it.getString("lottiePath").orEmpty()
                    val ratio = it.getFloat("ratio", 1f)
                    val width: Int
                    val height: Int
                    when {
                        ratio == 1f -> {
                            width = videoQuality
                            height = videoQuality
                        }

                        ratio > 1f -> {
                            height = videoQuality
                            width = (ratio * height).roundToInt()
                        }

                        else -> {
                            width = videoQuality
                            height = (width / ratio).toInt()
                        }
                    }
                    val gsTransitionsStr =
                        it.getSerializable("gsTransition") as ArrayList<String>
                    Glide.with(this).load(imageSlideDatas.firstOrNull()?.fromImagePath)
                        .into(art)

                    Glide.with(this)
                        .load(File(FileHelper.frameFolderPath).resolve(frameInfoId ?: ""))
                        .into(imgFrame)

                    Glide.with(this)
                        .load(File(FileHelper.filterFolderPath).resolve(filterInfoId))
                        .into(imgFilter)

                    val gsTransitions = gsTransitionsStr.map { transitionId ->
                        GSTransitionUtils.getGSTransitionList()
                            .firstOrNull { it.id == transitionId }
                            ?: GSTransition()
                    }
                    Observable.fromCallable {
                        val imageSlideEncodeObj = ImageSlideEncode(
                            listImageData = imageSlideDatas,
                            listStickerAdded = stickerAddeds,
                            listEffectPath = getEffectPath(themeInfo ?: ""),
                            effectScaleType = themeScaleType,
                            delayTime = delayTime,
                            audioPath = musicFilePath,
                            audioVolume = musicVol,
                            videoQuality = videoQuality,
                            gsTransition = gsTransitions,
                            frameId = frameInfoId,
                            filterDataId = filterInfoId,
                            width,
                            height,
                            lottiePath

                        )
                        imageSlideEncodeObj.performEncodeVideo({
                            runOnUiThread {
                                progressBar.setProgress(it * 100)
                                tvProgressText.text =
                                    getString(R.string.label_exporting_progress, (it * 100).toInt())
                            }
                        }, { outPath ->
                            val finalPath = saveVideo(outPath)
                            runOnUiThread {
                                onComplete(finalPath)
                            }
                        }, {
                            runOnUiThread {
                                onFailed()
                            }
                        })

                        return@fromCallable ""
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<String> {
                            override fun onNext(outPath: String) {

                            }

                            override fun onComplete() {

                            }

                            override fun onSubscribe(d: Disposable) {
                                Logger.e("renderSlideAction $d")
                                compoiteDisposable.add(d)
                            }

                            override fun onError(e: Throwable) {}
                        })


                }
            }

            joinVideoActonCode -> {
                val videoListStr = intent.getStringArrayListExtra("joinVideoList") as ArrayList<String>
                Glide.with(this).load(videoListStr.firstOrNull())
                    .into(art)
                videoPathForJoinArray.addAll(videoListStr)

                for (path in videoPathForJoinArray) {
                    joinVideoHashMap[path] = ""
                }
                joinVideoHashMap.forEach {
                    unDuplicateFilePathArray.add(it.key)
                }
                unDuplicateFilePathArray.forEach {
                    totalReSizeVideoDuration += MediaHelper.getAudioDuration(it).toInt()
                }
                joinVideoSize = selectTargetSize(videoPathForJoinArray)
                maximumJoinBitRate = selectMaxBitRate(videoPathForJoinArray)
                if (maximumJoinBitRate > 10000000) maximumJoinBitRate = 10000000
                Logger.e("join video size = ${joinVideoSize.width} - ${joinVideoSize.height}")
                doReSizeForJoinVideo()

            }

            trimVideoActonCode -> {
                tvProgressText.isVisible = false
                val inputPath = intent.getStringExtra("path") ?: ""
                val startTime = intent.getIntExtra("startTime", -1)
                val endTime = intent.getIntExtra("endTime", -1)

                Glide.with(this).load(inputPath)
                    .into(art)
                Logger.e("""trim $inputPath - $startTime -- $endTime""")
                Thread {
                    val outVideoPath = FileHelper.getOutputVideoPath()
                    val total = endTime - startTime
                    val startTimeString =
                        Utils.convertSecondsToTime((startTime.toFloat() / 1000).roundToInt())
                    val duration = Utils.convertSecondsToTime((total.toFloat() / 1000).roundToInt())
                    Logger.e("start = $startTimeString -- duration = $duration")
                    Logger.e("start time = $startTime --- end time = $endTime")
                    val ffmpeg =
                        FFmpeg(
                            FFmpegCmd.cutVideo(
                                inputPath,
                                startTime.toDouble(),
                                endTime.toDouble(),
                                outVideoPath
                            )
                        )
                    fFmpeg = ffmpeg
                    ffmpeg.runCmd({
                        runOnUiThread {
                            progressBar.setProgress(it * 100f / total)
                            tvProgressText.text =
                                getString(R.string.label_exporting_progress, (it * 100))
                        }
                    }, {
                        val latestFilePath = saveVideo(outVideoPath)
                        runOnUiThread {
                            if (!cancelable) {
                                logSaveSuccessEvent()
                                Thread {
                                    Thread.sleep(500)
                                    runOnUiThread {
                                        ShareVideoActivity.gotoActivity(
                                            this,
                                            latestFilePath,
                                            true,
                                            true,
                                            action
                                        )

                                        finish()
                                    }
                                }.start()


                            }

                        }
                    })
                }.start()
            }
        }

        hideHeader()
        initNativeAds()
    }

    private fun initNativeAds() {
        AdsHelper.apNativeSaving.observe(this) {
            if (it != null) {
                layoutBinding.frAds.visibility = View.VISIBLE
                VioAdmob.getInstance().populateNativeAdView(
                    this,
                    it,
                    layoutBinding.frAds,
                    layoutBinding.includeNative.shimmerContainerBanner
                )
                AdsHelper.apNativeSaving.value = null
            }
        }
        AdsHelper.apNativeSavingLoadFail.observe(this) {
            if (it) {
                layoutBinding.frAds.visibility = View.INVISIBLE
                AdsHelper.apNativeSavingLoadFail.value = false
            }
        }
    }

    private var fFmpeg: FFmpeg? = null

    private val videoPathForJoinArray = ArrayList<String>()
    private var joinVideoSize = Size(0, 0)
    private val joinVideoHashMap = HashMap<String, String>()
    private val unDuplicateFilePathArray = ArrayList<String>()
    private var joinProcessCount = 0
    private var maximumJoinBitRate = 0
    private var totalReSizeVideoDuration = 0
    private var currentReSizeVideoDuration = 0
    private var reSizedVideoDuration = 0
    private val onResizeDoJoinProgressHandle = { progress: Double ->
        val progress =
            0.9f * (progress * currentReSizeVideoDuration + reSizedVideoDuration) * 100f / totalReSizeVideoDuration
        runOnUiThread {
            progressBar.setProgress(progress.toFloat())
            tvProgressText.text =
                getString(R.string.label_exporting_progress, progress.toInt())
        }
    }

    private val onReSizeDoJoinFinishHandle = { inPath: String, outPath: String ->
        joinVideoHashMap[inPath] = outPath
        reSizedVideoDuration += MediaHelper.getAudioDuration(inPath).toInt()
        ++joinProcessCount
        if (joinProcessCount == unDuplicateFilePathArray.size) {
            runOnUiThread {
                tvProgressText.text = getString(R.string.label_exporting_progress, 90)
            }
            joinReSizeVideo()
        } else {
            doReSizeForJoinVideo()
        }
    }

    private fun joinReSizeVideo() {
        val finalPathArray = ArrayList<String>()
        videoPathForJoinArray.forEach {
            joinVideoHashMap[it]?.let { path ->
                finalPathArray.add(path)
            }
        }
        val outputPath = joiVideoSameType(finalPathArray)
        val finalOutPath = saveVideo(outputPath)
        runOnUiThread {
            onComplete(finalOutPath)
        }
    }

    private fun saveVideo(filePath: String): String {
        val fileName = "${getString(R.string.folder_name)}_${System.currentTimeMillis()}.mp4"
        val folderName = getString(R.string.folder_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/$folderName")
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Video.Media.TITLE, fileName)
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.IS_PENDING, true)
            }

            val uri = contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues,
            )
            if (uri != null) {
                val preFile = File(filePath)
                val stream = contentResolver.openOutputStream(uri)
                stream?.writeFrom(preFile)
                stream?.close()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, false)
                contentResolver.update(uri, contentValues, null, null)
                runCatching { preFile.delete() }
            }
            return "/storage/emulated/0/Movies/$folderName/$fileName"
        } else {
            val dir = File("${Environment.getExternalStorageDirectory()}/$folderName")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val preFile = File(filePath)
            val newFile = File(dir, fileName)
            val stream = FileOutputStream(newFile)
            stream.writeFrom(preFile)
            stream.close()
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Video.Media.TITLE, fileName)
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.DATA, newFile.absolutePath)
            }
            contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues,
            )
            runCatching { preFile.delete() }

            return newFile.absolutePath
        }
    }

    private fun OutputStream.writeFrom(file: File) {
        val fileInputStream = FileInputStream(file)
        use { stream ->
            val buf = ByteArray(1024)
            var writeLen: Int
            while (fileInputStream.read(buf).also { writeLen = it } > 0) {
                stream.write(buf, 0, writeLen)
            }
        }
        fileInputStream.close()
    }

    private fun doReSizeForJoinVideo() {

        val outputPath = FileHelper.getTempVideoPath()
        val path = unDuplicateFilePathArray[joinProcessCount]
        currentReSizeVideoDuration = MediaHelper.getAudioDuration(path).toInt()
        val glFilter = GlFilter()
        GPUMp4ComposerObj = GPUMp4Composer(path, outputPath)
            .size(joinVideoSize.width, joinVideoSize.height)
            .fillMode(FillMode.PRESERVE_ASPECT_FIT)
            .filter(glFilter)
            .videoBitrate(maximumJoinBitRate)
            .listener(object : GPUMp4Composer.Listener {
                override fun onFailed(exception: Exception?) {

                }

                override fun onProgress(progress: Double) {
                    Logger.e("$joinProcessCount -- progress = $progress")
                    onResizeDoJoinProgressHandle.invoke(progress)
                }

                override fun onCanceled() {

                }

                override fun onCompleted() {
                    onReSizeDoJoinFinishHandle.invoke(path, outputPath)
                }

            }).start()
    }

    override fun initActions() {
        layoutBinding.cancelButton.setOnClickListener {
            showYesNoDialog(getString(R.string.do_you_want_to_cancel)) {
                cancelable = true
                GPUMp4ComposerObj?.cancel()
                finish()
            }
        }
    }

    private fun selectTargetSize(videoPathList: ArrayList<String>): Size {
        var outputSize = Size(0, 0)
        for (path in videoPathList) {
            val vidSize = MediaHelper.getVideoSize(path)
            if (vidSize.width > outputSize.width) {
                outputSize = vidSize
            }
        }
        val finalWidth = if (outputSize.width % 2 == 1) {
            outputSize.width + 1
        } else {
            outputSize.width
        }
        val finalHeight = if (outputSize.height % 2 == 1) {
            outputSize.height + 1
        } else {
            outputSize.height
        }
        return Size(finalWidth, finalHeight)
    }

    private var exportFinish = false
    private var isPausing = false
    private var finalDir = ""

    private fun logSaveSuccessEvent() {
        when (intent.getIntExtra(action, 1000)) {
            renderSlideActionCode -> {
                if (SharedPreferUtils.saveSlideshowSuccessOnFirstLaunch && isFirstInstall()) {
                    SharedPreferUtils.saveSlideshowSuccessOnFirstLaunch = false
                    TrackingFactory.Common.saveSlideshowSuccessFirstLaunch().track()
                }
                TrackingFactory.SlideshowEditor.saveSuccess().track()
            }

            renderVideoSlideActionCode -> TrackingFactory.EditVideo.saveSuccess().track()
            trimVideoActonCode -> TrackingFactory.TrimVideo.saveSuccess().track()
            else -> TrackingFactory.JoinVideo.saveSuccess().track()
        }
    }

    private fun onFailed() {
        Toast.makeText(this, R.string.some_thing_went_wrong_try_again_please, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onComplete(filePath: String) {
        if (cancelable) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } else {
            logSaveSuccessEvent()
            exportFinish = true
            finalDir = filePath
            if (!isPausing) {

                AdsHelper.forceShowInterSuccess(this) {
                    ShareVideoActivity.gotoActivity(
                        this, filePath,
                        showRating = true,
                        fromProcess = true,
                        fromAction = intent.getIntExtra(action, 1000)
                    )
                    Timber.d("File path %s", filePath)
                    runOnUiThread {
                        progressBar.setProgress(100f)
                        tvProgressText.text = getString(R.string.label_exporting_progress, 100)
                    }

                    doSendBroadcast(filePath)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPausing) {
            isPausing = false
            if (exportFinish) {
                if (finalDir.isNotEmpty()) {
                    onComplete(finalDir)
                }
            }
        }

    }

    private var GPUMp4ComposerObj: GPUMp4Composer? = null
    private var mVideoOutRatio = 3
    private var mTempVideoSlidePathList = ArrayList<String>()
    private var mSlideMusicVolume = 0f
    private var mSlideVideoVolume = 0f
    private var mAudioPath = ""

    @SuppressLint("WrongConstant")
    private fun joiVideoSameType(
        pathList: ArrayList<String>,
        onProgress: ((Float) -> Unit)? = null
    ): String {
        var totalVideoDuration = 0
        pathList.forEach {
            totalVideoDuration += MediaHelper.getAudioDuration(it).toInt()
        }
        val outputJoinVideoPath = FileHelper.getTempVideoPath()
        val mediaMuxer = MediaMuxer(outputJoinVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var audioIndex = -1
        var videoIndex = -1
        for (path in pathList) {
            if (MediaHelper.videoHasAudio(path)) {
                val videoExtractor = MediaExtractor()
                videoExtractor.setDataSource(path)

                val audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(path)

                val formatOfAudio = MediaHelper.selectAudioTrack(audioExtractor)
                val formatOfVideo = MediaHelper.selectVideoTrack(videoExtractor)
                videoIndex = mediaMuxer.addTrack(formatOfVideo)
                audioIndex = mediaMuxer.addTrack(formatOfAudio)


                break
            }
        }

        if (audioIndex == -1) {
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(pathList[0])
            val formatOfVideo = MediaHelper.selectVideoTrack(videoExtractor)
            videoIndex = mediaMuxer.addTrack(formatOfVideo)
        }

        mediaMuxer.start()

        val buffer = ByteBuffer.allocate(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()
        var videoTimeOffset = 0L
        var audioTimeOffset = 0L

        for (path in pathList) {
            val videoHasAudio = MediaHelper.videoHasAudio(path)
            MediaHelper.getAudioDuration(path)
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(path)
            MediaHelper.selectVideoTrack(videoExtractor)
            Logger.e("$path has audio = $videoHasAudio")
            while (true) {
                val chunkSize = videoExtractor.readSampleData(buffer, 0)
                if (chunkSize >= 0) {

                    bufferInfo.presentationTimeUs = videoExtractor.sampleTime + videoTimeOffset
                    bufferInfo.flags = videoExtractor.sampleFlags
                    bufferInfo.size = chunkSize

                    mediaMuxer.writeSampleData(videoIndex, buffer, bufferInfo)

                    val progress = (bufferInfo.presentationTimeUs.toFloat() / 10 / totalVideoDuration)
                    onProgress?.invoke(progress)
                    videoExtractor.advance()

                } else {

                    break
                }

            }
            videoExtractor.release()
            if (videoHasAudio) {
                val audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(path)
                MediaHelper.selectAudioTrack(audioExtractor)
                while (true) {
                    val chunkSize = audioExtractor.readSampleData(buffer, 0)
                    if (chunkSize >= 0) {

                        bufferInfo.presentationTimeUs = audioExtractor.sampleTime + audioTimeOffset
                        bufferInfo.flags = audioExtractor.sampleFlags
                        bufferInfo.size = chunkSize

                        mediaMuxer.writeSampleData(audioIndex, buffer, bufferInfo)

                        audioExtractor.advance()

                    } else {

                        break
                    }
                }
                audioExtractor.release()
            }


            val duration = MediaHelper.getVideoDuration(path) * 1000
            videoTimeOffset += duration
            audioTimeOffset += duration
        }

        mediaMuxer.stop()
        mediaMuxer.release()
        return outputJoinVideoPath
    }

    private fun selectMaxBitRate(videoList: ArrayList<String>): Int {
        var max = 0
        videoList.forEach {
            val bit = MediaHelper.getVideoBitRare(it)
            if (bit > max) max = bit
        }
        if (max > 10000000) max = 10000000
        return max
    }

    override fun onPause() {
        super.onPause()
        isPausing = true
    }


    override fun onDestroy() {
        super.onDestroy()

        try {
            GPUMp4ComposerObj?.cancel()
            fFmpeg?.cancel()
        } catch (e: Exception) {

        }

    }

    override fun onBackPressed() {
        showYesNoDialog(getString(R.string.do_you_want_to_cancel)) {
            AdsHelper.loadInterSuccess(this)
            cancelable = true
            finish()
        }

    }

}

@WorkerThread
fun getEffectPath(idTheme: String): List<String> {
    if (idTheme == "") return emptyList()
    val dir: File = File(FileHelper.themeFolderUnzip).resolve(idTheme)
    val totalNumFiles = dir.listFiles()?.size?.toLong() ?: 0
    return (1..totalNumFiles).map {
        dir.resolve("d$it.png").path
    }
}
