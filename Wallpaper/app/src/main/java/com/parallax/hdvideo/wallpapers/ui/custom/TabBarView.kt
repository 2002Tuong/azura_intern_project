package com.parallax.hdvideo.wallpapers.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.math.MathUtils
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.dp2Px
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.spToPx
import kotlin.math.min

class TabBarView: View {

    private var typefaceIsSelected: Typeface? = null
    private var curTypeface: Typeface? = null
    private val listTabBars = mutableListOf<TabBar>()

    private val heightOfItem = dpToPx(40f)
    private val radiusOfItem = dp2Px(20f)
    private var heightOfText: Int = 0
    private var marginOfItem: Int = dpToPx(5f)
    private var backgroundViewWidth = 0f
    private var backgroundViewPositionX = 0f
    private var currentPos = 0
    private var animator: ValueAnimator? = null
    private var viewPager: ViewPager? = null
    var tabPositionDidSelect: ((tabBar: TabBar, position: Int) -> Unit)? = null
    private var drawLabelComplete = true
    private var bg = Color.parseColor("#0094ff")
    private var isTouchingColor = false

    private var marginLeftRight: Int = dpToPx(16f)

    private val textPaint: TextPaint by lazy {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#222222")
        paint.textSize = spToPx(16f)
        paint.typeface = curTypeface
        paint
    }

