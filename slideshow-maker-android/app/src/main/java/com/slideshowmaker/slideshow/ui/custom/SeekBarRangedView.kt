package com.slideshowmaker.slideshow.ui.custom

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.slideshowmaker.slideshow.R
import java.util.*
import kotlin.math.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SeekBarRangedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var thumbnail: Bitmap
    private lateinit var thumbnailPressed: Bitmap
    private lateinit var minVAnimator: ValueAnimator
    private lateinit var maxVAnimator: ValueAnimator

    private var activePointerId = INVALID_POINTER_ID
    private var scaledTouchSlop = 0
    private var downMotionX = 0f
    private var isDragging = false

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var bgLineRectF = RectF()
    private var progressLineRectF = RectF()
    private var pressedThumb: Thumb? = null
    private var thumbnailHalfWidth = 0f
    private var thumbnailHalfHeight = 0f
    private var thumbnailPressedHalfWidth = 0f
    private var thumbnailPressedHalfHeight = 0f
    private var paddingValue = 0f
    private var bgLineHeight = 0f
    private var progressLineHeight = 0f
    private var progressBgColor = BASE_BACKGROUND_COLOR
    private var progressColor = BASE_COLOR
    private var isRoundedCorner = false
    private var isStepProgress = false
    private val progressStepArr: MutableList<Float> = ArrayList()
    private var stepRadius = BASE_STEP_RADIUS.toFloat()

    private var normalizedMinimum = 0f
    private var normalizedMaximum = 1f

    var actionCallback: SeekBarRangedChangeCallback? = null

    var minimumVal = 0f
        private set

    var maximumVal = 0f
        private set

    var selectedMinimumVal: Float
        get() = normalizedToValue(normalizedMinimum)
        set(value) {
            setSelectedMinValue(value, false)
        }

    var selectedMaximumVal: Float
        get() = normalizedToValue(normalizedMaximum)
        set(value) {
            setSelectedMaxValue(value, false)
        }

    interface SeekBarRangedChangeCallback {
        fun onChanged(minValue: Float, maxValue: Float)
        fun onChanging(minValue: Float, maxValue: Float)
    }

    init {
        setupAttrs(context, attrs)
    }

    private fun setupAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SeekBarRangedView, 0, 0)
        val minimum: Float
        val curMinimum: Float
        val maximum: Float
        val curMaximum: Float
        val progressHeight: Int
        val bgProgressHeight: Int
        try {
            minimum = typedArray.getFloat(R.styleable.SeekBarRangedView_min, BASE_MIN_PROGRESS)
            curMinimum = typedArray.getFloat(R.styleable.SeekBarRangedView_currentMin, minimum)
            maximum = typedArray.getFloat(R.styleable.SeekBarRangedView_max, BASE_MAX_PROGRESS)
            curMaximum = typedArray.getFloat(R.styleable.SeekBarRangedView_currentMax, maximum)
            progressHeight = typedArray.getDimensionPixelSize(
                R.styleable.SeekBarRangedView_progressHeight,
                BASE_PROGRESS_HEIGHT
            )
            bgProgressHeight = typedArray.getDimensionPixelSize(
                R.styleable.SeekBarRangedView_backgroundHeight,
                BASE_PROGRESS_HEIGHT
            )
            isRoundedCorner = typedArray.getBoolean(R.styleable.SeekBarRangedView_rounded, false)
            progressColor = typedArray.getColor(R.styleable.SeekBarRangedView_progressColor, BASE_COLOR)
            progressBgColor =
                typedArray.getColor(R.styleable.SeekBarRangedView_backgroundColor, BASE_BACKGROUND_COLOR)
            if (typedArray.hasValue(R.styleable.SeekBarRangedView_thumbsResource)) {
                setThumbsImageResource(
                    typedArray.getResourceId(
                        R.styleable.SeekBarRangedView_thumbsResource,
                        R.drawable.ic_thumb_handler
                    )
                )
            } else {
                if (typedArray.hasValue(R.styleable.SeekBarRangedView_thumbNormalResource)) {
                    setThumbNormalImageResource(
                        typedArray.getResourceId(
                            R.styleable.SeekBarRangedView_thumbNormalResource,
                            R.drawable.ic_thumb_handler
                        )
                    )
                }
                if (typedArray.hasValue(R.styleable.SeekBarRangedView_thumbPressedResource)) {
                    setThumbPressedImageResource(
                        typedArray.getResourceId(
                            R.styleable.SeekBarRangedView_thumbPressedResource,
                            R.drawable.ic_thumb_handler
                        )
                    )
                }
            }
        } finally {
            typedArray.recycle()
        }
        init(minimum, curMinimum, maximum, curMaximum, progressHeight, bgProgressHeight)
    }

    private fun init(
        min: Float,
        currentMin: Float,
        max: Float,
        currentMax: Float,
        progressHeight: Int,
        bgProgressHeight: Int
    ) {
        if (::thumbnail.isInitialized.not() && ::thumbnailPressed.isInitialized.not()) {
            setThumbNormalImageResource(R.drawable.ic_thumb_handler)
            setThumbPressedImageResource(R.drawable.ic_thumb_handler)
        } else if (::thumbnail.isInitialized.not()) {
            setThumbNormalImageResource(R.drawable.ic_thumb_handler)
        } else if (::thumbnailPressed.isInitialized.not()) {
            setThumbPressedImageResource(R.drawable.ic_thumb_handler)
        }
        measureThumb()
        measureThumbPressed()
        updatePadding()
        setBackgroundHeight(bgProgressHeight.toFloat(), false)
        setProgressHeight(progressHeight.toFloat(), false)
        minimumVal = min
        maximumVal = max
        selectedMinimumVal = currentMin
        selectedMaximumVal = currentMax

        // This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScrollViews.
        isFocusable = true
        isFocusableInTouchMode = true
        if (!isInEditMode) {
            scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        }
    }

    /**
     * This method will change the min value to a desired value. Note that if Progress by Steps is enabled, min will stay as default.
     *
     * @param value new min value
     * @return true if changed
     */
    fun setMinValue(value: Float): Boolean {
        if (isStepProgress) {
            return false
        }
        minimumVal = value
        selectedMinimumVal = selectedMinimumVal
        return true
    }

    /**
     * This method will change the max value to a desired value. Note that if Progress by Steps is enabled, max will stay as default.
     *
     * @param value new max value
     * @return true if changed
     */
    fun setMaxValue(value: Float): Boolean {
        if (isStepProgress) {
            return false
        }
        maximumVal = value
        setSelectedMaxVal(selectedMaximumVal)
        return true
    }

    fun setSelectedMinValue(value: Float, animate: Boolean) {
        setSelectedMinValue(value, animate, BASE_ANIMATE_DURATION)
    }

    fun setSelectedMinValue(value: Float, animate: Boolean, duration: Long) {
        if (animate) {
            if (::minVAnimator.isInitialized) {
                minVAnimator.cancel()
            }
            minVAnimator = getAnimator(selectedMinimumVal, value, duration) { valueAnimator ->
                setSelectedMinVal(valueAnimator.animatedValue as Float)
            }
            minVAnimator.start()
        } else {
            setSelectedMinVal(value)
        }
    }

    private fun setSelectedMinVal(value: Float) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (maximumVal - minimumVal == 0f) {
            setNormalizedMinValue(0f)
        } else {
            setNormalizedMinValue(valueToNormalized(value))
        }
        onChangedValues()
    }

    fun setSelectedMaxValue(value: Float, animate: Boolean) {
        setSelectedMaxValue(value, animate, BASE_ANIMATE_DURATION)
    }

    fun setSelectedMaxValue(value: Float, animate: Boolean, duration: Long) {
        if (animate) {
            if (::maxVAnimator.isInitialized) {
                maxVAnimator.cancel()
            }
            maxVAnimator = getAnimator(selectedMaximumVal, value, duration) { valueAnimator ->
                setSelectedMaxVal(valueAnimator.animatedValue as Float)
            }
            maxVAnimator.start()
        } else {
            setSelectedMaxVal(value)
        }
    }

    private fun setSelectedMaxVal(value: Float) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (maximumVal - minimumVal == 0f) {
            setNormalizedMaxValue(1f)
        } else {
            setNormalizedMaxValue(valueToNormalized(value))
        }
        onChangedValues()
    }

    private fun getAnimator(
        current: Float,
        next: Float,
        duration: Long,
        updateListener: AnimatorUpdateListener
    ) = ValueAnimator().apply {
        this.interpolator = DecelerateInterpolator()
        this.duration = duration
        this.setObjectValues(current, next)
        this.setEvaluator(object : FloatEvaluator() {
            fun evaluate(fraction: Float, startValue: Float, endValue: Float): Int {
                return (startValue + (endValue - startValue) * fraction).roundToInt()
            }
        })
        this.addUpdateListener(updateListener)
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    private fun setNormalizedMinValue(value: Float) {
        normalizedMinimum = max(0f, min(1f, min(value, normalizedMaximum)))
        invalidate()
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private fun setNormalizedMaxValue(value: Float) {
        normalizedMaximum = max(0f, min(1f, max(value, normalizedMinimum)))
        invalidate()
    }

    fun setRounded(rounded: Boolean) {
        isRoundedCorner = rounded
        invalidate()
    }

    /**
     * Set progress bar background height
     *
     * @param height is given in pixels
     */
    fun setBackgroundHeight(height: Float) {
        setBackgroundHeight(height, true)
    }

    private fun setBackgroundHeight(height: Float, invalidate: Boolean) {
        bgLineHeight = height
        if (invalidate) {
            requestLayout()
        }
    }

    /**
     * Set progress bar progress height
     *
     * @param height is given in pixels
     */
    fun setProgressHeight(height: Float) {
        setProgressHeight(height, true)
    }

    private fun setProgressHeight(height: Float, invalidate: Boolean) {
        progressLineHeight = height
        if (invalidate) {
            requestLayout()
        }
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setBackgroundColor(red: Int, green: Int, blue: Int) {
        setBackgroundColor(0xFF, red, green, blue)
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param alpha hex value for alpha
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setBackgroundColor(alpha: Int, red: Int, green: Int, blue: Int) {
        progressBgColor = Color.argb(alpha, red, green, blue)
        invalidate()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setBackgroundColor(color: Color) {
        setBackgroundColor(color)
    }

    /**
     * You can simulate the use of this method with by calling [.setBackgroundColor] with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setBackgroundColorResource(@ColorRes resId: Int) {
        setBackgroundColor(context.getColor(resId))
    }

    override fun setBackgroundColor(color: Int) {
        progressBgColor = color
        invalidate()
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setProgressColor(red: Int, green: Int, blue: Int) {
        setProgressColor(0xFF, red, green, blue)
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param alpha hex value for alpha
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setProgressColor(alpha: Int, red: Int, green: Int, blue: Int) {
        progressColor = Color.argb(alpha, red, green, blue)
        invalidate()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setProgressColor(color: Color) {
        setProgressColor(color.toArgb())
    }

    /**
     * You can simulate the use of this method with by calling [.setProgressColor] with ContextCompat:
     * setProgressColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setProgressColorResource(@ColorRes resId: Int) {
        setProgressColor(context.getColor(resId))
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        invalidate()
    }

    fun setThumbsImage(bitmap: Bitmap) {
        setThumbNormalImage(bitmap)
        setThumbPressedImage(bitmap)
    }

    fun setThumbsImageResource(@DrawableRes resId: Int) {
        setThumbNormalImageResource(resId)
        setThumbPressedImageResource(resId)
    }

    fun setThumbNormalImage(bitmap: Bitmap) {
        thumbnail = bitmap
        thumbnailPressed =
            if (::thumbnailPressed.isInitialized.not()) thumbnail else thumbnailPressed
        measureThumb()
        updatePadding()
        requestLayout()
    }

    fun setThumbNormalImageResource(@DrawableRes resId: Int) {
        val drawable = resources.getDrawable(resId, null)
        thumbnail =
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(Canvas(thumbnail))
        setThumbNormalImage(thumbnail)
    }

    fun setThumbPressedImage(bitmap: Bitmap) {
        thumbnailPressed = bitmap
        thumbnail = if (::thumbnail.isInitialized.not()) thumbnailPressed else thumbnail
        measureThumbPressed()
        updatePadding()
        requestLayout()
    }

    fun setThumbPressedImageResource(@DrawableRes resId: Int) {
        val drawable = resources.getDrawable(resId, null)
        thumbnailPressed =
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(Canvas(thumbnailPressed))
        setThumbPressedImage(thumbnailPressed)
    }

    //</editor-fold>
    private fun onChangedValues() {
        actionCallback?.onChanged(selectedMinimumVal, selectedMaximumVal)
    }

    private fun onChangingValues() {
        actionCallback?.onChanging(selectedMinimumVal, selectedMaximumVal)
    }

    private fun measureThumb() {
        thumbnailHalfWidth = 0.5f * thumbnail.width
        thumbnailHalfHeight = 0.5f * thumbnail.height
    }

    private fun measureThumbPressed() {
        thumbnailPressedHalfWidth = 0.5f * thumbnailPressed.width
        thumbnailPressedHalfHeight = 0.5f * thumbnailPressed.height
    }

    private fun updatePadding() {
        val width = max(thumbnailHalfWidth, thumbnailPressedHalfWidth)
        val height = max(thumbnailHalfHeight, thumbnailPressedHalfHeight)
        paddingValue = max(max(width, height), stepRadius)
    }

    /**
     * Converts a normalized value to a value space between absolute minimum and maximum.
     *
     * @param normalized The value to "de-normalize".
     * @return The "de-normalized" value.
     */
    private fun normalizedToValue(normalized: Float): Float {
        return minimumVal + normalized * (maximumVal - minimumVal)
    }

    /**
     * Converts the given value to a normalized value.
     *
     * @param value The value to normalize.
     * @return The normalized value.
     */
    private fun valueToNormalized(value: Float): Float {
        return if (0f == maximumVal - minimumVal) 0f /* prevent division by zero */ else (value - minimumVal) / (maximumVal - minimumVal)
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoordinate The normalized value to convert.
     * @return The converted value in screen space.
     */
    private fun normalizedToScreen(normalizedCoordinate: Float): Float {
        return paddingValue + normalizedCoordinate * (width - 2 * paddingValue)
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoordinate The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private fun screenToNormalized(screenCoordinate: Float): Float {
        val widthValue = width
        return if (widthValue <= 2 * paddingValue) {
            0f //prevent division by zero
        } else {
            val resultNormalized = (screenCoordinate - paddingValue) / (widthValue - 2 * paddingValue)
            min(1f, max(0f, resultNormalized))
        }
    }

    //<editor-fold desc="Touch logic">
    private fun trackTouchEvent(event: MotionEvent) {
        val indexOfPointer = event.findPointerIndex(activePointerId)
        var pointX = event.getX(indexOfPointer)
        if (isStepProgress) {
            pointX = getClosestStep(screenToNormalized(pointX))
        }
        if (Thumb.MIN == pressedThumb) {
            setNormalizedMinValue(if (isStepProgress) pointX else screenToNormalized(pointX))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(if (isStepProgress) pointX else screenToNormalized(pointX))
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val indexOfPointer = ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val idOfPointer = ev.getPointerId(indexOfPointer)
        if (idOfPointer == activePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (indexOfPointer == 0) 1 else 0
            downMotionX = ev.getX(newPointerIndex)
            activePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun onStartTrackingTouch() {
        isDragging = true
    }

    private fun onStopTrackingTouch() {
        isDragging = false
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private fun evalPressedThumb(touchX: Float): Thumb? {
        var resultThumb: Thumb? = null
        val thumbPressedMin = isInThumbRange(touchX, normalizedMinimum)
        val thumbPressedMax = isInThumbRange(touchX, normalizedMaximum)
        if (thumbPressedMin && thumbPressedMax) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a
            // corner, not being able to drag them apart anymore.
            resultThumb = if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (thumbPressedMin) {
            resultThumb = Thumb.MIN
        } else if (thumbPressedMax) {
            resultThumb = Thumb.MAX
        }
        return resultThumb
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private fun isInThumbRange(touchX: Float, normalizedThumbValue: Float): Boolean {
        return abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbnailHalfWidth
    }
    //</editor-fold>

    //<editor-fold desc="Progress-by-Step logic">
    /**
     * When enabled, min and max are set to 0 and 100 (default values) and cannot be changed
     *
     * @param enable if true, enables Progress by Step
     */
    fun enableProgressBySteps(enable: Boolean) {
        isStepProgress = enable
        if (enable) {
            setMinValue(BASE_MIN_PROGRESS)
            setMaxValue(BASE_MAX_PROGRESS)
        }
        invalidate()
    }

    /**
     * Note: 0 and 100 will automatically be added as min and max respectively, you don't need to add it again.
     *
     * @param steps values for each step
     */
    fun setProgressSteps(vararg steps: Float) {
        val result: MutableList<Float> = ArrayList()
        for (step in steps) {
            result.add(step)
        }
        setProgressSteps(result)
    }

    /**
     * Note: 0 and 100 will automatically be added as min and max respectively, you don't need to add it again.
     *
     * @param steps values for each step
     */
    fun setProgressSteps(steps: List<Float>) {
        progressStepArr.clear()
        progressStepArr.add(valueToNormalized(BASE_MIN_PROGRESS))
        for (step in steps) {
            progressStepArr.add(valueToNormalized(step))
        }
        progressStepArr.add(valueToNormalized(BASE_MAX_PROGRESS))
        invalidate()
    }

    /**
     * @param radius in pixels
     */
    fun setProgressStepRadius(radius: Float) {
        stepRadius = radius
        updatePadding()
        invalidate()
    }

    val progressSteps: List<Float>
        get() {
            val result: MutableList<Float> = ArrayList()
            for (step in progressStepArr) {
                result.add(normalizedToValue(step))
            }
            return result
        }

    private fun getClosestStep(value: Float): Float {
        var distance = abs(progressStepArr[0] - value)
        var currentDis: Float
        var closest = 0f
        for (step in progressStepArr) {
            currentDis = abs(step - value)
            if (currentDis < distance) {
                closest = step
                distance = currentDis
            }
        }
        return closest
    }

    //</editor-fold>

    //<editor-fold desc="View life-cycle">
    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 200
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        }
        val maxThumbHeight = max(thumbnail.height, thumbnailPressed.height)
        val maxHeight = max(progressLineHeight, bgLineHeight).toInt()
        var height = max(max(maxThumbHeight, maxHeight), dpToPx(stepRadius))
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            height = min(height, MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        // draw seek bar background line
        val cornersRadidus: Float =
            max(bgLineHeight, progressLineHeight) * if (isRoundedCorner) 0.5f else 0f
        bgLineRectF[paddingValue, 0.5f * (height - bgLineHeight), width - paddingValue] =
            0.5f * (height + bgLineHeight)
        paint.color = progressBgColor
        canvas.drawRoundRect(bgLineRectF, cornersRadidus, cornersRadidus, paint)
        bgLineRectF.left = normalizedToScreen(normalizedMinimum)
        bgLineRectF.right = normalizedToScreen(normalizedMaximum)

        // draw seek bar progress line
        progressLineRectF[paddingValue, 0.5f * (height - progressLineHeight), width - paddingValue] =
            0.5f * (height + progressLineHeight)
        progressLineRectF.left = normalizedToScreen(normalizedMinimum)
        progressLineRectF.right = normalizedToScreen(normalizedMaximum)
        paint.color = progressColor
        canvas.drawRoundRect(progressLineRectF, cornersRadidus, cornersRadidus, paint)
        val minXValue = normalizedToScreen(normalizedMinimum)
        val maxXValue = normalizedToScreen(normalizedMaximum)

        // draw progress steps, if enabled
        if (isStepProgress) {
            var stepX: Float
            for (step in progressStepArr) {
                stepX = normalizedToScreen(step)
                paint.color =
                    if (stepX > maxXValue || stepX < minXValue) progressBgColor else progressColor
                drawStep(canvas, normalizedToScreen(step), stepRadius, paint)
            }
        }

        // draw thumbs
        drawThumb(canvas, minXValue, Thumb.MIN == pressedThumb)
        drawThumb(canvas, maxXValue, Thumb.MAX == pressedThumb)
    }

    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the image.
     * @param pressed          Is the thumb currently in "pressed" state
     */
    private fun drawThumb(canvas: Canvas, screenCoordinate: Float, pressed: Boolean) {
        canvas.drawBitmap(
            if (pressed) thumbnailPressed else thumbnail,
            screenCoordinate - if (pressed) thumbnailPressedHalfWidth else thumbnailHalfWidth,
            0.5f * height - if (pressed) thumbnailPressedHalfHeight else thumbnailHalfHeight, paint
        )
    }

    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the step.
     * @param radius           Step circle radius
     * @param paint            Paint to color the steps
     */
    private fun drawStep(canvas: Canvas, screenCoordinate: Float, radius: Float, paint: Paint) {
        canvas.drawCircle(screenCoordinate, 0.5f * height, radius, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val pointerIndex: Int
        val eventAction = event.action
        when (eventAction and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                activePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(activePointerId)
                downMotionX = event.getX(pointerIndex)
                pressedThumb = evalPressedThumb(downMotionX)

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event)
                }
                isPressed = true
                invalidate()
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag()
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {
                if (isDragging) {
                    trackTouchEvent(event)
                } else {
                    // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(activePointerId)
                    val pointX = event.getX(pointerIndex)
                    if (abs(pointX - downMotionX) > scaledTouchSlop) {
                        isPressed = true
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                onChangingValues()
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                pressedThumb = null
                invalidate()
                actionCallback?.onChanged(selectedMinimumVal, selectedMaximumVal)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                // final int index = ev.getActionIndex();
                downMotionX = event.getX(index)
                activePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
            else -> {
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putFloat("MIN", normalizedMinimum)
        bundle.putFloat("MAX", normalizedMaximum)
        bundle.putFloat("MIN_RANGE", minimumVal)
        bundle.putFloat("MAX_RANGE", maximumVal)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinimum = bundle.getFloat("MIN")
        normalizedMaximum = bundle.getFloat("MAX")
        minimumVal = bundle.getFloat("MIN_RANGE")
        maximumVal = bundle.getFloat("MAX_RANGE")
        onChangedValues()
        onChangingValues()
    }

    //</editor-fold>

    private fun dpToPx(dp: Float) =
        ceil(dp * Resources.getSystem().displayMetrics.density.toDouble()).toInt()

    private enum class Thumb {
        MIN, MAX
    }

    companion object {
        private const val INVALID_POINTER_ID = 255
        private const val ACTION_POINTER_UP = 0x6
        private const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        private const val ACTION_POINTER_INDEX_SHIFT = 8
        private val BASE_COLOR = Color.argb(0xFF, 0xFB, 0x76, 0x09)
        private val BASE_BACKGROUND_COLOR = Color.argb(0xFF, 0xC0, 0xC0, 0xC0)
        private const val BASE_PROGRESS_HEIGHT = 10
        private const val BASE_STEP_RADIUS = BASE_PROGRESS_HEIGHT + 2
        private const val BASE_MIN_PROGRESS = 0f
        private const val BASE_MAX_PROGRESS = 100f
        private const val BASE_ANIMATE_DURATION: Long = 1000
    }
}

@Suppress("unused")
inline fun SeekBarRangedView.addActionListener(
    crossinline onChanged: (minValue: Float, maxValue: Float) -> Unit = { _, _ -> },
    crossinline onChanging: (minValue: Float, maxValue: Float) -> Unit = { _, _ -> },
): SeekBarRangedView.SeekBarRangedChangeCallback {
    val callback = object : SeekBarRangedView.SeekBarRangedChangeCallback {

        override fun onChanged(minValue: Float, maxValue: Float) {
            onChanged.invoke(minValue, maxValue)
        }

        override fun onChanging(minValue: Float, maxValue: Float) {
            onChanging.invoke(minValue, maxValue)
        }
    }
    actionCallback = callback
    return callback
}
