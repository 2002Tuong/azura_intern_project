package com.slideshowmaker.slideshow.modules.slide_show_package_2.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.slideshowmaker.slideshow.utils.BitmapHelper
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

class SlideShow(private val mImagePathList: ArrayList<String>):Serializable {

    private val slides = arrayListOf<Slide>()

    private var FPS = 30
    private var timerPerSlide = 5000

    private var delayTimeInMills = 3000
    val delayTimeInSec get() = delayTimeInMills/1000

    private var transitionTime = 2000
    private var numberFramePerSlide = FPS * timerPerSlide

    private var _totalFrame = 0
    val totalFrame get() = _totalFrame

    private var totalTimeMiniSec = 0

    private var numberSlide = 0

    private var currentSlide: SlideRenderData
    private var nextSlide: SlideRenderData
    private var backupSlide: SlideRenderData

    private var currentSlideIndex = 0

    private val blackBitmap:Bitmap

    private val _bitmapHashMap = HashMap<String, Bitmap>()

    private val _bitmapHashMapV2 = HashMap<String, String>()
    val bitmapHashMap get() = _bitmapHashMapV2

    var isReady = false

    init {

        blackBitmap = BitmapHelper.getBlackBitmap()
        initSlideData(mImagePathList)

       // mCurrentSlide = SlideRenderData(mSlides[0], drawResizedBitmap(mSlides[0].imagePath))
        currentSlide = SlideRenderData(slides[0], getBitmapFromHashMapV2(slides[0].imagePath))
       // mNextSlide = SlideRenderData(mSlides[1], drawResizedBitmap(mSlides[1].imagePath))
        nextSlide = SlideRenderData(slides[1], getBitmapFromHashMapV2(slides[1].imagePath))
        currentSlideIndex = 2
        if (slides.size > 2) {
            //mBackupSlide = SlideRenderData(mSlides[2], drawResizedBitmap(mSlides[2].imagePath))
            backupSlide = SlideRenderData(slides[2], getBitmapFromHashMapV2(slides[2].imagePath))
        } else {
            backupSlide = getBlackSlide()
        }
        isReady = true
    }

    fun updateTime(delayTime:Int) :Boolean{
        if(delayTime == this.delayTimeInMills) return false
        this.delayTimeInMills = delayTime*1000
        timerPerSlide= this.delayTimeInMills +transitionTime
        numberFramePerSlide = FPS * timerPerSlide/1000
        initSlideData(mImagePathList)
        isReady = false
      return true
    }

    private fun initSlideData(imagesPath: ArrayList<String>) {
        slides.clear()
        for (item in imagesPath) {
            slides.add(Slide(View.generateViewId(), item))
        }
        numberSlide = imagesPath.size
        _totalFrame = numberFramePerSlide*numberSlide
        totalTimeMiniSec = numberSlide*timerPerSlide
    }


    fun getFrameByVideoTime(timeMiniSec:Int): FrameData? {

        if(timeMiniSec >= totalTimeMiniSec) {
            return null
        }

        var time = timeMiniSec
        if(timeMiniSec > totalTimeMiniSec) time = totalTimeMiniSec

        val targetSlideIndex = time/(timerPerSlide)

        val surplus = time%timerPerSlide
        val progress:Float
        if(surplus < delayTimeInMills) {
            progress = 0f
        } else {
            progress = (surplus - delayTimeInMills)/transitionTime.toFloat()
        }

        val zoom = 1f

        val targetSlide = slides[targetSlideIndex]
        if(currentSlide.slide.slideId != targetSlide.slideId) {
            currentSlide = nextSlide
            nextSlide = backupSlide
            updateBackupSlide()
        }
        return FrameData(currentSlide.imageBitmap, nextSlide.imageBitmap, progress, currentSlide.slide.slideId, zoom)
    }

    fun getFrameByVideoTimeForRender(timeMiniSec:Int): FrameData? {

        if(timeMiniSec >= totalTimeMiniSec) {
            return null
        }

        var time = timeMiniSec
        if(timeMiniSec > totalTimeMiniSec) time = totalTimeMiniSec

        val surplus = time%timerPerSlide
        val progress:Float
        if(surplus < delayTimeInMills) {
            progress = 0f
        } else {
            progress = (surplus - delayTimeInMills)/transitionTime.toFloat()
        }

        val zoom = 1f

        var targetSlideIndex = (timeMiniSec-1)/(timerPerSlide)
        if(targetSlideIndex <= 0) targetSlideIndex = 0
        else if(targetSlideIndex >= slides.size-1) targetSlideIndex = slides.size-1
        currentSlide = SlideRenderData(slides[targetSlideIndex], getBitmapFromHashMapV2(slides[targetSlideIndex].imagePath))
        if(targetSlideIndex < (slides.size - 1)) {
            nextSlide = SlideRenderData(slides[targetSlideIndex+1], getBitmapFromHashMapV2(slides[targetSlideIndex+1].imagePath))
        } else {
            nextSlide = getBlackSlide()
        }
        return FrameData(currentSlide.imageBitmap, nextSlide.imageBitmap, progress, currentSlide.slide.slideId, zoom)
    }