    private val BGPaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#ffe656")
        paint
    }

    private val maskPaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#222222")
        paint
    }

    private val XfermodePaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#0094ff")
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        paint
    }
    private val touchPaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(251, 245, 227)
        paint
    }
    private var tabIsTouching: TabBar? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup(context)
    }

    private fun setup(context: Context) {
        typefaceIsSelected = ResourcesCompat.getFont(context, R.font.roboto_bold)
        curTypeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        val rectText = Rect()
        textPaint.getTextBounds("g", 0, 1, rectText)
        heightOfText = rectText.height()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tabBarSize = listTabBars.size
        if (tabBarSize > 0) {
            var widthValue = marginLeftRight * 1f
            val y = paddingTop.toFloat()
            listTabBars.forEachIndexed { index, tabBar ->
                tabBar.x = widthValue + marginOfItem * index
                tabBar.width =
                    (Resources.getSystem().displayMetrics.widthPixels - marginLeftRight * tabBarSize) / tabBarSize * 1f
                widthValue += tabBar.width
                tabBar.y = y
                tabBar.height = heightOfItem.toFloat()
            }
            val frameWidthValue = (widthValue + marginOfItem * (listTabBars.size - 1)).toInt()
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val height =
                if (heightMode == MeasureSpec.EXACTLY) heightSize else heightOfItem + paddingTop + paddingBottom
            selectedTab(listTabBars[currentPos])
            setMeasuredDimension(frameWidthValue + paddingRight, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val canvas = canvas ?: return
        if (listTabBars.isNotEmpty()) {
            tabIsTouching?.also {
                canvas.drawRoundRect(
                    RectF(it.x, it.y, it.x + it.width, it.y + heightOfItem),
                    radiusOfItem,
                    radiusOfItem,
                    touchPaint
                )
            }
            val bitmapText = Bitmap.createBitmap(
                width - paddingLeft - paddingRight,
                heightOfItem,
                Bitmap.Config.ARGB_8888
            )
            val canvas2 = Canvas(bitmapText)
            val textY = (heightOfItem + heightOfText) / 2f
            listTabBars.forEachIndexed { index, tabBar ->
                textPaint.typeface = if (index == currentPos) typefaceIsSelected else curTypeface
                val widthText = StaticLayout.getDesiredWidth(tabBar.title, textPaint)
                val posXOfText = tabBar.x + (tabBar.width - widthText) / 2
                canvas2.drawText(tabBar.title, posXOfText, textY, textPaint)
            }
            canvas.drawBitmap(bitmapText, paddingLeft.toFloat(), paddingTop.toFloat(), null)
            drawBackground(
                canvas,
                bitmapText,
                backgroundViewWidth,
                backgroundViewPositionX,
                paddingTop.toFloat()
            )
            if (!drawLabelComplete) {
                drawLabelComplete = true
                tabPositionDidSelect?.invoke(listTabBars[currentPos], currentPos)
            }
        }

    }

    private fun drawBackground(
        canvas: Canvas,
        bitmapText: Bitmap,
        width: Float,
        x: Float,
        y: Float
    ) {
        val rect = RectF(x, y, x + width, y + heightOfItem)
        canvas.drawRoundRect(rect, radiusOfItem, radiusOfItem, BGPaint)

        val bitmapOut =
            Bitmap.createBitmap(bitmapText.width, bitmapText.height, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(bitmapOut)

        canvas2.drawBitmap(bitmapText, 0f, 0f, null)

        val bitmapMask = Bitmap.createBitmap(width.toInt(), heightOfItem, Bitmap.Config.ARGB_8888)
        val canvasMask = Canvas(bitmapMask)

        canvasMask.drawRoundRect(
            0f,
            0f,
            rect.width(),
            rect.height(),
            radiusOfItem,
            radiusOfItem,
            maskPaint
        )
        canvas2.drawBitmap(bitmapMask, x, 0f, XfermodePaint)

        canvas.drawBitmap(bitmapOut, paddingLeft.toFloat(), paddingTop.toFloat(), null)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                for ((index, it) in listTabBars.withIndex()) {
                    if (index != currentPos && it.rect.contains(x, y) && isTouchingColor) {
                        tabIsTouching = it
                        break
                    }
                }
                if (tabIsTouching != null) invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                tabIsTouching = null
                val x = event.x
                val y = event.y
                for ((index, it) in listTabBars.withIndex()) {
                    if (index != currentPos && it.rect.contains(x, y)) {
                        currentPos = index
                        viewPager?.currentItem = currentPos
                        startAnimation(listTabBars[currentPos], it)
                        break
                    }
                }
                invalidate()
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                tabIsTouching = null
                invalidate()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun startAnimation(fromTab: TabBar, toTab: TabBar) {
        animator?.cancel()
        viewPager?.removeOnPageChangeListener(pageChangeListener)
        val valueAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(200L)
        this.animator = valueAnimator
        valueAnimator.interpolator = LinearInterpolator()
        val deltaWidth = toTab.width - fromTab.width
        val deltaX = toTab.x - fromTab.x
        valueAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            backgroundViewWidth = fromTab.width + deltaWidth * progress
            backgroundViewPositionX = fromTab.x + deltaX * progress
            invalidate()
        }
        valueAnimator.doOnEnd {
            viewPager?.addOnPageChangeListener(pageChangeListener)
            tabPositionDidSelect?.invoke(listTabBars[currentPos], currentPos)
        }
        valueAnimator.start()
    }

    private fun selectedTab(tabBar: TabBar) {
        backgroundViewWidth = tabBar.width
        backgroundViewPositionX = tabBar.x
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager
        val adapter = viewPager.adapter ?: return
        currentPos = viewPager.currentItem
        for (i in 0 until adapter.count) {
            listTabBars.add(TabBar(adapter.getPageTitle(i)?.toString() ?: ""))
        }
        viewPager.removeOnPageChangeListener(pageChangeListener)
        viewPager.addOnPageChangeListener(pageChangeListener)
        requestLayout()
    }

    fun setTabBackgroundColor(color: Int) {
        bg = color
    }

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            val tabBarItem = listTabBars[position]
            val nextTabItem = listTabBars[min(position + 1, listTabBars.size - 1)]
            backgroundViewWidth = MathUtils.lerp(tabBarItem.width, nextTabItem.width, positionOffset)
            backgroundViewPositionX = MathUtils.lerp(tabBarItem.x, nextTabItem.x, positionOffset)
            invalidate()
        }

        override fun onPageSelected(position: Int) {
            currentPos = position
            val item = listTabBars[position]
            if (item.height > 0f) {
                tabPositionDidSelect?.invoke(item, position)
            } else drawLabelComplete = false
            Logger.d("onPageSelected", position)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewPager?.removeOnPageChangeListener(pageChangeListener)
        viewPager = null
        tabPositionDidSelect = null
    }

    data class TabBar(
        var title: String,
        var width: Float = 0f,
        var height: Float = 0f,
        var x: Float = 0f,
        var y: Float = 0f
    ) {

        val rect: RectF get() = RectF(x, y, x + width, y + height)
    }
}