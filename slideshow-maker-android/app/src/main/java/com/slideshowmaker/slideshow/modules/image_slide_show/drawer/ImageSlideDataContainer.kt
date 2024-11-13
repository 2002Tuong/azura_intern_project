package com.slideshowmaker.slideshow.modules.image_slide_show.drawer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import com.slideshowmaker.slideshow.utils.BitmapHelper
import com.slideshowmaker.slideshow.utils.FileHelper
import timber.log.Timber
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

class ImageSlideDataContainer constructor(val mImageList: ArrayList<String> = ArrayList()) {

    private val imageSlideDataList = ArrayList<ImageSlideData>()

    @Volatile
    var delayTimeInMillis = 1500

    val transitionTimeInMillis = 500
    private val fps = 25
    val imageList get() = mImageList

    /*    private lateinit var mCurrentSlide: ImageSlideData*/
    var currentSlideIndex = 0

    @Volatile
    private lateinit var currentBitmap: Bitmap

    @Volatile
    private lateinit var nextBitmap: Bitmap

    @Volatile
    private lateinit var backupBitmap: Bitmap

    private var fromLookupBitmap = BitmapHelper.getBitmapFromAsset("lut/DEFAULT.jpg")
    private var toLookupBitmap = BitmapHelper.getBitmapFromAsset("lut/DEFAULT.jpg")

    var ratio = 1f

    init {
        if (mImageList.isNotEmpty())
            initData()
    }

    fun initData() {
        File("${FileHelper.tempImageFolderPath}").deleteRecursively()
        imageSlideDataList.clear()
        if (mImageList.size > 0) {
            for (index in 0 until mImageList.size) {
                val nextImageDir = if (index < mImageList.size - 1) {
                    mImageList[index + 1]
                } else {
                    mImageList[mImageList.size - 1]
                }
                val imageSlideData =
                    ImageSlideData(
                        View.generateViewId() + System.currentTimeMillis(),
                        mImageList[index],
                        nextImageDir
                    )
                imageSlideDataList.add(imageSlideData)
            }

            val firstSlide = imageSlideDataList[0]
            currentBitmap = getBitmapResized(firstSlide.fromImagePath)
            currentSlideId = firstSlide.slideId
            val secondSlide = imageSlideDataList[1]
            nextBitmap = getBitmapResized(secondSlide.fromImagePath)

        }
        updateBackupSlide()

    }

