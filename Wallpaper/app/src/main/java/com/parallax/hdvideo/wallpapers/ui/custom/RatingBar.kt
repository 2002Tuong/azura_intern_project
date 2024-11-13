package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.dpToPx

class RatingBar: LinearLayout {

    private val listRateView = mutableListOf<ImageView>()
    private val countRate = 5
    private var oldRate = 0
    private var heightOfStar = dpToPx(32f)
    private var widthOfStar = dpToPx(33f)
    private var marginOfStar = dpToPx(20f)
    private var firstX = 0f
    private var lastX = 0f

    var onClickedStarCallback: ((index: Int) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr) {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val ratingValue = attrs?.let {
            val array = context.obtainStyledAttributes(it, R.styleable.RatingBar)
            val r = array.getInt(R.styleable.RatingBar_ratting, countRate)
            widthOfStar = array.getDimensionPixelOffset(R.styleable.RatingBar_widthStar, widthOfStar)
            heightOfStar = array.getDimensionPixelOffset(R.styleable.RatingBar_heightStar, heightOfStar)
            marginOfStar = array.getDimensionPixelOffset(R.styleable.RatingBar_marginStar, marginOfStar)
            array.recycle()
            r
        } ?: countRate
        (0 until countRate).forEach{
            val view = ImageView(context)
            view.setImageResource(R.drawable.ic_star_stroke_re)
            val params = LayoutParams(widthOfStar, heightOfStar)
            if (it < countRate - 1) {
                params.marginEnd = marginOfStar
            }
            addView(view, params)
            view.tag = it
            listRateView.add(view)
        }
        setRatingStar(ratingValue)
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(MeasureSpec.makeMeasureSpec((widthStar + marginStar) * countRating , MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightStar, MeasureSpec.EXACTLY))
//    }

    var rating: Int
        get() = oldRate
        set(value) {
            if (value >= 0 && value != oldRate) {
                oldRate = value
                setRatingStar(value)
                onClickedStarCallback?.invoke(value)
            }
        }

    private fun setRatingStar(value: Int) {
        (value until countRate).forEach { listRateView[it].setImageResource(R.drawable.ic_star_stroke_re) }
        (0 until value).forEach { listRateView[it].setImageResource(R.drawable.ic_star_rating_re) }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                firstX = listRateView[0].x - marginOfStar / 2
                lastX = listRateView[countRate - 1].x + marginOfStar / 2
                true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                rating = calculateRating(event)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun calculateRating(event: MotionEvent) : Int {
        val x = event.x
        if (x < firstX) return 1
        if (x >= lastX) return countRate
        var fromX = firstX
        for (i in 0 until countRate) {
            val toX = fromX + widthOfStar + marginOfStar
            if (x >= fromX && x < toX) {
                return i + 1
            }
            fromX = toX
        }
        return 0
    }

}