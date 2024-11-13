package com.slideshowmaker.slideshow.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.BitmapWithIndexInfo
import com.slideshowmaker.slideshow.data.models.RecordedInfo
import com.slideshowmaker.slideshow.models.RecordedModel
import com.slideshowmaker.slideshow.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.max
import kotlin.math.roundToInt

class VideoTimelineView : View {

    private val omagePreviewPaths = ArrayList<String>()
    private var bitmaps = ArrayList<BitmapWithIndexInfo>()
    private var deltaX = 0f

    private var centerPoint = 0f

    private var middleLineWidth = 2f
    private var middleLineHeight = 88f

    private var density = 1f

    private val normalPaint = Paint()

    private var isRecord = false

    private var maxValueTimeInMil = 0

    private val textPaint = Paint()

    private var sizeOfTimeline = 1
    private var heightOfTimeline = 56f
    private val recordedInfoList = ArrayList<RecordedInfo>()
    private val markedPaint = Paint().apply {
        color = Color.parseColor("#66ff0000")
    }
    var onMoveHandle: ((Int) -> Unit)? = null
    var onStopRecordingHandle: ((Int) -> Unit)? = null
    var onStropSuccessHandle: ((RecordedInfo) -> Unit)? = null
    var onStartFailHandle: (() -> Unit)? = null
    var onUpCallbackHandle:((Int)->Unit)? = null
    private var textSize = 12f

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attributes: AttributeSet?) {

        density = DimenUtils.density(context)
        middleLineHeight *= density
        middleLineWidth *= density
        heightOfTimeline *= density

        textSize *= density

        normalPaint.apply {
            color = Color.parseColor("#ff604d")
            isAntiAlias = true
        }

        textPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = textSize
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMear = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        centerPoint = widthMear / 2f
        deltaX = centerPoint
    }

    override fun onDraw(canvas: Canvas) {
        if (sizeOfTimeline > 0)
            for (index in 0 until bitmaps.size) {
                val bitmap = bitmaps[index]
                canvas.drawBitmap(
                    bitmap.bitmap,
                    deltaX + density * 56 * index,
                    (middleLineHeight - bitmap.bitmap.height) / 2f,
                    null
                )
            }
        if (sizeOfTimeline > 0)
            for (item in recordedInfoList) {
                if (sizeOfTimeline > 0)
                    drawMark(canvas, item.startMs, item.endMs)
            }
        if (isRecord) {
            drawRecordingMark(canvas)
        }
        drawCenterLine(canvas)
        drawText(canvas)
        drawCurrentTimeText(canvas)
    }

    private fun drawCenterLine(canvas: Canvas?) {
        val circleRadius = 6 * density
        canvas?.drawRect(
            centerPoint - middleLineWidth / 2f,
            circleRadius,
            centerPoint + middleLineWidth / 2f,
            middleLineHeight - circleRadius,
            normalPaint
        )
        canvas?.drawCircle(centerPoint, circleRadius, circleRadius, normalPaint)
        canvas?.drawCircle(centerPoint, middleLineHeight - circleRadius, circleRadius, normalPaint)
    }

    private fun drawMark(canvas: Canvas?, startMs: Int, endMs: Int) {
        val startOffset = (startMs * sizeOfTimeline) / maxValueTimeInMil
        val endOffset = (endMs * sizeOfTimeline) / maxValueTimeInMil
        canvas?.drawRect(
            deltaX + startOffset,
            (middleLineHeight - heightOfTimeline) / 2f,
            deltaX + startOffset + (endOffset - startOffset),
            (middleLineHeight + heightOfTimeline) / 2f,
            markedPaint
        )
    }

    private fun drawRecordingMark(canvas: Canvas?) {
        val startOffset = (startRecordingOffset * sizeOfTimeline) / maxValueTimeInMil
        val endOffset = (endRecordingOffset * sizeOfTimeline) / maxValueTimeInMil
        canvas?.drawRect(
            deltaX + startOffset,
            (middleLineHeight - heightOfTimeline) / 2f,
            deltaX + startOffset + (endOffset - startOffset),
            (middleLineHeight + heightOfTimeline) / 2f,
            markedPaint
        )
    }

    private fun drawText(canvas: Canvas?) {
        val constTotalText = context.getString(R.string.total)
        val drawText =
            "$constTotalText:${Utils.convertSecToTimeString((maxValueTimeInMil / 1000f).roundToInt())}"
        val textWidth = Utils.getTextWidth(drawText, textPaint)
        val textHeight = Utils.getTextHeight(drawText, textPaint)
        canvas?.drawText(drawText, width - textWidth - 12 * density, middleLineHeight + textHeight, textPaint)
    }

    private fun drawCurrentTimeText(canvas: Canvas?) {
        val drawText = Utils.convertSecToTimeString(getCurrentTime().toInt() / 1000)
        val textWeight = Utils.getTextWidth(drawText, textPaint)
        val textHeight = Utils.getTextHeight(drawText, textPaint)
        canvas?.drawText(drawText, width / 2 - textWeight / 2, middleLineHeight + textHeight, textPaint)
    }


    fun loadImage(imagePathList: ArrayList<String>) {
        Observable.fromCallable<String> {
            if (imagePathList.size >= 10) {
                val durationPerImage = maxValueTimeInMil / 10
                for (index in 0 until 10) {
                    sizeOfTimeline += (56 * density).toInt()
                    val bitmap =
                        drawResizedBitmap(imagePathList[(index * durationPerImage) * imagePathList.size / maxValueTimeInMil])
                    bitmaps.add(BitmapWithIndexInfo(index, bitmap))
                    Logger.e("path = ${imagePathList[index]}")
                }
            } else {
                for (index in 0 until imagePathList.size) {
                    sizeOfTimeline += (56 * density).toInt()
                    val bitmap = drawResizedBitmap(imagePathList[index])
                    bitmaps.add(BitmapWithIndexInfo(index, bitmap))
                    Logger.e("path = ${imagePathList[index]}")
                }
            }

            return@fromCallable ""
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onNext(t: String) {

                }

                override fun onComplete() {

                    invalidate()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }

    fun loadImageVideo(videoPathList: ArrayList<String>) {
        Observable.fromCallable<String> {

            val durationPerImage = maxValueTimeInMil / 10
            for (index in 0 until 10) {
                val targetTimeInMillis = index * durationPerImage
                sizeOfTimeline += (56 * density).toInt()
                for (videoPath in videoPathList) {
                    val videoDuration = MediaHelper.getVideoDuration(videoPath)
                    var total = 0
                    if (total + videoDuration > targetTimeInMillis) {
                        val media = MediaMetadataRetriever()
                        media.setDataSource(videoPath)
                        val extractedImage = media.getFrameAtTime(
                            (targetTimeInMillis - total).toLong() * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                        extractedImage?.also { val bitmap = drawResizedBitmap(extractedImage)
                            bitmaps.add(BitmapWithIndexInfo(index, bitmap)) }

                        break
                    } else {
                        total += videoDuration
                    }
                }
                Logger.e("path = ${videoPathList[index]}")
            }

            return@fromCallable ""
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onNext(t: String) {

                }

                override fun onComplete() {
                    invalidate()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })


    }

    private var touchedPointX = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isRecord) return false
        if (event?.action == MotionEvent.ACTION_DOWN) {
            updateTouchPoint(event.rawX)
        } else if (event?.action == MotionEvent.ACTION_MOVE) {
            onMoveView(event.rawX)
        } else if(event?.action == MotionEvent.ACTION_CANCEL || event?.action == MotionEvent.ACTION_UP) {
            onUpCallbackHandle?.invoke(getCurrentTime().toInt())
        }

        return true
    }

    private fun updateTouchPoint(rawX: Float) {
        touchedPointX = rawX
    }

    private var currentTimeInMs = 0
    private fun onMoveView(rawX: Float) {

        var distanceX = deltaX + rawX - touchedPointX
        if ((distanceX + sizeOfTimeline) <= centerPoint) {
            distanceX = centerPoint - sizeOfTimeline
        } else if (distanceX >= centerPoint) {
            distanceX = centerPoint
        }
        deltaX = distanceX
        onMoveHandle?.invoke(getCurrentTime().roundToInt())
        currentTimeInMs = getCurrentTime().roundToInt()
        updateTouchPoint(rawX)
        invalidate()
    }


    private fun drawResizedBitmap(imagePath: String): Bitmap {
        val size = (DimenUtils.density(context) * 56).toInt()

        val bitmap = BitmapHelper.getBitmapFromFilePath(imagePath)
        var outputBitmapSize: Int
        outputBitmapSize = if (bitmap.width < size && bitmap.height < size) {
            max(bitmap.width, bitmap.height)
        } else {
            size
        }
        outputBitmapSize = size
        Logger.e("out size = $outputBitmapSize")
        val resizedBitmap = BitmapHelper.resizeMatchBitmap(bitmap, outputBitmapSize.toFloat())

        val resizedBitmapWithBg =
            Bitmap.createBitmap(outputBitmapSize, outputBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            drawBitmap(
                resizedBitmap,
                (outputBitmapSize - resizedBitmap.width) / 2f,
                (outputBitmapSize - resizedBitmap.height) / 2f,
                null
            )
        }

        return resizedBitmapWithBg
    }
    private fun drawResizedBitmap(bitmap: Bitmap): Bitmap {
        val size = (DimenUtils.density(context) * 56).toInt()

        val bitmap = bitmap
        var outputBitmapSize: Int
        outputBitmapSize = if (bitmap.width < size && bitmap.height < size) {
            max(bitmap.width, bitmap.height)
        } else {
            size
        }
        outputBitmapSize = size
        Logger.e("out size = $outputBitmapSize")
        val resizedBitmap = BitmapHelper.resizeMatchBitmap(bitmap, outputBitmapSize.toFloat())

        val resizedBitmapWithBg =
            Bitmap.createBitmap(outputBitmapSize, outputBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            drawBitmap(
                resizedBitmap,
                (outputBitmapSize - resizedBitmap.width) / 2f,
                (outputBitmapSize - resizedBitmap.height) / 2f,
                null
            )
        }

        return resizedBitmapWithBg
    }
    fun setMaxValue(maxValue: Int) {
        maxValueTimeInMil = maxValue
    }


    private var startRecordingOffset = 0f
    private var endRecordingOffset = 0f
    private var startRecordingTime = 0L
    fun startRecording() {
        if (checkEnd(getCurrentTime().roundToInt())) {
            onStartFailHandle?.invoke()
            return
        }
        isRecord = true
        startRecordingOffset = getCurrentTime()
        startRecordingTime = System.currentTimeMillis()
    }

    fun stopRecording(outPath: String) {
        isRecord = false
        endRecordingOffset = getCurrentTime()
        val recordedInfo = RecordedInfo(
            outPath,
            startRecordingOffset.roundToInt(),
            endRecordingOffset.roundToInt()
        )
        recordedInfoList.add(recordedInfo)
        startRecordingOffset = 0f
        endRecordingOffset = 0f
        onStropSuccessHandle?.invoke(recordedInfo)
        invalidate()
    }

    fun drawAndMove() {
        if (!isRecord) return
        val deltaTime = 40
        startRecordingTime = System.currentTimeMillis()
        val distance = (deltaTime.toFloat() * sizeOfTimeline / maxValueTimeInMil)
        deltaX -= distance


        endRecordingOffset = getCurrentTime()
        if (checkEnd(endRecordingOffset.roundToInt())) {
            onStopRecordingHandle?.invoke(getCurrentTime().roundToInt())
        }
        onMoveHandle?.invoke(getCurrentTime().roundToInt())
        invalidate()
    }


    private fun checkEnd(endOffset: Int): Boolean {
        if (getCurrentTime() >= maxValueTimeInMil - 10) return true
        for (item in recordedInfoList) {
            if (endOffset >= item.startMs - 10 && endOffset < item.endMs) return true
        }
        return false
    }

    private fun getBlackBitmap(): Bitmap {
        val size = (DimenUtils.density(context) * 56).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (i in 0 until bitmap.width)
            for (j in 0 until bitmap.height)
                bitmap.setPixel(i, j, Color.BLACK)
        return bitmap
    }

    fun deleteRecord(path: String) {
        for (item in recordedInfoList) {
            if (item.recordFilePath == path) {
                recordedInfoList.remove(item)
                invalidate()
                return
            }
        }
    }

    private fun getCurrentTime(): Float =
        ((centerPoint - deltaX) * maxValueTimeInMil / sizeOfTimeline)

    fun moveTo(timeMs: Int) {
        deltaX = centerPoint - (timeMs * sizeOfTimeline / maxValueTimeInMil)
        invalidate()
    }

    fun setDataList(dataList: ArrayList<RecordedModel>) {
        recordedInfoList.clear()
        for (item in dataList) {
            recordedInfoList.add(RecordedInfo(item.path, item.startOffset, item.endOffset))
        }
        invalidate()
    }

}