    private val noneLutDir = "lut/DEFAULT.jpg"
    fun onRepeat() {
        currentSlideId = 1L
        val firstSlide = imageSlideDataList[0]
        currentBitmap = getBitmapResized(firstSlide.fromImagePath)
        nextBitmap = getBitmapResized(firstSlide.toImagePath)
        currentSlideId = firstSlide.slideId
        currentSlideIndex = 0
        fromLookupBitmap =
            BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex].lookupType}.jpg")

        toLookupBitmap = if (currentSlideIndex < imageSlideDataList.size - 1)
            BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex + 1].lookupType}.jpg")
        else BitmapHelper.getBitmapFromAsset(noneLutDir)

    }

    private fun getBitmapResized(path: String): Bitmap {
        val bitmapFile = File(path)
        return if (bitmapFile.exists()) {
            val outputName = "${bitmapFile.parentFile?.name}${bitmapFile.name}"
            val tmpImageFile = File("${FileHelper.tempImageFolderPath}/$outputName")
            if (tmpImageFile.exists()) {
                BitmapHelper.getBitmapFromFilePath(tmpImageFile.absolutePath)
            } else {
                val resizeBitmap = drawResizedBitmap(path)
                FileHelper.saveBitmapToTempData(resizeBitmap, outputName)
                resizeBitmap
            }
        } else {
            BitmapHelper.getBlackBitmap()
        }


    }

    private var currentSlideId = 1L
    fun getFrameDataByTime(timeMs: Int, needReload: Boolean = false): ImageSlideFrame {
        /*        var slideIndex = -1
                for(index in 0 until mImageSlideDataList.size) {
                    val startTime = index*(delayTimeMs+transitionTimeMs)
                    val endTime = startTime+(delayTimeMs+transitionTimeMs)
           *//*         if(timeMs in startTime+1 until endTime) {
                slideIndex = index
                break
            }*//*
            *//*if(timeMs in (startTime + 1)..endTime) {
                slideIndex = index
                break
            }*//*
            if(timeMs <= endTime) {
                slideIndex = index
                break
            }
        }*/
        var indexOfSlice = ((timeMs) / (delayTimeInMillis + transitionTimeInMillis))
        if (indexOfSlice == -1) indexOfSlice = imageSlideDataList.size - 1
        val targetSlide = imageSlideDataList[indexOfSlice]
        currentSlideIndex = indexOfSlice

        val delta = timeMs - indexOfSlice * (delayTimeInMillis + transitionTimeInMillis)
        var progress: Float
        progress = when {
            delta in 0..delayTimeInMillis -> 0f
            indexOfSlice == imageSlideDataList.size - 1 -> 0f
            else -> {
                ((delta - delayTimeInMillis).toFloat() / transitionTimeInMillis)
            }
        }

        calculateZoom(timeMs)

        if (needReload || targetSlide.slideId != currentSlideId) {
            if (targetSlide.slideId != currentSlideId) {
                val f = zoomValue
                zoomValue = zoom1Value
                zoom1Value = f

                val f2 = zoomFValue
                zoomFValue = zoomTValue
                zoomTValue = f2
            }
            fromLookupBitmap =
                BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex].lookupType}.jpg")

            toLookupBitmap = if (currentSlideIndex < imageSlideDataList.size - 1)
                BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex + 1].lookupType}.jpg")
            else BitmapHelper.getBitmapFromAsset(noneLutDir)

            currentBitmap = getBitmapResized(targetSlide.fromImagePath)
            nextBitmap = getBitmapResized(targetSlide.toImagePath)
            currentSlideId = targetSlide.slideId

        }
        //Logger.e("slide index = $slideIndex -- progress = $progress -- $mCurrentBitmap -- $mNextBitmap")
        return ImageSlideFrame(
            currentBitmap,
            nextBitmap,
            fromLookupBitmap,
            toLookupBitmap,
            progress,
            currentSlideId,
            zoomValue,
            zoom1Value
        )
    }

    private var zoomValue = 1f
    private var zoom1Value = 1f

    private val zoomDuration = 5000

    private var zoomFValue = 1f
    private var zoomTValue = 0.95f

    private fun calculateZoom(timeMs: Int) {
    }

    fun seekTo(timeMs: Int, needReload: Boolean = false): ImageSlideFrame {

        var indexOfSlice = ((timeMs) / (delayTimeInMillis + transitionTimeInMillis))

        if (indexOfSlice == imageSlideDataList.size) indexOfSlice = 0
        currentSlideIndex = indexOfSlice
        val targetSlide = imageSlideDataList[indexOfSlice]

        val surplus = timeMs % (delayTimeInMillis + transitionTimeInMillis)
        val progress: Float
        progress = if (surplus <= delayTimeInMillis) {
            0f
        } else {
            (surplus - delayTimeInMillis) / (transitionTimeInMillis.toFloat())
        }

        calculateZoom(timeMs)
        if (needReload || targetSlide.slideId != currentSlideId) {

            fromLookupBitmap =
                BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex].lookupType}.jpg")
            toLookupBitmap = if (currentSlideIndex < imageSlideDataList.size - 1)
                BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex + 1].lookupType}.jpg")
            else
                BitmapHelper.getBitmapFromAsset(noneLutDir)
            currentBitmap = getBitmapResized(targetSlide.fromImagePath)
            nextBitmap = getBitmapResized(targetSlide.toImagePath)
            currentSlideId = targetSlide.slideId

        }
        return ImageSlideFrame(
            currentBitmap,
            nextBitmap,
            fromLookupBitmap,
            toLookupBitmap,
            progress,
            currentSlideId,
            zoomValue,
            zoom1Value
        )
    }

    private fun updateBackupSlide() {
        Thread {

            for (index in 0 until imageSlideDataList.size) {
                val item = imageSlideDataList[index]

                /*if(index <= mImageSlideDataList.size-1)
                 mBackupBitmap = getBitmapResized(mImageSlideDataList[index + 1].toImagePath)
                 else {
                     mBackupBitmap = BitmapUtils.getBlackBitmap()
                 }*/
                getBitmapResized(item.fromImagePath)
                /*       if(index == mImageSlideDataList.size-1 ) {
                           mBackupBitmap = getBitmapResized(mImageSlideDataList[mImageSlideDataList.size-1].fromImagePath)
                       } else {
                           mBackupBitmap = getBitmapResized(mImageSlideDataList[index + 1].toImagePath)
                       }
                       break*/

            }


        }.start()
    }

    private fun drawResizedBitmap(imagePath: String): Bitmap {
        val screenWith: Int
        val screenHeight: Int
        if (ratio < 1) {
            screenHeight = 1080
            screenWith = (screenHeight.toFloat() * ratio).roundToInt()
        } else {
            screenWith = 1080
            screenHeight = (screenWith.toFloat() / ratio).roundToInt()
        }
        Timber.d("Resizing to $ratio of $screenWith:$screenHeight")
        val rawBitmap = BitmapHelper.getBitmapFromFilePath(imagePath)
        val outputBitmapSize: Int
        if (rawBitmap.width < screenWith && rawBitmap.height < screenWith) {
            outputBitmapSize = max(rawBitmap.width, rawBitmap.height)
        } else {
            outputBitmapSize = screenWith
        }

        val blurBgBitmap = BitmapHelper.blurBitmapV2(
            BitmapHelper.resizeMatchBitmap(
                rawBitmap,
                outputBitmapSize.toFloat() + 100
            ), 20
        )

        val rawBitmapResize = BitmapHelper.resizeWrapBitmap(rawBitmap, screenWith, screenHeight)

        val resizeBitmapWithBg =
            Bitmap.createBitmap(screenWith, screenHeight, Bitmap.Config.ARGB_8888)
        Timber.d("out bitmap size is ${screenWith} $screenHeight")
        Timber.d("rawBitmapResized bitmap size is ${rawBitmapResize.width} ${rawBitmapResize.height}")
        Canvas(resizeBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            blurBgBitmap?.let {
                val (src, dest) = getDesRect(it, ImageView.ScaleType.CENTER_CROP.ordinal, this)
                drawBitmap(
                    it,
                    src,
                    dest,
                    null
                )
            }
            drawBitmap(
                rawBitmapResize,
                (screenWith - rawBitmapResize.width) / 2f,
                (screenHeight - rawBitmapResize.height) / 2f,
                null
            )
        }

        return resizeBitmapWithBg
    }

    private fun getDesRect(
        currentBitmap: Bitmap,
        scaleType: Int,
        canvas: Canvas
    ): Pair<Rect, Rect> {
        return when (scaleType) {
            ImageView.ScaleType.FIT_XY.ordinal, ImageView.ScaleType.CENTER_INSIDE.ordinal -> {
                val src = Rect(0, 0, currentBitmap.width - 1, currentBitmap.height - 1)
                val ratio = src.width().toFloat() / src.height()
                val dest = when (scaleType) {
                    ImageView.ScaleType.FIT_XY.ordinal -> Rect(
                        0,
                        0,
                        canvas.width - 1,
                        canvas.height - 1
                    )

                    else -> getRect(canvas.width - 1, canvas.height - 1, ratio)
                }
                Pair(src, dest)
            }

            else -> {
                val dest = Rect(0, 0, canvas.width - 1, canvas.height - 1)
                val ratio = dest.width().toFloat() / dest.height()
                val src = getRect(currentBitmap.width - 1, currentBitmap.height - 1, ratio)
                Pair(src, dest)
            }
        }
    }

    private fun getRect(width: Int, height: Int, ratio: Float): Rect {
        return if (width.toFloat() / height > ratio) {
            val desWidth = (height * ratio).toInt()
            val left = (width - desWidth) / 2
            val right = left + desWidth
            Rect(left, 0, right, height)
        } else {
            val desHeight = (width / ratio).toInt()
            val top = (height - desHeight) / 2
            val bottom = top + desHeight
            Rect(0, top, width, bottom)
        }
    }

    fun getMaxDurationMs(): Int {
        return (delayTimeInMillis + transitionTimeInMillis) * imageSlideDataList.size
    }

    fun getStartTimeById(slideId: Long): Int {

        for (index in 0 until imageSlideDataList.size) {
            val item = imageSlideDataList[index]
            if (slideId == item.slideId) {
                return index * (delayTimeInMillis + transitionTimeInMillis)
            }
        }
        return 0
    }

    fun getCurrentDelayTimeMs(): Int = delayTimeInMillis

    fun getSlideList(): ArrayList<ImageSlideData> = imageSlideDataList

    fun prepareForRender(imageSlideDataList: ArrayList<ImageSlideData>, delayTime: Int) {
        this.imageSlideDataList.clear()
        this.imageSlideDataList.addAll(imageSlideDataList)
        currentSlideIndex = 0
        this.delayTimeInMillis = delayTime
        val firstSlide = this.imageSlideDataList[0]
        currentBitmap = getBitmapResized(firstSlide.fromImagePath)
        nextBitmap = getBitmapResized(firstSlide.toImagePath)
        currentSlideId = firstSlide.slideId
        val secondSlide = this.imageSlideDataList[1]
        backupBitmap = getBitmapResized(secondSlide.toImagePath)

        fromLookupBitmap =
            BitmapHelper.getBitmapFromAsset("lut/${this.imageSlideDataList[currentSlideIndex].lookupType}.jpg")

        toLookupBitmap = if (currentSlideIndex < this.imageSlideDataList.size - 1)
            BitmapHelper.getBitmapFromAsset("lut/${this.imageSlideDataList[currentSlideIndex + 1].lookupType}.jpg")
        else BitmapHelper.getBitmapFromAsset(noneLutDir)
    }

    fun getFrameByTimeForRender(timeMs: Int): ImageSlideFrame {
        var slideIndex = ((timeMs - 1) / (delayTimeInMillis + transitionTimeInMillis))

        if (slideIndex == imageSlideDataList.size) slideIndex--
        val targetSlide = imageSlideDataList[slideIndex]
        currentSlideIndex = slideIndex
        val surplus = (timeMs - 1) % (delayTimeInMillis + transitionTimeInMillis)
        val progress: Float
        progress = if (surplus <= delayTimeInMillis) {
            0f
        } else {
            (surplus + 1 - delayTimeInMillis) / transitionTimeInMillis.toFloat()
        }
        calculateZoom(timeMs)
        if (targetSlide.slideId != currentSlideId) {

            val f = zoomValue
            zoomValue = zoom1Value
            zoom1Value = f

            val f2 = zoomFValue
            zoomFValue = zoomTValue
            zoomTValue = f2

            fromLookupBitmap =
                BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex].lookupType}.jpg")

            toLookupBitmap = if (currentSlideIndex < imageSlideDataList.size - 1)
                BitmapHelper.getBitmapFromAsset("lut/${imageSlideDataList[currentSlideIndex + 1].lookupType}.jpg")
            else BitmapHelper.getBitmapFromAsset(noneLutDir)

            currentBitmap = getBitmapResized(targetSlide.fromImagePath)
            nextBitmap = getBitmapResized(targetSlide.toImagePath)
            currentSlideId = targetSlide.slideId

            /*       if (mCurrentSlideId== 1L) {
                       mBackupBitmap = getBitmapResized(mImageSlideDataList[0].fromImagePath)
                   } else {
                       for (index in 0..mImageSlideDataList.size) {
                           val item = mImageSlideDataList[index]
                           if (item.slideId == mCurrentSlideId) {
                               if(index < mImageSlideDataList.size-1)
                                   mBackupBitmap = getBitmapResized(mImageSlideDataList[index + 1].toImagePath)
                               else {
                                   mBackupBitmap = BitmapUtils.getBlackBitmap()
                               }
                               break
                           }
                       }
                   }*/
        }

        return ImageSlideFrame(
            currentBitmap, nextBitmap, fromLookupBitmap,
            toLookupBitmap = toLookupBitmap,
            progress = progress,
            slideId = currentSlideId,
            zoomProgress = zoomValue,
            zoomProgress1 = zoom1Value
        )
    }

    fun setNewImageList(pathList: ArrayList<String>) {
        mImageList.clear()
        mImageList.addAll(pathList)
        initData()
    }

    fun changeCurrentSlideId(id: Long) {
        currentSlideId = id
    }
}