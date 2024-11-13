package com.bloodpressure.app.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.RelativeLayout
import com.bloodpressure.app.R
import com.bloodpressure.app.utils.Logger
import kotlin.math.max

class InfiniteRippleButton : RelativeLayout {
    private var WIDTH = 0
    private var HEIGHT = 0

    /**
     * Set framerate for Ripple animation
     *
     * @param frameRate New framerate value, default is 10
     */
    var frameRate = 10

    /**
     * Duration of the Ripple animation in ms
     *
     * @param rippleDuration Duration, default is 400ms
     */
    var rippleDuration = 400

    /**
     * Set alpha for ripple effect color
     *
     * @param rippleAlpha Alpha value between 0 and 255, default is 90
     */
    var rippleAlpha = 90
    private var canvasHandler: Handler? = null
    private var radiusMax = 0f
    private var animationRunning = false
    private var timer = 0
    private var timerEmpty = 0
    private var durationEmpty = -1
    private var x = -1f
    private var y = -1f

    /**
     * Duration of the ending animation in ms
     *
     * @param zoomDuration Duration, default is 200ms
     */
    var zoomDuration = 0L

    /**
     * Scale of the end animation
     *
     * @param zoomScale Value of scale animation, default is 1.03f
     */
    var zoomScale = 0f
    private var scaleAnimation: ScaleAnimation? = null

    /**
     * At the end of Ripple effect, the child views has to zoom
     *
     * @param hasToZoom Do the child views have to zoom ? default is False
     */
    var isZooming: Boolean? = null

    /**
     * Set if ripple animation has to be centered in its parent view or not, default is False
     *
     * @param isCentered
     */
    var isCentered: Boolean? = null
    private var rippleType: Int? = null
    private var paint: Paint? = null
    private var originBitmap: Bitmap? = null
    private var rippleColor = 0

