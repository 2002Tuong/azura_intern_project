package com.slideshowmaker.slideshow.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class CropVideoTimeView :View{
    private val mImagePreviewPathList = ArrayList<String>()

    private var density = 0f

    private var managerHeight = 56f

    private var managerWidth = 14f
    private var managerRadius = 5f

    private var lineHeight = 2f

    private var topOffset = 24f
    private var textSize = 12f

    private val highlightPaint = Paint()
    private val highlightColor = Color.parseColor("#FB7609")

    private val grayPaint = Paint()
    private val grayColor = Color.parseColor("#66000000")

    private val textPaint = Paint()

    private val leftRegionManager = Region()
    private val rightRegionManager = Region()

    private var leftDeltaX = 0f
    private var rightDeltaY = 100f

    private var startProgressValue = 0f
    private var endProgressValue = 100f

    private var swipeLeft = false
    private var swipeRight = false

    private var imgBackground:Bitmap? = null

    private val bitmapHashMap = HashMap<String, Bitmap>()

    private var maximumValue = 0

    var onChangeListener: OnChangeListener? = null

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attributes: AttributeSet?) {
        density = DimenUtils.density(context)

        managerHeight*=density
        managerWidth*=density
        lineHeight*=density
        managerRadius*=density
        topOffset*=density
        textSize*=density
        highlightPaint.apply {
            isAntiAlias =true
            style = Paint.Style.FILL
            color = highlightColor
        }

        grayPaint.apply {
            isAntiAlias =true
            style = Paint.Style.FILL
            color = grayColor
        }

        textPaint.apply {
            color = Color.parseColor("#080F1C")
            isAntiAlias =true
            style = Paint.Style.FILL
            textSize = textSize
        }

    }


    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)

        rightDeltaY = width - managerWidth
    }