    fun getFrameDataByNumberFrame(numberFrame: Int): FrameData? {
        if (numberFrame > _totalFrame) return null
        val targetSlideIndex = numberFrame / numberFramePerSlide
        val surplus = numberFrame % (numberFramePerSlide)
        val progress: Float
        if (surplus < delayTimeInMills * FPS) {
            progress = 0f
        } else {
            progress = ((surplus - (delayTimeInMills * FPS)).toFloat() / (transitionTime * FPS))
        }
        val zoomProgress: Float

        val j = numberFrame % (10 * FPS * 4)
        zoomProgress = 0.9f + 0.1f * (abs(sin(PI * 2 * j.toFloat() / (5 * 32 * 4).toFloat()))).toFloat()


        val targetSlide = slides[targetSlideIndex]
        if (targetSlide.imagePath != currentSlide.slide.imagePath) {
            currentSlide = nextSlide
            nextSlide = backupSlide
            currentSlideIndex++
            updateBackupSlide()
            return FrameData(
                currentSlide.imageBitmap,
                nextSlide.imageBitmap,
                progress,
                currentSlide.slide.slideId,
                zoomProgress
            )
        } else {
            return FrameData(
                currentSlide.imageBitmap,
                nextSlide.imageBitmap,
                progress,
                currentSlide.slide.slideId,
                zoomProgress
            )
        }
    }

    fun repeat() {
        isReady = false
        currentSlide = SlideRenderData(slides[0], getBitmapFromHashMapV2(slides[0].imagePath))
        nextSlide = SlideRenderData(slides[1], getBitmapFromHashMapV2(slides[1].imagePath))
        currentSlideIndex = 2
        if (slides.size > 2) {
            backupSlide = SlideRenderData(slides[2], getBitmapFromHashMapV2(slides[2].imagePath))
        } else {
            backupSlide = getBlackSlide()
        }

        isReady = true
    }

    private fun updateBackupSlide() {
        Thread{
            if(nextSlide.slide.imagePath == "none") {
               // mBackupSlide = SlideRenderData(mSlides[0], drawResizedBitmap(mSlides[0].imagePath))
                backupSlide = SlideRenderData(slides[0], getBitmapFromHashMapV2(slides[0].imagePath))
            } else {
                for(index in 0..slides.size) {
                    val item = slides[index]
                    if(item.slideId == nextSlide.slide.slideId) {
                        if(index == slides.size-1) {
                            backupSlide = getBlackSlide()
                        } else {
                            //mBackupSlide = SlideRenderData(mSlides[index+1], drawResizedBitmap(mSlides[index+1].imagePath))
                            backupSlide = SlideRenderData(slides[index+1], getBitmapFromHashMapV2(slides[index+1].imagePath))
                        }
                        break
                    }
                }
            }

        }.start()
    }

    fun seekTo(timeMilSec:Int, onComplete:()->Unit) {

        Observable.fromCallable<String> {
            var targetSlideIndex = (timeMilSec-1)/(timerPerSlide)
            if(targetSlideIndex <= 0) targetSlideIndex = 0
             else if(targetSlideIndex >= slides.size-1) targetSlideIndex = slides.size-1
            Logger.e("target slide = $targetSlideIndex")
            currentSlide = SlideRenderData(slides[targetSlideIndex], getBitmapFromHashMapV2(slides[targetSlideIndex].imagePath))
            if(targetSlideIndex < (slides.size - 1)) {
                nextSlide = SlideRenderData(slides[targetSlideIndex+1], getBitmapFromHashMapV2(slides[targetSlideIndex+1].imagePath))
            } else {
                nextSlide = getBlackSlide()
            }
            return@fromCallable ""
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onNext(t: String) {

                }

                override fun onComplete() {
                    updateBackupSlide()
                    onComplete.invoke()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }

    private fun drawResizedBitmap(imagePath: String): Bitmap {
        val screenW = 1080
        val rawBitmap = BitmapHelper.getBitmapFromFilePath(imagePath)
        val outBitmapSize: Int
        if (rawBitmap.width < screenW && rawBitmap.height < screenW) {
            outBitmapSize = max(rawBitmap.width, rawBitmap.height)
        } else {
            outBitmapSize = screenW
        }

        val blurBgBitmap = BitmapHelper.blurBitmapV2(
            BitmapHelper.resizeMatchBitmap(
                rawBitmap,
                outBitmapSize.toFloat() + 100
            ), 20
        )

        val rawBitmapResized = BitmapHelper.resizeWrapBitmap(rawBitmap, outBitmapSize.toFloat())

        val resizedBitmapWithBg =
            Bitmap.createBitmap(outBitmapSize, outBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            blurBgBitmap?.let {
                drawBitmap(
                    it,
                    (outBitmapSize - it.width) / 2f,
                    (outBitmapSize - it.height) / 2f,
                    null
                )
            }
            drawBitmap(
                rawBitmapResized,
                (outBitmapSize - rawBitmapResized.width) / 2f,
                (outBitmapSize - rawBitmapResized.height) / 2f,
                null
            )
        }

        return resizedBitmapWithBg
    }

    private fun getBitmapFromHashMap(imagePath: String):Bitmap {
        if(_bitmapHashMap[imagePath] == null) {
            val bitmap = drawResizedBitmap(imagePath)
            _bitmapHashMap[imagePath] = bitmap
            return bitmap
        } else {
            return _bitmapHashMap[imagePath]!!
        }
    }

    private fun getBitmapFromHashMapV2(imagePath: String):Bitmap {
        if(_bitmapHashMapV2[imagePath] == null) {
           val bitmap = drawResizedBitmap(imagePath)
            val outFilePath = FileHelper.saveBitmapToTempData(bitmap)
            _bitmapHashMapV2[imagePath] = outFilePath
            return bitmap
        } else {
            return BitmapHelper.getBitmapFromFilePath(_bitmapHashMapV2[imagePath]!!)
        }
    }

    fun getTotalDuration():Int = totalTimeMiniSec

    private fun getBlackSlide() : SlideRenderData {
        return SlideRenderData(Slide(View.generateViewId(), "none"), blackBitmap)
    }

    fun getImagePathList():ArrayList<String> {
        return mImagePathList
    }

}