    /**
     * Set Ripple padding if you want to avoid some graphic glitch
     *
     * @param ripplePadding New Ripple padding in pixel, default is 0px
     */
    var ripplePadding = 0
    private var gestureDetector: GestureDetector? = null
    private val runnable = Runnable { invalidate() }
    private var onCompletionListener: OnRippleCompleteListener? = null
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    /**
     * Method that initializes all fields and sets listeners
     *
     * @param context Context used to create this view
     * @param attrs Attribute used to initialize fields
     */
    private fun init(context: Context, attrs: AttributeSet) {
        if (isInEditMode) return
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.InfiniteRippleButton)
        rippleColor = typedArray.getColor(
            R.styleable.InfiniteRippleButton_rv_color,
            resources.getColor(R.color.rippleColor)
        )
        rippleType = typedArray.getInt(R.styleable.InfiniteRippleButton_rv_type, 0)
        isZooming = typedArray.getBoolean(R.styleable.InfiniteRippleButton_rv_zoom, false)
        isCentered = typedArray.getBoolean(R.styleable.InfiniteRippleButton_rv_centered, false)
        rippleDuration =
            typedArray.getInteger(R.styleable.InfiniteRippleButton_rv_rippleDuration, rippleDuration)
        frameRate = typedArray.getInteger(R.styleable.InfiniteRippleButton_rv_framerate, frameRate)
        rippleAlpha = typedArray.getInteger(R.styleable.InfiniteRippleButton_rv_alpha, rippleAlpha)
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.InfiniteRippleButton_rv_ripplePadding, 0)
        canvasHandler = Handler()
        zoomScale = typedArray.getFloat(R.styleable.InfiniteRippleButton_rv_zoomScale, 1.03f)
        zoomDuration = typedArray.getInt(R.styleable.InfiniteRippleButton_rv_zoomDuration, 200).toLong()
        typedArray.recycle()
        paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = rippleColor
            alpha = rippleAlpha
        }
        this.setWillNotDraw(false)
        this.isDrawingCacheEnabled = true
        this.isClickable = true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (animationRunning) {
            canvas.save()
            if (rippleDuration <= timer * frameRate) {
                animationRunning = false
                timer = 0
                durationEmpty = -1
                timerEmpty = 0
                // There is problem on Android M where canvas.restore() seems to be called automatically
                // For now, don't call canvas.restore() manually on Android M (API 23)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    canvas.restore()
                }
                invalidate()
                if (onCompletionListener != null) onCompletionListener!!.onComplete(this)
                return
            } else canvasHandler?.postDelayed(runnable, frameRate.toLong())
            if (timer == 0) canvas.save()
            canvas.drawCircle(
                x,
                y,
                radiusMax * (timer.toFloat() * frameRate / rippleDuration),
                paint!!
            )
            paint!!.color = Color.parseColor("#ffff4444")
            if (rippleType == 1 && originBitmap != null && timer.toFloat() * frameRate / rippleDuration > 0.4f) {
                if (durationEmpty == -1) durationEmpty = rippleDuration - timer * frameRate
                timerEmpty++
                val tmpBitmap: Bitmap =
                    getCircleBitmap((radiusMax * (timerEmpty.toFloat() * frameRate / durationEmpty)).toInt())
                canvas.drawBitmap(tmpBitmap, 0f, 0f, paint)
                tmpBitmap.recycle()
            }
            paint!!.color = rippleColor
            if (rippleType == 1) {
                if (timer.toFloat() * frameRate / rippleDuration > 0.6f) paint!!.alpha =
                    (rippleAlpha - rippleAlpha * (timerEmpty.toFloat() * frameRate / durationEmpty)).toInt() else paint!!.alpha =
                    rippleAlpha
            } else paint!!.alpha =
                (rippleAlpha - rippleAlpha * (timer.toFloat() * frameRate / rippleDuration)).toInt()
            timer++
        }
        animateRipple(0f, 0f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        WIDTH = w
        HEIGHT = h
        scaleAnimation = ScaleAnimation(1.0f, zoomScale, 1.0f, zoomScale, w / 2f, h / 2f)
        scaleAnimation!!.duration = zoomDuration
        scaleAnimation!!.repeatMode = Animation.REVERSE
        scaleAnimation!!.repeatCount = Animation.INFINITE
    }

    /**
     * Launch Ripple animation for the current view with a MotionEvent
     *
     * @param event MotionEvent registered by the Ripple gesture listener
     */
    fun animateRipple(event: MotionEvent) {
        Logger.d("event: ${event.x} ${event.y}")
        createAnimation(event.x, event.y)
    }

    /**
     * Launch Ripple animation for the current view centered at x and y position
     *
     * @param x Horizontal position of the ripple center
     * @param y Vertical position of the ripple center
     */
    fun animateRipple(x: Float, y: Float) {
        createAnimation(x, y)
    }

    /**
     * Create Ripple animation centered at x, y
     *
     * @param x Horizontal position of the ripple center
     * @param y Vertical position of the ripple center
     */
    private fun createAnimation(x: Float, y: Float) {
        if (this.isEnabled && !animationRunning) {
            if (isZooming!!) this.startAnimation(scaleAnimation)
            radiusMax = max(WIDTH, HEIGHT).toFloat()
            if (rippleType != 2) radiusMax /= 2f
            radiusMax -= ripplePadding.toFloat()
            if (isCentered!! || rippleType == 1) {
                this.x = measuredWidth / 2f
                this.y = measuredHeight / 2f
            } else {
                this.x = x
                this.y = y
            }
            animationRunning = true
            if (rippleType == 1 && originBitmap == null) originBitmap = getDrawingCache(true)
            invalidate()
        }
    }

    private fun getCircleBitmap(radius: Int): Bitmap {
        val output: Bitmap = Bitmap.createBitmap(
            originBitmap!!.width,
            originBitmap!!.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(
            (x - radius).toInt(),
            (y - radius).toInt(),
            (x + radius).toInt(),
            (y + radius).toInt()
        )
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(x, y, radius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        originBitmap?.let { canvas.drawBitmap(it, rect, rect, paint) }
        return output
    }

    /**
     * Set Ripple color, default is #FFFFFF
     *
     * @param rippleColor New color resource
     */
    fun setRippleColor(rippleColor: Int) {
        this.rippleColor = resources.getColor(rippleColor)
    }

    fun getRippleColor(): Int {
        return rippleColor
    }

    fun getRippleType(): RippleType {
        return RippleType.values()[rippleType!!]
    }

    /**
     * Set Ripple type, default is RippleType.SIMPLE
     *
     * @param rippleType New Ripple type for next animation
     */
    fun setRippleType(rippleType: RippleType) {
        this.rippleType = rippleType.ordinal
    }

    fun setOnRippleCompleteListener(listener: OnRippleCompleteListener?) {
        onCompletionListener = listener
    }

    /**
     * Defines a callback called at the end of the Ripple effect
     */
    interface OnRippleCompleteListener {
        fun onComplete(infiniteRippleButton: InfiniteRippleButton?)
    }

    enum class RippleType(var type: Int) {
        SIMPLE(0), DOUBLE(1), RECTANGLE(2)
    }
}