/*    fun setImagePathList(imagePathList:ArrayList<String>) {
        mImagePreviewPathList.clear()
        mImagePreviewPathList.addAll(imagePathList)
    }*/

     fun loadImage(imagePathList:ArrayList<String>) {
        Logger.e("crop w = $width")
        Observable.fromCallable<String> {
            val bitmap = Bitmap.createBitmap((width-2*managerWidth).toInt(), (DimenUtils.density(context)*56).toInt(), Bitmap.Config.ARGB_8888)
            for(i in 0 until bitmap.width)
                for(j in 0 until bitmap.height) {
                    bitmap.setPixel(i,j,Color.WHITE)
                }
            imgBackground = bitmap
            val canvas = Canvas(imgBackground!!)
            var deltaX = 0f
            canvas.apply {
                while (deltaX < width) {
                    val bitmap = getBitmap(imagePathList[(deltaX*imagePathList.size/width).toInt()])
                    drawBitmap(bitmap,deltaX,0f,null)
                    deltaX += density*56
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

    fun loadImage(videoPath:String, width:Int) {
        setMax(MediaHelper.getVideoDuration(videoPath))
        Observable.fromCallable<String> {
            val bitmap = Bitmap.createBitmap((width-2*managerWidth).toInt(), (DimenUtils.density(context)*56).toInt(), Bitmap.Config.ARGB_8888)
            for(i in 0 until bitmap.width)
                for(j in 0 until bitmap.height) {
                    bitmap.setPixel(i,j,Color.WHITE)
                }
            imgBackground = bitmap
            val canvas = Canvas(imgBackground!!)

            var deltaX = 0f
            val metadataRetriever = MediaMetadataRetriever()
            metadataRetriever.setDataSource(videoPath)

            canvas.apply {
                while (deltaX < width) {
                    Logger.e("time = ${(maximumValue*deltaX/width)}")
                    val extractedImage = metadataRetriever.getFrameAtTime((maximumValue*1000*deltaX/width).roundToLong(), MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    extractedImage?.also { drawBitmap(drawResizedBitmap(extractedImage),deltaX,0f,null) }
                    deltaX += density*56
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

    fun loadVideoImagePreview(videoPathList:ArrayList<String>, width: Int) {

        Observable.fromCallable<String> {
            val bitmap = Bitmap.createBitmap((width-2*managerWidth).toInt(), (DimenUtils.density(context)*56).toInt(), Bitmap.Config.ARGB_8888)
            for(i in 0 until bitmap.width)
                for(j in 0 until bitmap.height) {
                    bitmap.setPixel(i,j,Color.WHITE)
                }
            imgBackground = bitmap
            val canvas = Canvas(imgBackground!!)

            var deltaX = 0f
            val totalImage = (width/(DimenUtils.density(VideoMakerApplication.getContext())*56)).toInt()
            Logger.e("number image = $totalImage")
            for(index in 0 until totalImage) {
                val targetTimeInMillis = maximumValue*index/totalImage
                var currentTotalDuration = 0
                for(videoPath in videoPathList) {
                    val videoDuration = MediaHelper.getVideoDuration(videoPath)
                    if(currentTotalDuration+videoDuration > targetTimeInMillis) {

                        canvas.apply {
                            val mediaMetadataRetriever = MediaMetadataRetriever()
                            mediaMetadataRetriever.setDataSource(videoPath)
                            val extractedImage = mediaMetadataRetriever.getFrameAtTime((targetTimeInMillis-currentTotalDuration).toLong()*1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            extractedImage?.also { drawBitmap(drawResizedBitmap(extractedImage),deltaX,0f,null) }
                            deltaX+=(DimenUtils.density(VideoMakerApplication.getContext())*56)
                        }
                        break
                    } else {
                        currentTotalDuration+=videoDuration
                    }
                }
            }
            canvas.apply {
                val videoPath = videoPathList[videoPathList.size-1]
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(videoPath)
                val extractedBitmap = mediaMetadataRetriever.getFrameAtTime((MediaHelper.getVideoDuration(videoPath)).toLong()*1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                extractedBitmap?.also { drawBitmap(drawResizedBitmap(extractedBitmap),deltaX,0f,null) }
                deltaX+=(DimenUtils.density(VideoMakerApplication.getContext())*56)
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

    private fun getBitmap(path:String):Bitmap {
        return if(bitmapHashMap[path] == null) {
            val bitmap = drawResizedBitmap(path)
            bitmapHashMap[path] = bitmap
            bitmap
        } else {
            bitmapHashMap[path]!!
        }
    }
    override fun onDraw(canvas: Canvas) {
        drawImageBg(canvas)
        drawLeftGrayArea(canvas)
        drawRightGrayArea(canvas)
        drawTopAndBottomLine(canvas)
        drawLeftControl(canvas)
        drawRightControl(canvas)
        drawTimeText(canvas)
    }

    private fun drawImageBg(canvas: Canvas?) {
        imgBackground?.let {
            val bitmap = it
            canvas?.drawBitmap(bitmap,managerWidth,topOffset, null)
        }
    }

    private fun drawLeftGrayArea(canvas: Canvas?) {
        val topPos = topOffset
        val leftDeltaX = (startProgressValue*(width-managerWidth))/100
        rightDeltaY = (endProgressValue/100)*(width-managerWidth)
        canvas?.drawRect(managerWidth,topPos, leftDeltaX+managerWidth/2f, managerHeight+topPos, grayPaint)
    }

    private fun drawRightGrayArea(canvas: Canvas?) {
        val topPos = topOffset
        val rightDeltaX = (endProgressValue*(width-managerWidth))/100
        canvas?.drawRect(rightDeltaX, 0f+topPos, width-managerWidth, managerHeight+topPos, grayPaint)
    }

    private fun drawLeftControl(canvas: Canvas?) {
        canvas?.drawPath(getControlLeftPath().apply {
            val topPos = topOffset
            val leftDeltax = startProgressValue*(width-managerWidth)/100
            offset(leftDeltax, topPos)
            val boundRecF = RectF()
            computeBounds(boundRecF, true)
            leftRegionManager.setPath(this, Region(boundRecF.left.toInt(), boundRecF.top.toInt(), boundRecF.right.toInt(), boundRecF.bottom.toInt()))
        }, highlightPaint)
    }

    private fun drawRightControl(canvas: Canvas?) {
        canvas?.drawPath(getControlRightPath().apply {
            val topPos = topOffset
            val rightDeltaX = endProgressValue*(width-managerWidth)/100
            offset(rightDeltaX,topPos)
            val boundRecF = RectF()
            computeBounds(boundRecF, true)
            rightRegionManager.setPath(this, Region(boundRecF.left.toInt(), boundRecF.top.toInt(), boundRecF.right.toInt(), boundRecF.bottom.toInt()))
        }, highlightPaint)
    }

    private fun drawTopAndBottomLine(canvas: Canvas?) {
        val leftOffset = startProgressValue/100*(width-managerWidth)+managerWidth/2f
        val rightOffset = endProgressValue/100*(width-managerWidth)+managerWidth/2f
        val topPos = topOffset
        canvas?.drawRect(leftOffset,topPos,rightOffset, lineHeight+topPos, highlightPaint)
        canvas?.drawRect(leftOffset,height-lineHeight,rightOffset, height.toFloat(), highlightPaint)
    }

    private fun drawTimeText(canvas: Canvas?) {
        val startValue = maximumValue*startProgressValue/100
        val endValue = maximumValue*endProgressValue/100
        val startText = Utils.convertSecToTimeString((startValue/1000).roundToInt())
        val endText = Utils.convertSecToTimeString((endValue/1000).roundToInt())

        canvas?.drawText(startText, 0f,getTextHeight(startText,textPaint)+4*density, textPaint)
        canvas?.drawText(endText, width-getTextWidth(endText, textPaint)-10f,getTextHeight(startText,textPaint)+4*density, textPaint)
    }

    private fun getControlLeftPath():Path{
        val rectF = RectF()
        return Path().apply {

            moveTo(managerWidth, 0f)
            lineTo(managerWidth, managerHeight)
            lineTo(managerRadius, managerHeight)
            lineTo(managerRadius, managerHeight-managerRadius)
            lineTo(0f,managerHeight-managerRadius)
            lineTo(0f,managerRadius)
            lineTo(managerRadius, managerRadius)
            lineTo(managerRadius, 0f)
            lineTo(managerWidth, 0f)

            moveTo(managerRadius, managerRadius)
            rectF.set(0f,0f, 2*managerRadius, 2*managerRadius)
            arcTo(rectF, -90f,-90f)

            moveTo(managerWidth-managerRadius, managerHeight-managerRadius)
            rectF.set(0f,managerHeight-2*managerRadius, 2*managerRadius, managerHeight)
            arcTo(rectF, 90f,90f)

            close()
        }
    }

    private fun getControlRightPath():Path {
        val controlPath = getControlLeftPath()
        val transMatrix = Matrix().apply {
            postRotate(180f,managerWidth/2f, managerHeight/2f)
        }
        controlPath.apply {
            transform(transMatrix)

        }

        return controlPath
    }



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN) {
            if(leftRegionManager.contains(event!!.x.toInt(), event!!.y.toInt())) {
                swipeLeft = true
                swipeRight = false
            } else if(rightRegionManager.contains(event!!.x.toInt(), event!!.y.toInt())) {
                swipeLeft = false
                swipeRight = true
            } else {
                swipeLeft = false
                swipeRight = false
            }
        } else if(event?.action == MotionEvent.ACTION_UP) {
            if(swipeLeft) onChangeListener?.onUpLeft(startProgressValue*maximumValue/100)
            else if(swipeRight)onChangeListener?.onUpRight(endProgressValue*maximumValue/100)
            swipeLeft = false
            swipeRight = false
        }

        if(swipeLeft) {
            if(event?.action == MotionEvent.ACTION_MOVE)
            onSwipeLeft(event.rawX)

        } else if(swipeRight) {
            if(event?.action == MotionEvent.ACTION_MOVE)
                onSwipeRight(event.rawX)
        }

        return true
    }

    private fun onSwipeLeft(rawX:Float) {
        var deltaX = rawX-x
        if(deltaX <= 0f) deltaX = 0f
        else if(deltaX >= rightDeltaY-managerWidth)deltaX = rightDeltaY-managerWidth
        if(leftDeltaX!=deltaX) {
            leftDeltaX = deltaX
            startProgressValue = leftDeltaX*100/(width-managerWidth)
            onChangeListener?.onSwipeLeft(startProgressValue*maximumValue/100f)
            invalidate()
        }
    }

    private fun onSwipeRight(rawX:Float) {
        var deltaX = rawX-x
        if(deltaX>=width-managerWidth) deltaX = width-managerWidth
        else if(deltaX <= leftDeltaX+managerWidth) deltaX = leftDeltaX+managerWidth
        if(rightDeltaY != deltaX) {
            rightDeltaY = deltaX
            endProgressValue = rightDeltaY*100/(width-managerWidth)
            onChangeListener?.onSwipeRight(endProgressValue*maximumValue/100f)
            invalidate()
        }
    }

    private fun drawResizedBitmap(imagePath: String): Bitmap {
        val bytesSize = (DimenUtils.density(context)*56).toInt()
        val bitmap = BitmapHelper.getBitmapFromFilePath(imagePath)
        val outputBitmapSize: Int
        outputBitmapSize = if (bitmap.width < bytesSize && bitmap.height < bytesSize) {
            max(bitmap.width, bitmap.height)
        } else {
            bytesSize
        }

        var rawBitmapResized:Bitmap? = BitmapHelper.resizeMatchBitmap(bitmap, outputBitmapSize.toFloat())

        val resizedBitmapWithBg = Bitmap.createBitmap(outputBitmapSize, outputBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            drawBitmap(
                rawBitmapResized!!,
                (outputBitmapSize - rawBitmapResized!!.width) / 2f,
                (outputBitmapSize - rawBitmapResized!!.height) / 2f,
                null
            )
        }
        rawBitmapResized = null
        return resizedBitmapWithBg
    }

    private fun drawResizedBitmap(bitmap: Bitmap): Bitmap {
        val bytesSize = (DimenUtils.density(context)*56).toInt()
        val inputBitmap = bitmap
        val outputBitmapSize: Int
        outputBitmapSize = if (inputBitmap.width < bytesSize && inputBitmap.height < bytesSize) {
            max(inputBitmap.width, inputBitmap.height)
        } else {
            bytesSize
        }

        var rawBitmapResized:Bitmap? = BitmapHelper.resizeMatchBitmap(inputBitmap, outputBitmapSize.toFloat())

        val resizedBitmapWithBg = Bitmap.createBitmap(outputBitmapSize, outputBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            drawBitmap(
                rawBitmapResized!!,
                (outputBitmapSize - rawBitmapResized!!.width) / 2f,
                (outputBitmapSize - rawBitmapResized!!.height) / 2f,
                null
            )
        }
        rawBitmapResized = null
        return resizedBitmapWithBg
    }

    fun setMax(timeMSec:Int) {
        maximumValue= timeMSec
        invalidate()
    }

    fun setStartAndEnd(startTimeMilSec:Int, endTimeSec:Int) {
        startProgressValue = startTimeMilSec*100f/maximumValue
        endProgressValue = endTimeSec*100f/maximumValue


        if(endProgressValue <= 0f) endProgressValue = 0f
        else if(endProgressValue >= 100f) endProgressValue = 100f

        if(startProgressValue <= 0f) startProgressValue = 0f
        else if(startProgressValue >= 100f) startProgressValue = 90f

        invalidate()
    }

    private fun getTextWidth(text:String, paint: Paint):Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }
    private fun getTextHeight(text:String, paint: Paint):Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    interface OnChangeListener {
        fun onSwipeLeft(startTimeMilSec:Float)
        fun onUpLeft(startTimeMilSec: Float)
        fun onSwipeRight(endTimeMilSec:Float)
        fun onUpRight(endTimeMilSec:Float)
    }

}